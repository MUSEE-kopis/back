package muse_kopis.muse.performance.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminPerformanceRepository extends JpaRepository<AdminPerformance, Long> {

    default AdminPerformance getAdminPerformanceById(Long admin) {
        return findById(admin).orElse(null);
    }
}
