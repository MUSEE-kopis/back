package muse_kopis.muse.performance.shared.normalizer;

public class PerformanceNameNormalizer {

    public static String normalizeTitle(String title) {
        String normalized = title.replaceAll("뮤지컬|연극|오리지널|내한|공연", "");
        normalized = normalized.replaceAll("[^a-z0-9가-힣]", "").trim();
        return normalized;
    }
}
