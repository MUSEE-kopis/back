package muse_kopis.muse.genre.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import muse_kopis.muse.auth.oauth.domain.OauthMember;
import muse_kopis.muse.performance.domain.Performance;

@Entity
@Getter
@NoArgsConstructor
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Performance performance;
    @Enumerated(EnumType.STRING)
    private GenreType genre;
    @ManyToOne
    private OauthMember oauthMember;

    public Genre(Performance performance, GenreType genre, OauthMember oauthMember) {
        this.performance = performance;
        this.genre = genre;
        this.oauthMember = oauthMember;
    }

    public Genre(Performance performance, GenreType genre) {
        this.performance = performance;
        this.genre = genre;
    }
}
