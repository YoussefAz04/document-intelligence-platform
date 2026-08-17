package com.docintel.backend.document;

import java.util.List;

public record GenerationRequest(
        String question,
        List<GenerationSource> sources
) {
}
