package com.docintel.backend.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RagInteractionServiceTests {

    @Mock
    private RagInteractionRepository repository;

    @InjectMocks
    private RagInteractionService service;

    @Test
    void recordsAnAnswerWithTelemetry() {
        RagTelemetry telemetry = new RagTelemetry(RagConfidence.HIGH, 5, 2, 40, 500, 550);

        UUID interactionId = service.record("Question", "Answer [S1]", "llama3.2", telemetry);

        ArgumentCaptor<RagInteraction> captor = ArgumentCaptor.forClass(RagInteraction.class);
        verify(repository).save(captor.capture());
        assertThat(interactionId).isNotNull();
        assertThat(captor.getValue().getId()).isEqualTo(interactionId);
    }

    @Test
    void updatesFeedbackOnAnExistingInteraction() {
        RagInteraction interaction = RagInteraction.create(
                "Question",
                "Answer [S1]",
                "llama3.2",
                new RagTelemetry(RagConfidence.MEDIUM, 3, 1, 20, 300, 325)
        );
        when(repository.findById(interaction.getId())).thenReturn(Optional.of(interaction));

        RagFeedbackResponse response = service.submitFeedback(
                interaction.getId(),
                new RagFeedbackRequest(RagFeedback.HELPFUL, "Accurate citation")
        );

        assertThat(response.feedback()).isEqualTo(RagFeedback.HELPFUL);
        assertThat(response.comment()).isEqualTo("Accurate citation");
    }

    @Test
    void calculatesQualityRatesFromAggregatedStatistics() {
        RagStatsProjection projection = org.mockito.Mockito.mock(RagStatsProjection.class);
        when(projection.getTotalAnswers()).thenReturn(10L);
        when(projection.getRatedAnswers()).thenReturn(8L);
        when(projection.getHelpfulAnswers()).thenReturn(6L);
        when(projection.getAverageLatencyMs()).thenReturn(725.4);
        when(projection.getAverageRetrievedSources()).thenReturn(4.25);
        when(repository.summarize()).thenReturn(projection);

        RagStatsResponse stats = service.stats();

        assertThat(stats.feedbackRate()).isEqualTo(80.0);
        assertThat(stats.helpfulRate()).isEqualTo(75.0);
        assertThat(stats.averageLatencyMs()).isEqualTo(725);
        assertThat(stats.averageRetrievedSources()).isEqualTo(4.25);
    }
}
