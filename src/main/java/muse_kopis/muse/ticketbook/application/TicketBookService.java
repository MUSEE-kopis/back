package muse_kopis.muse.ticketbook.application;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muse_kopis.muse.actor.domain.Actor;
import muse_kopis.muse.actor.domain.ActorRepository;
import muse_kopis.muse.actor.domain.TicketBookActor;
import muse_kopis.muse.actor.domain.TicketBookActorRepository;
import muse_kopis.muse.actor.domain.dto.TicketBookActorDto;
import muse_kopis.muse.auth.oauth.domain.OauthMember;
import muse_kopis.muse.auth.oauth.domain.OauthMemberRepository;
import muse_kopis.muse.auth.oauth.domain.TierImageURL;
import muse_kopis.muse.auth.oauth.domain.UserTier;
import muse_kopis.muse.common.ticketbook.InvalidLocalDateException;
import muse_kopis.muse.common.ticketbook.NotFoundTicketBookException;
import muse_kopis.muse.genre.domain.Genre;
import muse_kopis.muse.genre.domain.GenreRepository;
import muse_kopis.muse.performance.domain.Performance;
import muse_kopis.muse.performance.domain.PerformanceRepository;
import muse_kopis.muse.photo.application.PhotoService;
import muse_kopis.muse.review.domain.ReviewRepository;
import muse_kopis.muse.review.domain.dto.ReviewResponse;
import muse_kopis.muse.ticketbook.domain.TicketBook;
import muse_kopis.muse.ticketbook.domain.TicketBookRepository;
import muse_kopis.muse.ticketbook.domain.dto.ShareablePage;
import muse_kopis.muse.ticketbook.domain.dto.TicketBookCalender;
import muse_kopis.muse.ticketbook.domain.dto.TicketBookResponse;
import muse_kopis.muse.photo.domain.Photo;
import muse_kopis.muse.photo.domain.PhotoRepository;
import muse_kopis.muse.ticketbook.domain.dto.UserGenreEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketBookService {

    private final TierImageURL tierImageURL;
    private final OauthMemberRepository oauthMemberRepository;
    private final TicketBookRepository ticketBookRepository;
    private final PerformanceRepository performanceRepository;
    private final PhotoRepository photoRepository;
    private final PhotoService photoService;
    private final ReviewRepository reviewRepository;
    private final GenreRepository genreRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ActorRepository actorRepository;
    private final TicketBookActorRepository ticketBookActorRepository;

    @Transactional
    public List<TicketBookResponse> getTicketBooks(Long memberId) {
        OauthMember oauthMember = oauthMemberRepository.getByOauthMemberId(memberId);
        return ticketBookRepository.findAllByOauthMember(oauthMember)
                .stream()
                .map(this::toTicketBookResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TicketBookResponse getTicketBook(Long ticketBookId) {
        TicketBook ticketBook = ticketBookRepository.getByTicketBookId(ticketBookId);
        return toTicketBookResponse(ticketBook);
    }

    @Transactional
    public Long writeTicketBook(
            Long memberId,
            Long performanceId,
            LocalDateTime viewDate,
            Integer star,
            String content,
            Boolean visible,
            List<String> photoURLs,
            List<TicketBookActorDto> castMembers
    ) {
        OauthMember oauthMember = oauthMemberRepository.getByOauthMemberId(memberId);
        Performance performance = performanceRepository.getByPerformanceId(performanceId);

        ReviewResponse review = ReviewResponse.from(oauthMember, star, content, visible, castMembers);
        List<TicketBookActor> actors = castMembers.stream()
                .map(dto -> {
                    Actor actor = actorRepository.findByActorId(dto.actorId());
                    if (actor == null) {
                        actor = actorRepository.findByName(dto.name())
                                .orElseGet(() -> actorRepository.save(
                                        Actor.builder()
                                                .name(dto.name())
                                                .actorId(dto.actorId())
                                                .url(dto.url())
                                                .build()));
                    }
                    return TicketBookActor.builder()
                            .actor(actor)
                            .build();
                })
                .toList();
        TicketBook ticketBook = TicketBook.from(oauthMember, viewDate, review, performance, actors);
        actors.forEach(actor -> actor.ticketBook(ticketBook));
        ticketBookRepository.save(ticketBook);
        ticketBookActorRepository.saveAll(actors);

        List<Photo> photos = validPhotos(photoURLs, ticketBook);
        if (!photos.isEmpty()) {
            photoRepository.saveAll(photos);
        }

        eventPublisher.publishEvent(new UserGenreEvent(memberId, performanceId));
        tierUpdate(oauthMember);
        return ticketBook.getId();
    }

    private List<Photo> validPhotos(List<String> urls, TicketBook ticketBook) {
        if (urls == null) {
            urls = new ArrayList<>();
        }
        urls = urls.stream().filter(url -> !url.isBlank()).toList();
        return urls.stream().map(url -> new Photo(url, ticketBook)).toList();
    }

    private void tierUpdate(OauthMember oauthMember) {
        long counted = ticketBookRepository.countTicketBookByOauthMember(oauthMember);
        UserTier tier = UserTier.fromCount(counted);
        switch (tier) {
            case MANIA -> oauthMember.updateUserTier(tier, tierImageURL.getMania());
            case LOVER -> oauthMember.updateUserTier(tier, tierImageURL.getLover());
            case NEWBIE -> oauthMember.updateUserTier(tier, tierImageURL.getNewbie());
        }
        oauthMemberRepository.save(oauthMember);
    }

    @Transactional
    public Long deleteTicketBook(Long memberId, Long ticketBookId) {
        OauthMember oauthMember = oauthMemberRepository.getByOauthMemberId(memberId);
        TicketBook ticketBook = ticketBookRepository.getByTicketBookId(ticketBookId);
        ticketBook.validate(oauthMember);
        photoService.deleteImages(ticketBook);
        photoRepository.deleteAll(photoRepository.findAllByTicketBook(ticketBook));
        reviewRepository.delete(ticketBook.getReview());
        ticketBookRepository.delete(ticketBook);
        tierUpdate(oauthMember);
        return ticketBookId;
    }

    @Transactional
    public List<TicketBookResponse> ticketBookInDate(Long memberId, LocalDate localDate) {
        OauthMember oauthMember = oauthMemberRepository.getByOauthMemberId(memberId);
        LocalDateTime startDateTime = localDate.atStartOfDay();
        LocalDateTime endDateTime = localDate.plusDays(1).atStartOfDay();
        List<TicketBook> ticketBooks = ticketBookRepository.findByOauthMemberAndViewDate(memberId, startDateTime, endDateTime);
        return ticketBooks.stream().map(this::toTicketBookResponse).toList();
    }

    @Transactional
    public Map<LocalDate, List<TicketBookCalender>> ticketBooksForMonth(Long memberId, Integer year, Integer month) {
        if (year == null || month == null) {
           throw new InvalidLocalDateException("년도 또는 달이 입력되지 않았습니다.");
        }
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startDate = yearMonth.atDay(1).atTime(0,0,0);
        LocalDateTime endDate = yearMonth.atEndOfMonth().plusDays(1).atStartOfDay();
        log.info("start {}", startDate);
        log.info("end {}", endDate);
        List<TicketBook> ticketBooks = ticketBookRepository.findByOauthMemberAndViewDate(memberId, startDate, endDate);
        Map<LocalDate, List<TicketBookCalender>> map = new HashMap<>();
        ticketBooks.forEach(ticketBook -> map
                .computeIfAbsent(ticketBook.getViewDate().toLocalDate(), k -> new ArrayList<>())
                        .add(TicketBookCalender.from(ticketBook)));
        return map;
    }

    @Transactional
    public Long updateTicketBook(
            Long memberId,
            Long ticketBookId,
            LocalDateTime viewDate,
            List<String> photoURLs,
            Integer star,
            String content,
            Boolean visible,
            List<TicketBookActorDto> castMembers
    ) {
        TicketBook ticketBook = ticketBookRepository.getByTicketBookId(ticketBookId);
        OauthMember oauthMember = oauthMemberRepository.getByOauthMemberId(memberId);
        ticketBook.validate(oauthMember);
        ReviewResponse review = ReviewResponse.from(oauthMember, star, content, visible, castMembers);
        List<TicketBookActor> actors = castMembers.stream()
                .map(dto -> {
                    Actor actor = actorRepository.findByActorId(dto.actorId());
                    if (actor == null) {
                        actor = actorRepository.findByName(dto.name())
                                .orElseGet(() -> actorRepository.save(
                                        Actor.builder()
                                                .name(dto.name())
                                                .actorId(dto.actorId())
                                                .url(dto.url())
                                                .build()));
                    }
                    return TicketBookActor.builder()
                            .actor(actor)
                            .build(); // ticketBook은 아직 없음 → 아래에서 세팅
                }).collect(Collectors.toList());
        ticketBook.update(viewDate, review, actors);
        photoService.updateImage(ticketBook, photoURLs);
        return ticketBookRepository.save(ticketBook).getId();
    }

    @Transactional
    public ShareablePage findByIdentifier(String identifier) {
        List<TicketBookResponse> ticketBooks = ticketBookRepository.findAllByIdentifier(identifier).stream()
                .map(this::toTicketBookResponse)
                .toList();
        if (ticketBooks.isEmpty()) {
            throw new NotFoundTicketBookException("티켓북을 찾을 수 없습니다.");
        }
        String username = ticketBooks.getFirst().reviewResponse().username();
        return new ShareablePage(ticketBooks, username);
    }

    @Transactional
    public String generateLink(Long memberId) {
        OauthMember oauthMember = oauthMemberRepository.getByOauthMemberId(memberId);
        List<TicketBook> ticketBooks = ticketBookRepository.findAllByOauthMember(oauthMember);
        String link = ticketBooks.getFirst().shareValidate()? UUID.randomUUID().toString() : ticketBooks.getFirst().getIdentifier();
        log.info(link);
        ticketBooks.forEach(ticketBook -> {
            if(ticketBook.getOauthMember().id().equals(oauthMember.id()) && ticketBook.shareValidate()) {
                ticketBook.share(link);
            }
        });
        ticketBookRepository.saveAll(ticketBooks);
        return link;
    }

    @Transactional
    public TicketBookResponse getSharedTicketBook(String identifier) {
        TicketBook ticketBook = ticketBookRepository.getByIdentifier(identifier);
        return toTicketBookResponse(ticketBook);
    }

    private TicketBookResponse toTicketBookResponse(TicketBook ticketBook) {
        List<Photo> photos = photoService.getImagesByTicketBook(ticketBook);
        List<Genre> genres = genreRepository.findAllByPerformanceAndOauthMember(ticketBook.getReview().getPerformance(),
                ticketBook.getOauthMember());
        return TicketBookResponse.from(ticketBook, photos, genres);
    }
}
