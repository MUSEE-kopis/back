package muse_kopis.muse.usergenre.application;

import jakarta.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muse_kopis.muse.auth.oauth.domain.OauthMember;
import muse_kopis.muse.auth.oauth.domain.OauthMemberRepository;
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
        List<Long> performanceIds = Arrays.asList(
                1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L,
                11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L,
                21L, 22L, 23L, 24L, 25L, 26L, 27L, 28L, 29L, 30L
        );
        List<Performance> performances = performanceRepository.findAllByIdIn(performanceIds);
        return performances.stream().map(PerformanceResponse::from).toList();
    }
}