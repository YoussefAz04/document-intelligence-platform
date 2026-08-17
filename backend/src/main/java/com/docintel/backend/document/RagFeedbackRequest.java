package com.docintel.backend.document;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RagFeedbackRequest(
        @NotNull RagFeedback feedback,
        @Size(max = 500) String comment
) {
}
