package muse_kopis.muse.actor.domain.dto;

import lombok.Builder;
import muse_kopis.muse.actor.domain.Actor;
import muse_kopis.muse.actor.domain.TicketBookActor;

@Builder
public record TicketBookActorDto(
        String actorId,
        String name,
        String url
) {
    public static TicketBookActorDto from(Actor actor) {
        return TicketBookActorDto.builder()
                .actorId(actor.getActorId())
                .name(actor.getName())
                .url(actor.getUrl())
                .build();
    }

    public static TicketBookActorDto from(TicketBookActor ticketBookActor) {
        Actor actor = ticketBookActor.getActor();
        return TicketBookActorDto.builder()
                .actorId(actor.getActorId())
                .name(actor.getName())
                .url(actor.getUrl())
                .build();
    }
}
