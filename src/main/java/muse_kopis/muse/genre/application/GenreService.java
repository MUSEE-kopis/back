package muse_kopis.muse.genre.application;

import jakarta.transaction.Transactional;
import java.util.List;

import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muse_kopis.muse.auth.oauth.domain.OauthMember;
import muse_kopis.muse.auth.oauth.domain.OauthMemberRepository;
import muse_kopis.muse.genre.domain.Genre;
import muse_kopis.muse.genre.domain.GenreRepository;
import muse_kopis.muse.genre.domain.GenreType;
import muse_kopis.muse.performance.domain.Performance;
import muse_kopis.muse.performance.domain.PerformanceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class GenreService {

    private final GenreRepository genreRepository;
    private final PerformanceRepository performanceRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final OauthMemberRepository oauthMemberRepository;

    public void saveGenre(Long performanceId, List<GenreType> genreTypes, Long memberId) {
        Performance performance = performanceRepository.getByPerformanceId(performanceId);
        OauthMember oauthMember = oauthMemberRepository.getByOauthMemberId(memberId);
        genreTypes.forEach(genreType -> genreRepository.save(new Genre(performance, genreType, oauthMember)));
        eventPublisher.publishEvent(performance);
    }

    @Transactional
    public void updateGenre(Long performanceId, List<GenreType> genres, Long memberId) {
        Performance performance = performanceRepository.getByPerformanceId(performanceId);
        OauthMember oauthMember = oauthMemberRepository.getByOauthMemberId(memberId);
        List<Genre> existGenres = genreRepository.findAllByPerformanceAndOauthMember(performance,
                oauthMember);
        existGenres.stream()
                .filter(genre -> !genres.contains(genre.getGenre()))
                .forEach(genreRepository::delete);
        Set<GenreType> existGenreTypes = existGenres.stream()
                .map(Genre::getGenre)
                .collect(Collectors.toSet());
        genres.stream()
                .filter(genreType -> !existGenreTypes.contains(genreType))
                .forEach(genreType -> genreRepository.save(new Genre(performance, genreType, oauthMember)));
    }

    public List<GenreType> getGenres(Long memberId, Long performanceId) {
        OauthMember oauthMember = oauthMemberRepository.getByOauthMemberId(memberId);
        Performance performance = performanceRepository.getByPerformanceId(performanceId);
        List<Genre> genres = genreRepository.findAllByPerformanceAndOauthMember(performance, oauthMember);
        return genres.stream().map(Genre::getGenre).collect(Collectors.toList());
    }

    @EventListener
    @Transactional
    public void updatePerformanceGenre(Performance performance) {
        GenreType genreType = genreRepository.mostFrequentGenre(performance);
        performance.updateGenre(genreType);
        performanceRepository.save(performance);
    }

    @Scheduled(cron = "0 0 2 ? * SUN", zone = "Asia/Seoul")
//    @Scheduled(fixedDelay = 3000)
    public void updatePerformanceGenre() {
        genreRepository.findAll().forEach(genre -> {
           try {
               if(genre.getGenre() != null) {
                   Performance performance = genre.getPerformance();
                   GenreType genreType = genreRepository.mostFrequentGenre(performance);
                   performance.updateGenre(genreType);
                   performanceRepository.save(performance);
                   log.info("updatePerformanceGenre {}", performance.getPerformanceName());
               }
           } catch (Exception e) {
               log.error("updatePerformanceGenre {}", e.getMessage());
           }
        });
    }
}
