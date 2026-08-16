package com.docintel.backend.document;

import java.time.Instant;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        String originalFilename,
        String contentType,
        DocumentStatus status,
        Instant createdAt,
        long chunkCount
) {
    public static DocumentResponse from(Document document) {
        return from(document, 0);
    }

    public static DocumentResponse from(Document document, long chunkCount) {
        return new DocumentResponse(
                document.getId(),
                document.getOriginalFilename(),
                document.getContentType(),
                document.getStatus(),
                document.getCreatedAt(),
                chunkCount
        );
    }
}
