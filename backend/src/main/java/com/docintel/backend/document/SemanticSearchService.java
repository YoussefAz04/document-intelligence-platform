package com.docintel.backend.document;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SemanticSearchService {

    private static final int DEFAULT_LIMIT = 5;
    private static final int MAX_LIMIT = 20;

    private final EmbeddingClient embeddingClient;
    private final DocumentChunkEmbeddingRepository embeddingRepository;

    public SemanticSearchService(
            EmbeddingClient embeddingClient,
            DocumentChunkEmbeddingRepository embeddingRepository
    ) {
        this.embeddingClient = embeddingClient;
        this.embeddingRepository = embeddingRepository;
    }

    @Transactional(readOnly = true)
    public List<SemanticSearchResult> search(String query, Integer requestedLimit) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Search query must not be blank.");
        }

        int limit = requestedLimit == null ? DEFAULT_LIMIT : requestedLimit;
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new IllegalArgumentException("Search limit must be between 1 and 20.");
        }

        List<Double> queryEmbedding = embeddingClient.createEmbeddings(List.of(query.trim())).get(0);
        return embeddingRepository.search(queryEmbedding, limit);
    }
}
