package com.docintel.backend.document;

import java.util.List;
import java.util.UUID;

public record RagAnswerResponse(
        UUID interactionId,
        String question,
        String answer,
        String model,
        List<RagCitation> citations,
        RagTelemetry telemetry
) {
}
