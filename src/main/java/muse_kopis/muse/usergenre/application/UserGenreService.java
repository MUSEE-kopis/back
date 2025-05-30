package muse_kopis.muse.usergenre.application;

import static muse_kopis.muse.genre.domain.GenreType.CREATIVE_MUSICAL;
import static muse_kopis.muse.genre.domain.GenreType.ETC_MUSICAL;
import static muse_kopis.muse.genre.domain.GenreType.LICENSE;
import static muse_kopis.muse.genre.domain.GenreType.NUMBER_PERFORMANCE;
import static muse_kopis.muse.genre.domain.GenreType.ORIGINAL_OR_INTERNATIONAL;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muse_kopis.muse.auth.oauth.domain.OauthMember;
import muse_kopis.muse.auth.oauth.domain.OauthMemberRepository;
import muse_kopis.muse.genre.domain.GenreType;
import muse_kopis.muse.performance.domain.Performance;
import muse_kopis.muse.performance.domain.PerformanceRepository;
import muse_kopis.muse.performance.domain.dto.PerformanceResponse;
import muse_kopis.muse.genre.domain.Genre;
import muse_kopis.muse.genre.domain.GenreRepository;
import muse_kopis.muse.ticketbook.domain.dto.UserGenreEvent;
import muse_kopis.muse.usergenre.domain.UserGenre;
import muse_kopis.muse.usergenre.domain.UserGenreRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserGenreService {

    private final static int totalTarget = 30;
    private final GenreRepository genreRepository;
    private final UserGenreRepository userGenreRepository;
    private final PerformanceRepository performanceRepository;
    private final OauthMemberRepository oauthMemberRepository;

    /**
    * User 가 좋아요를 누르거나 티켓북을 등록하면 선호 장르 업데이트를 진행 Event 로 처리
    */
    @EventListener
    @Transactional
    public void updateUserGenre(UserGenreEvent event) {
        OauthMember oauthMember = oauthMemberRepository.getByOauthMemberId(event.memberId());
        Performance performance = performanceRepository.getByPerformanceId(event.performanceId());
        weightUpdate(oauthMember, performance);
    }

    /**
     * Onboarding 시에 초기 선호 장르 업데이틀 위해서 사용, 여러 장르를 한번에 업데이트 하기 위한 용도
     * */
    @Transactional
    public void updateUserGenres(Long memberId, List<Long> performanceIds) {
        performanceIds.forEach(performanceId -> updateUserGenre(memberId, performanceId));
    }

    @Transactional
    public void updateUserGenre(Long memberId, Long performanceId) {
        OauthMember oauthMember = oauthMemberRepository.getByOauthMemberId(memberId);
        Performance performance = performanceRepository.getByPerformanceId(performanceId);
        weightUpdate(oauthMember, performance);
    }

    private void weightUpdate(OauthMember oauthMember, Performance performance) {
        List<Genre> genre = genreRepository.findAllByPerformance(performance);
        log.info(genre.toString());
        UserGenre userGenre = userGenreRepository.findByOauthMember(oauthMember)
                .orElseGet(() -> initGenre(oauthMember));
        try {
            genre.forEach(it -> userGenre.incrementGenreWeight(it.getGenre()));
        } catch (NullPointerException e) {
            log.warn("장르 타입이 NULL 입니다.");
        }
    }

    @EventListener
    public UserGenre initGenre(OauthMember oauthMember) {
        return userGenreRepository.save(new UserGenre(oauthMember));
    }

    @Transactional
    public List<PerformanceResponse> showOnboarding() {
        List<GenreType> genreTypes = Arrays.asList(
                ORIGINAL_OR_INTERNATIONAL, CREATIVE_MUSICAL, NUMBER_PERFORMANCE, ETC_MUSICAL, LICENSE
        );
        int perGenreTarget = totalTarget / genreTypes.size(); // ex. 30개 / 5장르 = 6개
        Set<Performance> result = new LinkedHashSet<>(); // 중복 방지를 위해 Set 사용
        // 1. 장르별로 공연 수집 (최대 perGenreTarget개씩)
        for (GenreType genre : genreTypes) {
            List<Performance> performances = performanceRepository.findRandomByGenreType(genre);
            int count = 0;
            for (Performance p : performances) {
                if (count >= perGenreTarget) break;
                if (result.add(p)) { // 중복이 아닐 때만 추가
                    count++;
                }
            }
        }
        // 2. 부족한 경우 전체 공연에서 보충
        if (result.size() < totalTarget) {
            int remaining = totalTarget - result.size();
            List<Performance> extra = performanceRepository.findAll();
            Collections.shuffle(extra);
            for (Performance p : extra) {
                if (result.size() >= totalTarget) break;
                result.add(p); // Set이므로 중복 자동 제거
            }
        }
        // 3. 변환 및 결과 반환
        return result.stream()
                .limit(totalTarget) // 혹시 초과한 경우 제한
                .map(PerformanceResponse::from)
                .toList();
    }
}