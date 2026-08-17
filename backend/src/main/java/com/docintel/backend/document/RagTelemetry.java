package com.docintel.backend.document;

public record RagTelemetry(
        RagConfidence confidence,
        int retrievedSourceCount,
        int citedSourceCount,
        long retrievalDurationMs,
        long generationDurationMs,
        long totalDurationMs
) {
}
