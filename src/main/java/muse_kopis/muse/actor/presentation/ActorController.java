package muse_kopis.muse.actor.presentation;

import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

import lombok.RequiredArgsConstructor;
import muse_kopis.muse.actor.application.ActorService;
import muse_kopis.muse.actor.domain.dto.ActorDto;
import muse_kopis.muse.auth.Auth;
import muse_kopis.muse.actor.domain.dto.CastMemberDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/actors")
@RequiredArgsConstructor
public class ActorController {

    private final ActorService actorService;

    /**
     * @apiNote Favorite Actor
     * @param Long memberId (JWT Token)
     * @param ActorDto actor
     * @return Long
     */
    @PostMapping
    @Operation(summary = "관심 배우 등록", description = "관심 배우를 등록합니다.")
    public ResponseEntity<Long> favorite(@Auth Long memberId, @RequestBody CastMemberDto actor) {
        return ResponseEntity.ok().body(actorService.favorite(memberId, actor.name(), actor.actorId(), actor.url()));
    }

    /**
     * @apiNote Favorites Actors
     * @param Long memberId (JWT Token)
     * @return List<ActorDto>
     */
    @GetMapping
    @Operation(summary = "관심 배우 조회", description = "관심 배우를 조회합니다.")
    public ResponseEntity<List<ActorDto>> favorites(@Auth Long memberId) {
        return ResponseEntity.ok().body(actorService.favorites(memberId));
    }

    /**
     * @apiNote Search Actor
     * @param actorName to find Actor
     * @return List<ActorDto>
     */
    @GetMapping("/search")
    @Operation(summary = "배우 검색", description = "검색어를 통해 배우를 조회합니다.")
    public ResponseEntity<List<ActorDto>> findActors(@RequestParam String actorName) {
        return ResponseEntity.ok().body(actorService.findActors(actorName));
    }

    /**
     * @apiNote Search Actor By PerformanceId
     * @param actorName to find Actor
     * @param performanceId to find Actor
     * @return List<ActorDto>
     */
    @GetMapping("/search/{performanceId}")
    @Operation(summary = "해당 공연에서 배우 검색", description = "검색어를 통해 해당 공연에 출연한 배우를 조회합니다.")
    public ResponseEntity<List<ActorDto>> findActorsByPerformanceId(@PathVariable Long performanceId, @RequestParam String actorName) {
        return ResponseEntity.ok().body(actorService.findActorsByPerformanceId(performanceId, actorName));
    }
}
