package com.docintel.backend.document;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RagQuestionRequest(
        @NotBlank
        @Size(max = 2000)
        String question,

        @Min(1)
        @Max(10)
        Integer limit
) {
}
