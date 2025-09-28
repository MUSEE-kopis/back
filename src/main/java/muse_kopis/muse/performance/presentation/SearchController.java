package muse_kopis.muse.performance.presentation;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import muse_kopis.muse.performance.application.SearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @PostMapping("/search")
    @Operation(summary = "검색어 추가", description = "실시간으로 사용자가 입력한 검색어를 추가합니다.")
    public void addSearchTerm(@RequestBody Map<String, String> payload) {
        String term = payload.get("term");
        if (term != null && !term.isBlank()) {
            searchService.addSearchTerm(term.toLowerCase()); // 소문자로 통일하여 저장
        }
    }

    @GetMapping("/suggestions")
    @Operation(summary = "실시간 검색어", description = "실시간 검색어에 따른 연관 검색어를 보여줍니다.")
    public List<String> getSuggestions(@RequestParam String prefix) {
        return searchService.getSuggestions(prefix.toLowerCase());
    }
}