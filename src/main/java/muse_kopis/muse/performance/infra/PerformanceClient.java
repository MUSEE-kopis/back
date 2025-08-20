package muse_kopis.muse.performance.infra;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import jakarta.transaction.Transactional;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muse_kopis.muse.actor.domain.Actor;
import muse_kopis.muse.actor.domain.CastMember;
import muse_kopis.muse.actor.domain.CastMemberRepository;
import muse_kopis.muse.common.api.FetchFailException;
import muse_kopis.muse.common.performance.NotFoundPerformanceException;
import muse_kopis.muse.performance.domain.Performance;
import muse_kopis.muse.performance.domain.PerformanceRepository;
import muse_kopis.muse.performance.domain.dto.Boxofs;
import muse_kopis.muse.performance.domain.dto.Boxofs.Boxof;
import muse_kopis.muse.performance.domain.dto.KOPISPerformanceDetailResponse;
import muse_kopis.muse.performance.domain.dto.KOPISPerformanceDetailResponse.Detail;
import muse_kopis.muse.performance.domain.dto.KOPISPerformanceResponse;
import muse_kopis.muse.performance.domain.dto.KOPISPerformanceResponse.DB;
import muse_kopis.muse.performance.domain.dto.PerformanceResponse;
import muse_kopis.muse.performance.shared.normalizer.PerformanceNameNormalizer;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class PerformanceClient {

    @Value("${API_URL_BOX_OFFICE}")
    private String API_URL_BOX_OFFICE;

    @Value("${KOPIS_API_KEY}")
    private String kopisKey;

    @Value("${API_URL}")
    private String API_URL;
    private final static String BLANK_OR_PARENTHESIS = "[\\s()]";
    private final static String UPCOMING = "공연예정";
    private final static String CURRENT = "공연중";
    private final static String GENRE = "GGGA";
    private final static String BLANK_OR_COMMA = "[,\\s]+";
    private final PerformanceRepository performanceRepository;
    private final CastMemberRepository castMemberRepository;
    private final RestTemplate restTemplate;
    private final XmlMapper xmlMapper;

    @Transactional
    @Retryable(value = { SQLException.class }, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void fetchPerformances(String startDate, String endDate, String currentPage, String rows, String state, String genre) {
        String url = API_URL + "?service=" + kopisKey + "&stdate=" + startDate + "&eddate=" + endDate +
                "&cpage=" + currentPage + "&rows=" + rows + "&prfstate=" + state + "&shcate=" + genre;
        log.info("데이터 요청 {}, {}", currentPage, state);
        String response = restTemplate.getForObject(url, String.class);
        try {
            KOPISPerformanceResponse KOPISPerformanceResponse = xmlMapper.readValue(response, KOPISPerformanceResponse.class);
            saveAllPerformance(KOPISPerformanceResponse);
            log.info("저장 완료 {}, {}", currentPage, state);
        } catch (Exception e) {
            throw new FetchFailException("데이터 패치에 실패했습니다.");
        }
    }

    private void saveAllPerformance(KOPISPerformanceResponse KOPISPerformanceResponse) throws JsonProcessingException {
        List<DB> list = Optional.ofNullable(KOPISPerformanceResponse.db()).orElse(Collections.emptyList());
        for (DB db : list) {
            String performanceId = db.mt20id();
            KOPISPerformanceDetailResponse performanceResponse = fetchPerformanceDetail(performanceId);
            if(performanceResponse.detail().isEmpty()) {
                continue;
            }
            Detail performanceDetail = performanceResponse.detail().getFirst();
            Performance performance = Performance.from(performanceDetail);
            if (performanceRepository.existsPerformance(
                    performance.getPerformanceName(),
                    performance.getVenue(),
                    performance.getStartDate(),
                    performance.getEndDate())) {
                log.info("이미 존재하는 공연입니다.");
                continue;
            }
            performanceRepository.save(performance);
            List<CastMember> castMembers = Arrays.stream(performanceDetail.cast().split(BLANK_OR_COMMA))
                    .map(String::trim)
                    .map(name -> name.endsWith("등") ? name.substring(0, name.length() - 1).trim() : name)
                    .filter(name -> !name.isEmpty())  // 빈 문자열 필터링
                    .map(name -> new CastMember(new Actor(name.replace("\"", ""), "", ""), performance, ""))
                    .toList();
            castMemberRepository.saveAll(castMembers);
        }
    }   // 동명이인 방지를 위한 actorId 생김에 따른 수정 필요

    @Transactional
    public List<PerformanceResponse> fetchPopularPerformance() {
        String url = API_URL_BOX_OFFICE + "?service=" + kopisKey + "&stdate="+ LocalDate.now().minusDays(30).format(
                DateTimeFormatter.ofPattern("yyyyMMdd"))+ "&eddate=" + LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "&catecode=" + GENRE;
        log.info("url = {}", url);
        try {
            String response = restTemplate.getForObject(url, String.class);
            Boxofs boxofs = xmlMapper.readValue(response, Boxofs.class);
            List<Boxof> boxofList = boxofs.boxof()
                    .stream()
                    .limit(50)
                    .toList();
            log.info(boxofList.toString());

            LevenshteinDistance levenshtein = new LevenshteinDistance();

            List<Performance> allPerformances = performanceRepository.findAllByStateOrState(CURRENT, UPCOMING);

            Map<Performance, Integer> bestMatches = new LinkedHashMap<>();

            for (Boxof apiPerf : boxofList) {
                Optional<Map.Entry<Performance, Integer>> bestMatchEntry = allPerformances.stream()
                        .map(dbPerf -> {
                            int titleDistance = levenshtein.apply(PerformanceNameNormalizer.normalizeTitle(dbPerf.getPerformanceName()), PerformanceNameNormalizer.normalizeTitle(apiPerf.prfnm()));
                            int venueDistance = levenshtein.apply(dbPerf.getVenue().replaceAll(BLANK_OR_PARENTHESIS, ""), apiPerf.prfplcnm().replaceAll(BLANK_OR_PARENTHESIS, ""));

                            if (titleDistance <= 3 && venueDistance <= 5) {
                                return Optional.of(Map.entry(dbPerf, titleDistance + venueDistance));
                            }
                            return Optional.<Map.Entry<Performance, Integer>>empty();
                        })
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .min(Comparator.comparingInt(Map.Entry::getValue));
                bestMatchEntry.ifPresent(entry -> {
                    Performance performance = entry.getKey();
                    Integer score = entry.getValue();
                    bestMatches.merge(performance, score, Integer::min);
                });
            }
            List<Performance> collect = bestMatches.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .limit(6)
                    .toList();
            log.info("collect : {}", collect.toString());
            return collect.stream().map(PerformanceResponse::from).collect(Collectors.toList());
        } catch (Exception e) {
            throw new NotFoundPerformanceException("공연을 찾을 수 없습니다.");
        }
    }

    private KOPISPerformanceDetailResponse fetchPerformanceDetail(String performanceId) throws JsonProcessingException {
        String url = API_URL + "/" + performanceId +"?service="+kopisKey;
        String response = restTemplate.getForObject(url, String.class);
        return xmlMapper.readValue(response, KOPISPerformanceDetailResponse.class);
    }

    public ByteArrayResource getPosterImage(Long performanceId) {
        Performance performance = performanceRepository.getByPerformanceId(performanceId);
        String url = performance.getPoster();
        try {
            byte[] imageByte = restTemplate.getForObject(url, byte[].class);
            return new ByteArrayResource(imageByte);
        } catch (Exception e) {
            throw new FetchFailException("이미지를 불러오지 못했습니다.");
        }
    }
}
