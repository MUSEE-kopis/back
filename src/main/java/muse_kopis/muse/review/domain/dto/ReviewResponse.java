package muse_kopis.muse.review.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import muse_kopis.muse.actor.domain.TicketBookActor;
import muse_kopis.muse.actor.domain.dto.TicketBookActorDto;
import muse_kopis.muse.auth.oauth.domain.OauthMember;
import muse_kopis.muse.review.domain.Review;

@Schema
@Builder
public record ReviewResponse(
        String username,
        String content,
        Integer star,
        List<TicketBookActorDto> castMembers,
        Boolean visible
) {
    public static ReviewResponse from(Review review, List<TicketBookActor> castMembers) {
        return ReviewResponse.builder()
                .username(review.getOauthMember().username())
                .content(review.getContent())
                .star(review.getStar())
                .visible(review.getVisible())
                .castMembers(castMembers.stream().map(TicketBookActorDto::from).toList())
                .build();
    }

    public static ReviewResponse from(OauthMember oauthMember, Integer star, String content, Boolean visible, List<TicketBookActorDto> castMembers) {
        return ReviewResponse.builder()
                .star(star)
                .content(content)
                .visible(visible)
                .username(oauthMember.username())
                .castMembers(castMembers)
                .build();
    }

    public static ReviewResponse from(Review review) {
        return ReviewResponse.builder()
                .username(review.getOauthMember().username())
                .content(review.getContent())
                .star(review.getStar())
                .visible(review.getVisible())
                .castMembers(review.getTicketBook().getActors().stream().map(TicketBookActorDto::from).toList())
                .build();
    }
}
