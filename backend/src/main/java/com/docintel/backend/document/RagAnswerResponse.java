package com.docintel.backend.document;

import java.util.List;

public record RagAnswerResponse(
        String question,
        String answer,
        String model,
        List<RagCitation> citations
) {
}
