package com.docintel.backend.document;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KeywordSearchService {

    private static final int DEFAULT_LIMIT = 5;
    private static final int MAX_LIMIT = 20;

    private final KeywordSearchRepository keywordSearchRepository;

    public KeywordSearchService(KeywordSearchRepository keywordSearchRepository) {
        this.keywordSearchRepository = keywordSearchRepository;
    }

    @Transactional(readOnly = true)
    public List<KeywordSearchResult> search(String query, Integer requestedLimit) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Search query must not be blank.");
        }

        int limit = requestedLimit == null ? DEFAULT_LIMIT : requestedLimit;
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new IllegalArgumentException("Search limit must be between 1 and 20.");
        }

        return keywordSearchRepository.search(query.trim(), limit);
    }
}
