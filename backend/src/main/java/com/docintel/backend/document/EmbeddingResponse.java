package com.docintel.backend.document;

import java.util.List;

public record EmbeddingResponse(
        String model,
        int dimension,
        List<List<Double>> embeddings
) {
}
