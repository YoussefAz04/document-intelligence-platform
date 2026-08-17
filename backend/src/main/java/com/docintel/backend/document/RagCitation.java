package com.docintel.backend.document;

import java.util.UUID;

public record RagCitation(
        String sourceId,
        UUID chunkId,
        UUID documentId,
        String filename,
        int chunkIndex,
        Integer pageNumber,
        String content
) {
    public static RagCitation from(String sourceId, HybridSearchResult result) {
        return new RagCitation(
                sourceId,
                result.chunkId(),
                result.documentId(),
                result.filename(),
                result.chunkIndex(),
                result.pageNumber(),
                result.content()
        );
    }
}
