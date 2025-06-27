package muse_kopis.muse.review.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import muse_kopis.muse.auth.oauth.domain.OauthMember;
import muse_kopis.muse.performance.domain.Performance;
import muse_kopis.muse.ticketbook.domain.TicketBook;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performance_id")
    private Performance performance;
    @OneToOne(fetch = FetchType.LAZY)
    private TicketBook ticketBook;
    private String content;
    private Integer star;
    private Boolean visible;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oauth_member_id")
    private OauthMember oauthMember;

    public Review(OauthMember oauthMember, Performance performance, String content, Integer star, Boolean visible) {
        this.oauthMember = oauthMember;
        this.performance = performance;
        this.content = content;
        this.star = star;
        this.visible = visible;
    }

    public Review update(String content, Integer star, Boolean visible) {
        this.content = content;
        this.star = star;
        this.visible = visible;
        return this;
    }

    public Boolean isVisible() {
        return visible;
    }
}
