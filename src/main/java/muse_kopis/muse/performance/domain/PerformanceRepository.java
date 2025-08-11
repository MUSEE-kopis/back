package muse_kopis.muse.performance.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import muse_kopis.muse.common.performance.NotFoundPerformanceException;
import muse_kopis.muse.genre.domain.GenreType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PerformanceRepository extends JpaRepository<Performance, Long> {

    default Performance getByPerformanceId(Long performanceId) {
        return findById(performanceId)
                .orElseThrow(() -> new NotFoundPerformanceException("공연을 찾을 수 없습니다."));
    }

    default Performance getByPerformanceNameAndVenue(String performanceName, String venue) {
        return findByPerformanceNameAndVenue(performanceName, venue)
                .orElseThrow(() -> new NotFoundPerformanceException("공연을 찾을 수 없습니다."));
    }

    @Query("SELECT p FROM Performance p WHERE p.state = :state ORDER BY function('RAND')")
    List<Performance> findAllByState(@Param("state") String state);

    @Query("SELECT p FROM Performance p WHERE :today BETWEEN p.startDate AND p.endDate")
    List<Performance> findPerformancesByDate(@Param("today") LocalDate today);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Performance p " +
            "WHERE p.performanceName = :performanceName " +
            "AND p.venue = :venue " +
            "AND p.startDate = :startDate " +
            "AND p.endDate = :endDate")
    Boolean existsPerformance(@Param("performanceName") String performanceName,
                              @Param("venue") String venue,
                              @Param("startDate") LocalDate startDate,
                              @Param("endDate") LocalDate endDate);

    @Query("SELECT DISTINCT p FROM Performance p " +
            "JOIN p.castMembers cm " +
            "JOIN cm.actor a " +
            "WHERE a.name = :favoriteActor")
    List<Performance> findPerformancesByFavoriteActor(@Param("favoriteActor") String favoriteActor);

    @Query(value = "SELECT p FROM Performance p WHERE p.genreType = :genreType ORDER BY FUNCTION('RAND')")
    List<Performance> findRandomByGenreType(@Param("genreType") GenreType genreType);
    @Query("SELECT p FROM Performance p WHERE REPLACE(p.performanceName, ' ', '') LIKE %:keyword%")
    List<Performance> findAllByPerformanceNameContains(@Param("keyword") String keyword);
    List<Performance> findByPerformanceName(String performanceName);
    List<Performance> findAllByIdIn(List<Long> performanceIds);
    List<Performance> findAllByStateOrState(String currentPerformances, String upcomingPerformances);
    List<Performance> findAllByGenreType(GenreType genreType);
    Optional<Performance> findByPerformanceNameAndVenue(String performanceName, String venue);
}
