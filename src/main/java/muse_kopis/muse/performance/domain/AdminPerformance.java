package muse_kopis.muse.performance.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPerformance {

    @Id
    private Long id;

    @OneToMany
    private List<Performance> performances;

    public List<Performance> performances() {
        return performances;
    }
}
