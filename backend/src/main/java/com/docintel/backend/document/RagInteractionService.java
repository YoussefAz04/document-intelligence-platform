package com.docintel.backend.document;

import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RagInteractionService {

    private final RagInteractionRepository repository;

    public RagInteractionService(RagInteractionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public UUID record(String question, String answer, String model, RagTelemetry telemetry) {
        RagInteraction interaction = RagInteraction.create(question, answer, model, telemetry);
        repository.save(interaction);
        return interaction.getId();
    }

    @Transactional
    public RagFeedbackResponse submitFeedback(UUID interactionId, RagFeedbackRequest request) {
        RagInteraction interaction = repository.findById(interactionId)
                .orElseThrow(() -> new NoSuchElementException("RAG interaction was not found."));
        interaction.applyFeedback(request.feedback(), request.comment());
        return RagFeedbackResponse.from(interaction);
    }

    @Transactional(readOnly = true)
    public RagStatsResponse stats() {
        return RagStatsResponse.from(repository.summarize());
    }
}
