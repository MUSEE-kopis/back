package muse_kopis.muse.actor.domain.dto;

import java.util.List;
import lombok.Builder;
import muse_kopis.muse.actor.domain.Actor;

@Builder
public record ActorWithPerformanceDto(
        String name,
        String actorId,
        String url,
        List<String> performanceName
) {
    public static ActorWithPerformanceDto from(Actor actor, List<String> performanceName) {
        return ActorWithPerformanceDto.builder()
                .name(actor.getName())
                .actorId(actor.getActorId())
                .url(actor.getUrl())
                .performanceName(performanceName)
                .build();
    }
}
