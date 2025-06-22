package muse_kopis.muse.actor.domain.dto;

import lombok.Builder;
import muse_kopis.muse.actor.domain.Actor;
import muse_kopis.muse.actor.domain.CastMember;
import muse_kopis.muse.performance.domain.Performance;

@Builder
public record CastMemberDto(
        String actorId,
        String name,
        String role,
        String url
) {
    public static CastMemberDto from(CastMember castMember) {
        return CastMemberDto.builder()
                .name(castMember.getActor().getName())
                .actorId(castMember.getActor().getActorId())
                .role(castMember.getRole())
                .url(castMember.getActor().getUrl())
                .build();
    }

    @Override
    public String toString() {
        return name;
    }
}
