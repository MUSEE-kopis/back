package muse_kopis.muse.performance.domain;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Getter;
import lombok.Setter;

@Getter
public class TrieNode {
    // 자식 노드 맵
    private final Map<Character, TrieNode> children = new ConcurrentHashMap<>();
    // 단어의 끝인지 여부
    @Setter
    private boolean isEndOfWord;
    // 단어의 빈도수
    private long frequency;
    // 이 노드를 거쳐가는 단어들 중 빈도수 상위 5개 캐시
    private final List<String> topSuggestions = new CopyOnWriteArrayList<>();

    public void incrementFrequency() {
        this.frequency++;
    }
}