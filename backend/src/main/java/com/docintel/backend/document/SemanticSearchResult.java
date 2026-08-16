package com.docintel.backend.document;

import java.util.UUID;

public record SemanticSearchResult(
        UUID chunkId,
        UUID documentId,
        String filename,
        int chunkIndex,
        Integer pageNumber,
        String content,
        double score
) {
}
