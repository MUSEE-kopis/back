package muse_kopis.muse.performance.application;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.RequiredArgsConstructor;
import muse_kopis.muse.performance.domain.TrieNode;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final TrieNode root = new TrieNode();
    private final RedisTemplate<String, String> redisTemplate;
    private static final String REDIS_ZSET_KEY = "searchTerms";
    private static final int MAX_SUGGESTIONS = 5;

    // 읽기는 동시에 여러 스레드가, 쓰기는 한 번에 하나의 스레드만 접근하도록 제어
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * 애플리케이션 시작 시 Redis의 데이터를 Trie에 로드
     */
    @PostConstruct
    public void init() {
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        Set<TypedTuple<String>> searchTerms = zSetOperations.rangeWithScores(REDIS_ZSET_KEY, 0, -1);
        if (searchTerms != null) {
            searchTerms.forEach(term -> {
                double score = term.getScore() != null ? term.getScore() : 0;
                insert(Objects.requireNonNull(term.getValue()), (long) score);
            });
        }
    }

    /**
     * 검색어 추가 (사용자 입력)
     */
    public void addSearchTerm(String term) {
        // 1. Redis Sorted Set에 빈도수 1 증가 (데이터 영속화)
        // ZINCRBY 명령어와 동일
        redisTemplate.opsForZSet().incrementScore(REDIS_ZSET_KEY, term, 1);
        long newFrequency = Objects.requireNonNull(redisTemplate.opsForZSet().score(REDIS_ZSET_KEY, term)).longValue();

        // 2. In-Memory Trie에 단어 추가 및 캐시 업데이트
        insert(term, newFrequency);
    }

    /**
     * 검색어 추천 (자동 완성)
     */
    public List<String> getSuggestions(String prefix) {
        lock.readLock().lock(); // 읽기 락 시작
        try {
            TrieNode currentNode = root;
            for (char c : prefix.toCharArray()) {
                currentNode = currentNode.getChildren().get(c);
                if (currentNode == null) {
                    return List.of(); // 접두사에 해당하는 단어가 없음
                }
            }
            return currentNode.getTopSuggestions();
        } finally {
            lock.readLock().unlock(); // 읽기 락 해제
        }
    }

    /**
     * Trie에 단어 삽입 및 캐시 업데이트 (내부 로직)
     */
    private void insert(String term, long frequency) {
        lock.writeLock().lock(); // 쓰기 락 시작
        try {
            TrieNode currentNode = root;
            for (char c : term.toCharArray()) {
                // 경로에 있는 모든 노드의 추천 캐시를 업데이트
                updateTopSuggestions(currentNode, term);
                currentNode = currentNode.getChildren().computeIfAbsent(c, k -> new TrieNode());
            }
            currentNode.setEndOfWord(true);
            currentNode.incrementFrequency();
        } finally {
            lock.writeLock().unlock(); // 쓰기 락 해제
        }
    }

    /**
     * 노드의 상위 5개 추천 검색어 캐시를 업데이트하는 로직
     */
    private void updateTopSuggestions(TrieNode node, String term) {
        List<String> suggestions = node.getTopSuggestions();
        if (!suggestions.contains(term)) {
            suggestions.add(term);
        }

        // 빈도수(Redis score)를 기준으로 내림차순 정렬
        suggestions.sort((s1, s2) -> {
            Double score1 = redisTemplate.opsForZSet().score(REDIS_ZSET_KEY, s1);
            Double score2 = redisTemplate.opsForZSet().score(REDIS_ZSET_KEY, s2);
            score1 = score1 == null ? 0 : score1;
            score2 = score2 == null ? 0 : score2;
            return score2.compareTo(score1);
        });

        // 5개가 넘어가면 가장 낮은 빈도수의 검색어를 제거
        if (suggestions.size() > MAX_SUGGESTIONS) {
            node.getTopSuggestions().remove(MAX_SUGGESTIONS);
        }
    }
}