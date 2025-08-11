package muse_kopis.muse.performance.presentation;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muse_kopis.muse.auth.Auth;
import muse_kopis.muse.performance.application.PerformanceService;
import muse_kopis.muse.performance.domain.dto.AdminSelect;
import muse_kopis.muse.performance.domain.dto.PerformanceRequest;
import muse_kopis.muse.performance.domain.dto.PerformanceResponse;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequestMapping("/performances")
@RequiredArgsConstructor
public class PerformanceController {

    private final PerformanceService performanceService;

    /**
     * @apiNote Search Performance
     * @param String search
     * @return List<PerformanceResponse>
     */
    @Operation(summary = "검색",
            description = "검색어를 통해 공연을 조회합니다.")
    @GetMapping("/search")
    public ResponseEntity<List<PerformanceResponse>> searchPerformance(@RequestParam(name = "keyword") String keyword) {
        return ResponseEntity.ok().body(performanceService.findAllPerformanceBySearch(keyword));
    }

    /**
     * @apiNote Performances Recommend By Admin
     * @return List<PerformanceResponse>
     */
    @Operation(summary = "관리자 지정 추천", description = "현재 진행중인 공연 중에서 관리자가 선택한 공연을 보여줍니다.")
    @GetMapping("/custom")
    public ResponseEntity<List<PerformanceResponse>> selectPerformanceByAdmin() {
        return ResponseEntity.ok().body(performanceService.findAllPerformance());
    }

    /**
     * @apiNote Performance Create By Admin
     * @param Long adminId
     * @param AdminSelect adminSelect
     * @return
     */
    @Operation(summary = "관리자 지정 추천 등록", description = "현재 진행중인 공연 중에서 관리자가 추천할 공연을 선택합니다.")
    @PostMapping("/custom")
    public ResponseEntity<Void> createPerformanceByAdmin(@Auth Long adminId, @RequestBody AdminSelect adminSelect) {
        performanceService.selectPerformanceByAdmin(adminId, adminSelect);
        return ResponseEntity.ok().build();
    }

    /**
     * @apiNote Performance Delete By Admin
     * @param Long adminId
     * @param AdminSelect adminSelect
     * @return
     */
    @DeleteMapping("/custom")
    public ResponseEntity<Void> deletePerformanceByAdmin(@Auth Long adminId, @RequestBody AdminSelect adminSelect) {
        performanceService.deletePerformanceByAdmin(adminId, adminSelect);
        return ResponseEntity.ok().build();
    }

    /**
     * @apiNote Popular Performances
     * @return List<PerformanceResponse>
     */
    @Operation(summary = "인기 작품 6편",
            description = "최근 예매율이 높은 6편의 공연을 보여줍니다.")
    @GetMapping("/popular")
    public ResponseEntity<List<PerformanceResponse>> getPerformancePopular() {
        return ResponseEntity.ok().body(performanceService.fetchPopularPerformance());
    }

    /**
     * @apiNote Performance Detail
     * @param Long performanceId
     * @return PerformanceResponse
     */
    @Operation(summary = "공연 상세",
            description = "특정 공연의 정보를 보여줍니다.")
    @GetMapping("/{performanceId}")
    public ResponseEntity<PerformanceResponse> getPerformance(@PathVariable Long performanceId) {
        return ResponseEntity.ok().body(performanceService.findById(performanceId));
    }

    /**
     * @apiNote Recommend Performances
     * @param Long memberId (JWT Token)
     * @return List<PerformanceResponse>
     */
    @GetMapping("/recommend")
    @Operation(summary = "사용자 맞춤 추천",
            description = "사용자 맞춤 공연을 추천합니다.")
    public ResponseEntity<List<PerformanceResponse>> recommendPerformance(@Auth Long memberId){
        return ResponseEntity.ok().body(performanceService.recommendPerformance(memberId));
    }

    /**
     * @apiNote Random Performances
     * @param Long memberId (JWT Token)
     * @return Set<PerformanceResponse>
     */
    @Operation(summary = "무작위 추천",
            description = "무작위로 현재 진행중인 공연을 추천합니다.")
    @GetMapping("/random")
    public ResponseEntity<Set<PerformanceResponse>> randomPerformance(@Auth Long memberId) {
        return ResponseEntity.ok().body(performanceService.getRandomPerformance(memberId));
    }

    /**
     * @apiNote Performance Poster
     * @param Long performanceId
     * @return ByteArrayResource
     */
    @Operation(summary = "공연 포스터 조회",
            description = "해당 공연의 포스터 이미지를 바이트배열로 제공합니다.")
    @GetMapping("/poster/{performanceId}")
    public ResponseEntity<ByteArrayResource> getPosterImage(@PathVariable Long performanceId) {
        ByteArrayResource image = performanceService.getPosterImage(performanceId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("image/gif"));
        return ResponseEntity.ok().headers(headers).body(image);
    }

//    @PostConstruct
    public void init() {
        log.info("Initialization started");
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        for (int i = 1; i < 142; i++) {
            final int currentPage = i;
            log.info("Processing page: " + currentPage);
            if (i < 3) {
                executorService.submit(() -> {
                    try {
                        performanceService.fetchPerformances("20180101", "20241231",
                                String.valueOf(currentPage), "100", "02", "GGGA");
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            if (i < 5) {
                executorService.submit(() -> {
                    try {
                        performanceService.fetchPerformances("20180101", "20241231",
                                String.valueOf(currentPage), "100", "01", "GGGA");
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
//            executorService.submit(() -> {
//                try {
//                    performanceService.fetchPerformances("20180101", "20241231",
//                            String.valueOf(currentPage), "100", "03", "GGGA");
//                } catch (JsonProcessingException e) {
//                    throw new RuntimeException(e);
//                }
//            });
        }
        log.info("Initialization ended");
    }

    // KOPIS 전체 공연 목록 저장
//    @GetMapping("/kopis")
    public ResponseEntity<Void> getPerformances(@ModelAttribute PerformanceRequest performanceRequest)
            throws JsonProcessingException {
        performanceService.fetchPerformances(performanceRequest.startDate(), performanceRequest.endDate(), performanceRequest.currentPage(),
                performanceRequest.rows(), performanceRequest.state(), performanceRequest.genre());
        return ResponseEntity.ok().build();
    }
}
