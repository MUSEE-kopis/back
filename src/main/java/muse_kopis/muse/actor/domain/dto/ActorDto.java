package muse_kopis.muse.actor.domain.dto;

import lombok.Builder;
import muse_kopis.muse.actor.domain.Actor;
import muse_kopis.muse.actor.domain.FavoriteActor;

@Builder
public record ActorDto(
        String name,
        String actorId,
        String url
) {
    public static ActorDto from(FavoriteActor actor) {
        return ActorDto.builder()
                .name(actor.getActor().getName())
                .actorId(actor.getActor().getActorId())
                .url(actor.getActor().getUrl())
                .build();
    }

    public static ActorDto from(Actor actor) {
        return ActorDto.builder()
                .name(actor.getName())
                .actorId(actor.getActorId())
                .url(actor.getUrl())
                .build();
    }
}
