package muse_kopis.muse.ticketbook.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muse_kopis.muse.actor.domain.TicketBookActor;
import muse_kopis.muse.auth.oauth.domain.OauthMember;
import muse_kopis.muse.common.auth.UnAuthorizationException;
import muse_kopis.muse.performance.domain.Performance;
import muse_kopis.muse.review.domain.Review;
import muse_kopis.muse.review.domain.dto.ReviewResponse;
import muse_kopis.muse.photo.domain.Photo;

@Slf4j
@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime viewDate;
    private String venue;
    @OneToMany(mappedBy = "ticketBook")
    private List<TicketBookActor> actors;
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "review_id")
    private Review review;
    @ManyToOne(fetch = FetchType.LAZY)
    private OauthMember oauthMember;
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "ticketBook")
    private List<Photo> photos;
    private String identifier;

    public static TicketBook from(
            OauthMember oauthMember,
            LocalDateTime viewDate,
            ReviewResponse review,
            Performance performance,
            List<TicketBookActor> castMembers
    ) {
        TicketBook tempTicketBook = TicketBook.builder()
                .oauthMember(oauthMember)
                .viewDate(viewDate)
                .venue(performance.getVenue())
                .actors(castMembers)
                .build();

        tempTicketBook.review = Review.builder()
                .star(review.star())
                .content(review.content())
                .visible(review.visible())
                .performance(performance)
                .oauthMember(oauthMember)
                .ticketBook(tempTicketBook)
                .build();
        // 다시 TicketBook에 Review 세팅
        return tempTicketBook;
    }

    public void update(LocalDateTime viewDate, ReviewResponse request, List<TicketBookActor> castMembers) {
        this.viewDate = viewDate;
        this.review = review.update(request.content(), request.star(), request.visible());
        this.actors = castMembers;
    }

    public void validate(OauthMember oauthMember) {
        if (!this.oauthMember.equals(oauthMember)){
            throw new UnAuthorizationException("티켓북 삭제 권한이 없습니다.");
        }
    }

    public void share(String shareableLink) {
        this.identifier = shareableLink;
    }

    public Boolean shareValidate() {
        return this.identifier == null || this.identifier.isEmpty();
    }
}
