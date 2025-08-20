package muse_kopis.muse.performance.shared.normalizer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PerformanceNameNormalizer {

    public static String normalizeTitle(String title) {
        if (title == null || title.isBlank()) {
            return "";
        }
        String normalized = title.split("-")[0];
        normalized = normalized.replaceAll("[<>]|[\\[\\]]", "");
        normalized = normalized.replaceAll("NOL 스페셜 스테이지|뮤지컬|연극|오리지널|내한|공연", "");
        normalized = normalized.replaceAll("[^a-zA-Z0-9가-힣]", "").trim();
        return normalized;
    }
}
