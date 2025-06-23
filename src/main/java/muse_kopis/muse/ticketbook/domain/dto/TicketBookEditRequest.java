package muse_kopis.muse.ticketbook.domain.dto;

import java.time.LocalDateTime;
import java.util.List;
import muse_kopis.muse.actor.domain.dto.TicketBookActorDto;

public record TicketBookEditRequest(
        LocalDateTime viewDate,
        String content,
        Integer star,
        Boolean visible,
        List<TicketBookActorDto> castMembers,
        List<String> photos
) {
}
