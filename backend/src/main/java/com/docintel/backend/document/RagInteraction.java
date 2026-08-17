package com.docintel.backend.document;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "rag_interactions")
public class RagInteraction {

    @Id
    private UUID id;

    @Column(nullable = false, columnDefinition = "text")
    private String question;

    @Column(nullable = false, columnDefinition = "text")
    private String answer;

    @Column(length = 120)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RagConfidence confidence;

    @Column(nullable = false)
    private int retrievedSourceCount;

    @Column(nullable = false)
    private int citedSourceCount;

    @Column(nullable = false)
    private long retrievalDurationMs;

    @Column(nullable = false)
    private long generationDurationMs;

    @Column(nullable = false)
    private long totalDurationMs;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RagFeedback feedback;

    @Column(columnDefinition = "text")
    private String feedbackComment;

    @Column(nullable = false)
    private Instant createdAt;

    protected RagInteraction() {
    }

    private RagInteraction(
            String question,
            String answer,
            String model,
            RagTelemetry telemetry
    ) {
        this.id = UUID.randomUUID();
        this.question = question;
        this.answer = answer;
        this.model = model;
        this.confidence = telemetry.confidence();
        this.retrievedSourceCount = telemetry.retrievedSourceCount();
        this.citedSourceCount = telemetry.citedSourceCount();
        this.retrievalDurationMs = telemetry.retrievalDurationMs();
        this.generationDurationMs = telemetry.generationDurationMs();
        this.totalDurationMs = telemetry.totalDurationMs();
        this.createdAt = Instant.now();
    }

    public static RagInteraction create(
            String question,
            String answer,
            String model,
            RagTelemetry telemetry
    ) {
        return new RagInteraction(question, answer, model, telemetry);
    }

    public void applyFeedback(RagFeedback feedback, String comment) {
        this.feedback = feedback;
        this.feedbackComment = comment == null || comment.isBlank() ? null : comment.trim();
    }

    public UUID getId() { return id; }
    public RagFeedback getFeedback() { return feedback; }
    public String getFeedbackComment() { return feedbackComment; }
}
