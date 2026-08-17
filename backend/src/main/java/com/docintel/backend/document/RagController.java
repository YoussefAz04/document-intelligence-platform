package com.docintel.backend.document;

import jakarta.validation.Valid;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final RagService ragService;
    private final RagInteractionService interactionService;

    public RagController(RagService ragService, RagInteractionService interactionService) {
        this.ragService = ragService;
        this.interactionService = interactionService;
    }

    @PostMapping("/ask")
    public RagAnswerResponse ask(@Valid @RequestBody RagQuestionRequest request) {
        return ragService.answer(request.question(), request.limit());
    }

    @PutMapping("/interactions/{interactionId}/feedback")
    public RagFeedbackResponse feedback(
            @PathVariable UUID interactionId,
            @Valid @RequestBody RagFeedbackRequest request
    ) {
        return interactionService.submitFeedback(interactionId, request);
    }

    @GetMapping("/stats")
    public RagStatsResponse stats() {
        return interactionService.stats();
    }
}
