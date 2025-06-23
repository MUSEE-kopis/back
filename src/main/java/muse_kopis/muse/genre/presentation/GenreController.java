package muse_kopis.muse.genre.presentation;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muse_kopis.muse.auth.Auth;
import muse_kopis.muse.genre.application.GenreService;
import muse_kopis.muse.genre.domain.GenreType;
import muse_kopis.muse.genre.domain.dto.PerformanceGenreInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/genre")
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    @Operation(summary = "공연 장르를 저장",
            description = "공연 장르를 저장합니다. 공연 ID와 장르타입으로 저장하고, 해당 공연의 장르를 갱신합니다. (사용자 토큰을 사용합니다)")
    @PostMapping
    public ResponseEntity<Void> setGenre(@Auth Long memberId, @RequestBody PerformanceGenreInfo performanceGenreInfo) {
        genreService.saveGenre(performanceGenreInfo.performanceId(), performanceGenreInfo.genres(), memberId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "공연 장르 수정",
            description = "공연 장르 수정을 합니다. 공연 ID를 가지고 장르를 업데이트 합니다. (사용자 토큰을 사용합니다)")
    @PatchMapping
    public ResponseEntity<Void> updateGenre(@Auth Long memberId, @RequestBody PerformanceGenreInfo performanceGenreInfo) {
        genreService.updateGenre(performanceGenreInfo.performanceId(), performanceGenreInfo.genres(), memberId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "공연 장르 조회",
            description = "공연 장르를 조회합니다. 공연 ID와 사용자 토큰을 사용합니다.")
    @GetMapping("/{performanceId}")
    public ResponseEntity<List<GenreType>> getGenres(@Auth Long memberId, @PathVariable Long performanceId) {
        return ResponseEntity.ok().body(genreService.getGenres(memberId, performanceId));
    }
}
