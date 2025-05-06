package muse_kopis.muse.performance.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import muse_kopis.muse.actor.domain.CastMember;
import muse_kopis.muse.genre.domain.GenreType;
import muse_kopis.muse.performance.domain.dto.KOPISPerformanceDetailResponse.Detail;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Performance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String performanceName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String venue; // 공연장
    @OneToMany(mappedBy = "performance")
    private List<CastMember> castMembers;
    private String limitAge;
    private String price;
    private String poster;
    private String state; // 공연완료(03), 공연중(02), 공연예정(01)
    private String entertainment;
    @Column(length = 1000)
    private String performanceTime;
    @Enumerated(EnumType.STRING)
    private GenreType genreType;
    private Double score;
    private String ticketLink;

    public static Performance from(Detail performanceDetail) {
        return Performance.builder()
                .performanceTime(performanceDetail.performanceTime())
                .price(performanceDetail.price())
                .poster(performanceDetail.poster())
                .performanceTime(performanceDetail.performanceTime())
                .performanceName(performanceDetail.performanceName())
                .venue(performanceDetail.venue())
                .startDate(LocalDate.parse(performanceDetail.startDate(), DateTimeFormatter.ofPattern("yyyy.MM.dd")))
                .endDate(LocalDate.parse(performanceDetail.endDate(), DateTimeFormatter.ofPattern("yyyy.MM.dd")))
                .limitAge(performanceDetail.limitAge())
                .entertainment(performanceDetail.entertainment())
                .state(performanceDetail.state())
                .build();
    }

    @Override
    public String toString() {
        return castMembers.stream()
                .map(CastMember::toString)
                .collect(Collectors.joining(", "));
    }

    public void updateState(String state) {
        this.state = state;
    }

    public void updateGenre(GenreType genreType) {
        this.genreType = genreType;
    }
}
