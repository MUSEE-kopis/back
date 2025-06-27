package muse_kopis.muse.ticketbook.presentation;

import io.swagger.v3.oas.annotations.Operation;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muse_kopis.muse.auth.Auth;
import muse_kopis.muse.ticketbook.application.TicketBookService;
import muse_kopis.muse.ticketbook.infra.config.FrontURLConfig;
import muse_kopis.muse.ticketbook.domain.dto.MonthDto;
import muse_kopis.muse.ticketbook.domain.dto.ShareablePage;
import muse_kopis.muse.ticketbook.domain.dto.TicketBookCalender;
import muse_kopis.muse.ticketbook.domain.dto.TicketBookEditRequest;
import muse_kopis.muse.ticketbook.domain.dto.TicketBookRequest;
import muse_kopis.muse.ticketbook.domain.dto.TicketBookResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ticketBooks")
public class TicketBookController {

    private final FrontURLConfig frontURLConfig;
    private final TicketBookService ticketBookService;

    /**
     * @apiNote Shared TicketBook
     * @param String identifier
     * @return ShareablePage
     */
    @Operation(summary = "티켓북 공유 페이지 조회",
            description = "공유 링크가 생성된 티켓북을 조회합니다.")
    @GetMapping("/share/{identifier}")
    public ResponseEntity<ShareablePage> getSharedTicketBooks(@PathVariable String identifier) {
        return ResponseEntity.ok().body(ticketBookService.findByIdentifier(identifier));
    }

    /**
     * @apiNote Shared TicketBook Detail
     * @param String identifier
     * @return TicketBookResponse
     */
    @Operation(summary = "티켓북 공유 페이지 상세 조회",
            description = "url을 가지고 공유한 티켓북의 상세 내용을 조회합니다.")
    @GetMapping("/share/detail/{identifier}")
    public ResponseEntity<TicketBookResponse> getSharedTicketBook(@PathVariable String identifier) {
        return ResponseEntity.ok().body(ticketBookService.getSharedTicketBook(identifier));
    }

    /**
     * @apiNote TicketBook Share
     * @param Long memberId (JWT Token)
     * @return String
     */
    @Operation(summary = "티켓북 공유",
            description = "memberId를 이용하여 공유할 티켓북 페이지 url을 만듭니다.")
    @PostMapping("/share")
    public ResponseEntity<String> generateSharedTicketBookLink(@Auth Long memberId) {
        return ResponseEntity.ok().body(frontURLConfig.url()+"ticketBooks/share/"+ticketBookService.generateLink(memberId));
    }

    /**
     * @apiNote TicketBook Details
     * @param Long memberId (JWT Token)
     * @return List<TicketBookResponse>
     */
    @Operation(summary = "티켓북 조회",
            description = "사용자가 저장한 티켓북들을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<TicketBookResponse>> ticketBooks(@Auth Long memberId) {
        return ResponseEntity.ok().body(ticketBookService.getTicketBooks(memberId));
    }

    /**
     * @apiNote TicketBook Detail
     * @param Long ticketBookId
     * @return TicketBookResponse
     */
    @Operation(summary = "티켓북 상세 조회",
            description = "사용자가 저장한 티켓북을 상세 조회합니다.")
    @GetMapping("/{ticketBookId}")
    public ResponseEntity<TicketBookResponse> ticketBook(@PathVariable Long ticketBookId) {
        return ResponseEntity.ok().body(ticketBookService.getTicketBook(ticketBookId));
    }

    /**
     * @apiNote Photo Calendar In Date
     * @param Long memberId (JWT Token)
     * @param LocalDate localDate
     * @return TicketBookResponse
     */
    @Operation(summary = "포토캘린더 특정 날짜 조회",
            description = "해당일에 기록해둔 티켓북들을 모두 조회합니다.")
    @GetMapping("/date")
    public ResponseEntity<List<TicketBookResponse>> ticketBookInDate(@Auth Long memberId, @RequestParam LocalDate localDate) {
        return ResponseEntity.ok().body(ticketBookService.ticketBookInDate(memberId, localDate));
    }

    /**
     * @apiNote Photo Calendar In Month
     * @param Long memberId (JWT Token)
     * @param MonthDto monthDto
     * @return Map<LocalDate, List<TicketBookCalender>>
     */
    @Operation(summary = "포토캘린더 조회",
            description = "등록한 티켓북을 달별로 조회합니다.")
    @GetMapping("/month")
    public ResponseEntity<Map<LocalDate, List<TicketBookCalender>>> ticketBooksByMonth(@Auth Long memberId, @ModelAttribute MonthDto monthDto) {
        return ResponseEntity.ok().body(ticketBookService.ticketBooksForMonth(memberId, monthDto.year(), monthDto.month()));
    }

    /**
     * @apiNote Write TicketBook
     * @param Long memberId (JWT Token)
     * @param TicketBookRequest ticketBookRequest
     * @return Long
     */
    @Operation(summary = "티켓북 등록",
            description = "photos는 url을 보내면 됩니다.(List<String>타입으로 되어있습니다.)")
    @PostMapping
    public ResponseEntity<Long> writeTicketBooks(@Auth Long memberId, @RequestBody TicketBookRequest ticketBookRequest) {
        return ResponseEntity.ok()
                .body(
                        ticketBookService.writeTicketBook(
                                memberId,
                                ticketBookRequest.performanceId(),
                                ticketBookRequest.viewDate(),
                                ticketBookRequest.star(),
                                ticketBookRequest.content(),
                                ticketBookRequest.visible(),
                                ticketBookRequest.photos(),
                                ticketBookRequest.castMembers()
                        )
                );
    }

    /**
     * @apiNote Delete TicketBook
     * @param Long memberId (JWT Token)
     * @param Long ticketBookId
     * @return Long
     */
    @Operation(summary = "티켓북 삭제")
    @DeleteMapping("/{ticketBookId}")
    public ResponseEntity<Long> deleteTicketBooks(@Auth Long memberId, @PathVariable Long ticketBookId) {
        return ResponseEntity.ok().body(ticketBookService.deleteTicketBook(memberId, ticketBookId));
    }

    /**
     * @apiNote Write TicketBook
     * @param Long memberId (JWT Token)
     * @param Long ticketBookId
     * @param TicketBookEditRequest ticketBookEditRequest
     * @return Long
     */
    @Operation(summary = "티켓북 수정",
            description = "photos는 url을 보내면 됩니다.(List<String>타입으로 되어있습니다.)")
    @PatchMapping("/{ticketBookId}")
    public ResponseEntity<Long> updateTicketBook(@Auth Long memberId, @PathVariable Long ticketBookId, @RequestBody TicketBookEditRequest ticketBookEditRequest) {
        return ResponseEntity.ok()
                .body(
                        ticketBookService.updateTicketBook(
                                memberId,
                                ticketBookId,
                                ticketBookEditRequest.viewDate(),
                                ticketBookEditRequest.photos(),
                                ticketBookEditRequest.star(),
                                ticketBookEditRequest.content(),
                                ticketBookEditRequest.visible(),
                                ticketBookEditRequest.castMembers()
                        )
                );
    }
}
