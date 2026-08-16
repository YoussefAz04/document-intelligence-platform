package com.docintel.backend.document;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class HybridSearchService {

    private static final int DEFAULT_LIMIT = 5;
    private static final int MAX_LIMIT = 20;
    private static final int CANDIDATE_MULTIPLIER = 3;
    private static final int RRF_RANK_CONSTANT = 60;

    private final SemanticSearchService semanticSearchService;
    private final KeywordSearchService keywordSearchService;

    public HybridSearchService(
            SemanticSearchService semanticSearchService,
            KeywordSearchService keywordSearchService
    ) {
        this.semanticSearchService = semanticSearchService;
        this.keywordSearchService = keywordSearchService;
    }

    public List<HybridSearchResult> search(String query, Integer requestedLimit) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Search query must not be blank.");
        }

        int limit = requestedLimit == null ? DEFAULT_LIMIT : requestedLimit;
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new IllegalArgumentException("Search limit must be between 1 and 20.");
        }

        String cleanQuery = query.trim();
        int candidateLimit = Math.min(MAX_LIMIT, limit * CANDIDATE_MULTIPLIER);
        List<SemanticSearchResult> semanticResults = semanticSearchService.search(cleanQuery, candidateLimit);
        List<KeywordSearchResult> keywordResults = keywordSearchService.search(cleanQuery, candidateLimit);

        Map<UUID, Candidate> candidates = new LinkedHashMap<>();
        for (int index = 0; index < semanticResults.size(); index++) {
            SemanticSearchResult result = semanticResults.get(index);
            candidates.computeIfAbsent(result.chunkId(), ignored -> Candidate.from(result))
                    .addSemantic(result.score(), reciprocalRank(index + 1));
        }

        for (int index = 0; index < keywordResults.size(); index++) {
            KeywordSearchResult result = keywordResults.get(index);
            candidates.computeIfAbsent(result.chunkId(), ignored -> Candidate.from(result))
                    .addKeyword(result.score(), reciprocalRank(index + 1));
        }

        return candidates.values()
                .stream()
                .sorted(Comparator.comparingDouble(Candidate::fusedScore).reversed())
                .limit(limit)
                .map(Candidate::toResult)
                .toList();
    }

    private double reciprocalRank(int rank) {
        return 1.0 / (RRF_RANK_CONSTANT + rank);
    }

    private static final class Candidate {

        private final UUID chunkId;
        private final UUID documentId;
        private final String filename;
        private final int chunkIndex;
        private final Integer pageNumber;
        private final String content;
        private double fusedScore;
        private Double semanticScore;
        private Double keywordScore;

        private Candidate(
                UUID chunkId,
                UUID documentId,
                String filename,
                int chunkIndex,
                Integer pageNumber,
                String content
        ) {
            this.chunkId = chunkId;
            this.documentId = documentId;
            this.filename = filename;
            this.chunkIndex = chunkIndex;
            this.pageNumber = pageNumber;
            this.content = content;
        }

        private static Candidate from(SemanticSearchResult result) {
            return new Candidate(
                    result.chunkId(),
                    result.documentId(),
                    result.filename(),
                    result.chunkIndex(),
                    result.pageNumber(),
                    result.content()
            );
        }

        private static Candidate from(KeywordSearchResult result) {
            return new Candidate(
                    result.chunkId(),
                    result.documentId(),
                    result.filename(),
                    result.chunkIndex(),
                    result.pageNumber(),
                    result.content()
            );
        }

        private void addSemantic(double score, double reciprocalRankScore) {
            semanticScore = score;
            fusedScore += reciprocalRankScore;
        }

        private void addKeyword(double score, double reciprocalRankScore) {
            keywordScore = score;
            fusedScore += reciprocalRankScore;
        }

        private double fusedScore() {
            return fusedScore;
        }

        private HybridSearchResult toResult() {
            SearchMatchType matchedBy;
            if (semanticScore != null && keywordScore != null) {
                matchedBy = SearchMatchType.BOTH;
            } else if (semanticScore != null) {
                matchedBy = SearchMatchType.SEMANTIC;
            } else {
                matchedBy = SearchMatchType.KEYWORD;
            }

            return new HybridSearchResult(
                    chunkId,
                    documentId,
                    filename,
                    chunkIndex,
                    pageNumber,
                    content,
                    fusedScore,
                    semanticScore,
                    keywordScore,
                    matchedBy
            );
        }
    }
}
