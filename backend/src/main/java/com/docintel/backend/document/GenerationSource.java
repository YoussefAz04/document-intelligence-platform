package com.docintel.backend.document;

public record GenerationSource(
        String sourceId,
        String filename,
        Integer pageNumber,
        String content
) {
}
