package muse_kopis.muse.performance.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muse_kopis.muse.actor.domain.FavoriteActor;
import muse_kopis.muse.actor.domain.FavoriteActorRepository;
import muse_kopis.muse.auth.oauth.domain.OauthMember;
import muse_kopis.muse.auth.oauth.domain.OauthMemberRepository;
import muse_kopis.muse.common.auth.UnAuthorizationException;
import muse_kopis.muse.common.performance.AdminNotSelectPerformanceException;
import muse_kopis.muse.performance.domain.AdminPerformance;
import muse_kopis.muse.performance.domain.AdminPerformanceRepository;
import muse_kopis.muse.performance.domain.Performance;
import muse_kopis.muse.performance.domain.PerformanceRepository;
import muse_kopis.muse.performance.domain.dto.AdminSelect;
import muse_kopis.muse.performance.domain.dto.PerformanceResponse;
import muse_kopis.muse.genre.domain.GenreType;
import muse_kopis.muse.performance.infra.PerformanceClient;
import muse_kopis.muse.usergenre.domain.UserGenre;
import muse_kopis.muse.usergenre.domain.UserGenreRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class PerformanceService {

    @Value("${adminId}")
    private Long adminId;
    private final static String CURRENT = "공연중";
    private final static String COMPLETE = "공연완료";
    private final PerformanceRepository performanceRepository;
    private final UserGenreRepository userGenreRepository;
    private final OauthMemberRepository oauthMemberRepository;
    private final PerformanceClient performanceClient;
    private final FavoriteActorRepository favoriteActorRepository;
    private final AdminPerformanceRepository adminPerformanceRepository;

    public List<PerformanceResponse> fetchPopularPerformance() {
        return performanceClient.fetchPopularPerformance();
    }

    public ByteArrayResource getPosterImage(Long performanceId) {
        return performanceClient.getPosterImage(performanceId);
    }

    public void fetchPerformances(String startDate, String endDate, String currentPage, String rows, String state, String genre)
            throws JsonProcessingException {
        performanceClient.fetchPerformances(startDate, endDate, currentPage, rows, state, genre);
    }

    @Transactional
    public PerformanceResponse findById(Long performanceId) {
        return PerformanceResponse.from(performanceRepository.getByPerformanceId(performanceId));
    }

    @Transactional
    public List<PerformanceResponse> findAllPerformance(){
        return adminPerformanceRepository.findById(adminId)
                .orElseThrow(() -> new AdminNotSelectPerformanceException("지정된 공연 리스트가 없습니다."))
                .performances()
                .stream()
                .map(PerformanceResponse::from).toList();
    }

    @Transactional
    public void selectPerformanceByAdmin(Long adminId, AdminSelect adminSelect) {
        if(Objects.equals(adminId, this.adminId)) {
            AdminPerformance admin = adminPerformanceRepository.getAdminPerformanceById(adminId);
            List<Performance> performances = admin.performances();
            performances.addAll(performanceRepository.findAllById(adminSelect.performanceIds()));

            AdminPerformance adminPerformance = AdminPerformance.builder()
                    .id(adminId)
                    .performances(performances)
                    .build();

            adminPerformanceRepository.save(adminPerformance);
        } else throw new UnAuthorizationException("유효하지 않은 토큰입니다. 관리자 권한을 확인하세요.");
    }

    @Transactional
    public void deletePerformanceByAdmin(Long adminId, AdminSelect adminSelect) {
        if(Objects.equals(adminId, this.adminId)) {
            AdminPerformance admin = adminPerformanceRepository.getAdminPerformanceById(adminId);
            List<Performance> performances = admin.performances();
            List<Performance> toRemove = performanceRepository.findAllById(adminSelect.performanceIds());
            performances.removeAll(toRemove);
        } else throw new UnAuthorizationException("유효하지 않은 토큰입니다. 관리자 권한을 확인하세요.");
    }

    @Transactional
    public List<PerformanceResponse> findAllPerformanceBySearch(String search) {
        return performanceRepository.findAllByPerformanceNameContains(search)
                .stream()
                .map(PerformanceResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<PerformanceResponse> recommendPerformance(Long memberId) {
        log.info("memberId : {}", memberId.toString());
        OauthMember oauthMember = oauthMemberRepository.getByOauthMemberId(memberId);
        UserGenre userGenre = userGenreRepository.getUserGenreByOauthMember(oauthMember);
        if (userGenre == null) {
            return Collections.emptyList();
        }
        GenreType favorite = userGenre.favorite();
        GenreType second = userGenre.second();
        GenreType third = userGenre.third();
        FavoriteActor favoriteActor = favoriteActorRepository.findFavoriteActorByMemberId(memberId);
        List<Performance> result = (favoriteActor != null) ?
                performanceRepository.findPerformancesByFavoriteActor(favoriteActor.getActor().getName())
                        .stream()
                        .filter(p -> p.getState().equals(CURRENT))
                        .collect(Collectors.toList())
                : new ArrayList<>();
        fillPerformanceList(result, favorite);
        fillPerformanceList(result, second);
        fillPerformanceList(result, third);
        return result.stream()
                .map(PerformanceResponse::from)
                .limit(7)
                .toList();
    }

    private void fillPerformanceList(List<Performance> result, GenreType genre) {
        if(genre == null) return;
        List<Performance> performances = performanceRepository.findAllByGenreType(genre);
        if (result.size() < 7 && !performances.isEmpty()) {
            Collections.shuffle(performances);
            result.addAll(performances.stream()
                    .distinct()
                    .filter(p -> p.getState().equals(CURRENT))
                    .limit(7 - result.size())
                    .toList());
        }
    }

    @Transactional
    public Set<PerformanceResponse> getRandomPerformance(Long memberId) {
        oauthMemberRepository.getByOauthMemberId(memberId);
        List<Performance> performances = performanceRepository.findAllByState(CURRENT);
        Set<PerformanceResponse> responses = new HashSet<>();
        while (responses.size() < 7){
            if (!performances.isEmpty()) {
                Random random = new Random();
                responses.add(PerformanceResponse.from(performances.get(random.nextInt(performances.size()))));
            }
        }
        return responses;
    }

    @Transactional
    @Scheduled(cron = "0 0 2 ? * SUN", zone = "Asia/Seoul")
    public void performanceStateUpdate() {
        List<Performance> performances = performanceRepository.findAllByState(CURRENT);
        performances.forEach(performance -> {
            if(performance.getEndDate().isBefore(ChronoLocalDate.from(LocalDateTime.now()))) {
                performance.updateState(COMPLETE);
            }
        });
        performanceRepository.saveAll(performances);
    }
}
