package com.docintel.backend.document;

import java.time.Instant;
import java.util.UUID;

public record DocumentChunkResponse(
        UUID id,
        UUID documentId,
        int chunkIndex,
        Integer pageNumber,
        String content,
        Instant createdAt
) {
    public static DocumentChunkResponse from(DocumentChunk chunk) {
        return new DocumentChunkResponse(
                chunk.getId(),
                chunk.getDocument().getId(),
                chunk.getChunkIndex(),
                chunk.getPageNumber(),
                chunk.getContent(),
                chunk.getCreatedAt()
        );
    }
}
