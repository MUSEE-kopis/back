package muse_kopis.muse.ticketbook.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;

import muse_kopis.muse.genre.domain.Genre;
import muse_kopis.muse.genre.domain.GenreType;
import muse_kopis.muse.review.domain.dto.ReviewResponse;
import muse_kopis.muse.ticketbook.domain.TicketBook;
import muse_kopis.muse.photo.domain.Photo;
import muse_kopis.muse.photo.domain.dto.PhotoResponse;

@Builder
public record TicketBookResponse(
        Long id,
        @JsonFormat(pattern = "yyyy-MM-dd a hh:mm", locale = "ko_KR")
        LocalDateTime viewDate,
        String venue,
        List<PhotoResponse> photos,
        ReviewResponse reviewResponse,
        String performanceName,
        String poster,
        Long performanceId,
        List<GenreType> genres
) {
    public static TicketBookResponse from(TicketBook ticketBook, List<Photo> photos, List<Genre> genres) {
        return TicketBookResponse.builder()
                .id(ticketBook.getId())
                .viewDate(ticketBook.getViewDate())
                .venue(ticketBook.getVenue())
                .photos(photos.stream()
                        .map(photo -> new PhotoResponse(photo.getUrl()))
                        .collect(Collectors.toList()))
                .reviewResponse(ReviewResponse.from(ticketBook.getReview(), ticketBook.getActors()))
                .performanceName(ticketBook.getReview().getPerformance().getPerformanceName())
                .poster(ticketBook.getReview().getPerformance().getPoster())
                .performanceId(ticketBook.getReview().getPerformance().getId())
                .genres(genres.stream()
                        .map(Genre::getGenre).toList())
                .build();
    }
}
