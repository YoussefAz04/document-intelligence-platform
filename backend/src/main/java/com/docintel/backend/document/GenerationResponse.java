package com.docintel.backend.document;

public record GenerationResponse(
        String model,
        String answer
) {
}
