package muse_kopis.muse.genre.domain;

import java.util.List;

import java.util.Optional;
import muse_kopis.muse.auth.oauth.domain.OauthMember;
import muse_kopis.muse.common.genre.NotFoundGenreException;
import muse_kopis.muse.performance.domain.Performance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {

    default GenreType mostFrequentGenre(Performance performance) {
        return findMostFrequentGenreByPerformance(performance)
                .orElseThrow(() -> new NotFoundGenreException("장르가 없습니다.")).getFirst();
    }
    List<Genre> findAllByPerformance(Performance performance);
    @Query("SELECT g.genre " +
            "FROM Genre g " +
            "WHERE g.performance = :performance " +
            "GROUP BY g.genre " +
            "ORDER BY COUNT(g.genre) DESC, g.genre ASC")
    Optional<List<GenreType>> findMostFrequentGenreByPerformance(@Param("performance") Performance performance);
    List<Genre> findAllByPerformanceAndOauthMember(Performance performance, OauthMember oauthMember);
}
