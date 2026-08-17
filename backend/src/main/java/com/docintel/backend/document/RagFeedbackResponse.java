package com.docintel.backend.document;

import java.util.UUID;

public record RagFeedbackResponse(
        UUID interactionId,
        RagFeedback feedback,
        String comment
) {
    public static RagFeedbackResponse from(RagInteraction interaction) {
        return new RagFeedbackResponse(
                interaction.getId(),
                interaction.getFeedback(),
                interaction.getFeedbackComment()
        );
    }
}
