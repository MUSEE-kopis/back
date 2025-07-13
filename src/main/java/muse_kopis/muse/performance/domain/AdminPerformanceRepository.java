package muse_kopis.muse.performance.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminPerformanceRepository extends JpaRepository<AdminPerformance, Long> {
    Optional<AdminPerformance> findById(Long adminId);

    void removeAllById(Long id);
}
