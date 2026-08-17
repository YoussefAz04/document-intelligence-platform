package com.docintel.backend.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RagServiceTests {

    @Mock
    private HybridSearchService hybridSearchService;

    @Mock
    private AnswerGenerationClient answerGenerationClient;

    @InjectMocks
    private RagService ragService;

    @Test
    void returnsOnlyCitationsReferencedByTheGeneratedAnswer() {
        HybridSearchResult firstSource = result("requirements.txt", 0);
        HybridSearchResult secondSource = result("calendar.txt", 1);
        when(hybridSearchService.search("What documents are required?", 5))
                .thenReturn(List.of(firstSource, secondSource));
        when(answerGenerationClient.generate(
                org.mockito.ArgumentMatchers.eq("What documents are required?"),
                org.mockito.ArgumentMatchers.anyList()
        )).thenReturn(new GenerationResponse(
                "test-model",
                "Applicants need a passport and transcript [S1]."
        ));

        RagAnswerResponse response = ragService.answer(" What documents are required? ", null);

        assertThat(response.model()).isEqualTo("test-model");
        assertThat(response.citations()).hasSize(1);
        assertThat(response.citations().get(0).sourceId()).isEqualTo("S1");
        assertThat(response.citations().get(0).filename()).isEqualTo("requirements.txt");
    }

    @Test
    void skipsGenerationWhenRetrievalFindsNothing() {
        when(hybridSearchService.search("Unknown question", 3)).thenReturn(List.of());

        RagAnswerResponse response = ragService.answer("Unknown question", 3);

        assertThat(response.model()).isNull();
        assertThat(response.citations()).isEmpty();
        assertThat(response.answer()).contains("could not find relevant information");
        verify(answerGenerationClient, never()).generate(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyList()
        );
    }

    private HybridSearchResult result(String filename, int chunkIndex) {
        return new HybridSearchResult(
                UUID.randomUUID(),
                UUID.randomUUID(),
                filename,
                chunkIndex,
                null,
                "Applicants need a passport and transcript.",
                0.03,
                0.7,
                0.1,
                SearchMatchType.BOTH
        );
    }
}
