package muse_kopis.muse.ticketbook.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import muse_kopis.muse.actor.domain.dto.TicketBookActorDto;

@Schema
public record TicketBookRequest(
        Long performanceId,
        LocalDateTime viewDate,
        String content,
        Integer star,
        Boolean visible,
        List<String> photos,
        List<TicketBookActorDto> castMembers
) {
}
