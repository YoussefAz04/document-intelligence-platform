package com.docintel.backend.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HybridSearchServiceTests {

    @Mock
    private SemanticSearchService semanticSearchService;

    @Mock
    private KeywordSearchService keywordSearchService;

    @InjectMocks
    private HybridSearchService hybridSearchService;

    @Test
    void combinesAndDeduplicatesResultsFromBothRetrievers() {
        UUID sharedChunkId = UUID.randomUUID();
        UUID semanticOnlyChunkId = UUID.randomUUID();
        UUID keywordOnlyChunkId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();

        when(semanticSearchService.search("passport transcript", 9)).thenReturn(List.of(
                semanticResult(sharedChunkId, documentId, 0.78),
                semanticResult(semanticOnlyChunkId, documentId, 0.64)
        ));
        when(keywordSearchService.search("passport transcript", 9)).thenReturn(List.of(
                keywordResult(sharedChunkId, documentId, 0.12),
                keywordResult(keywordOnlyChunkId, documentId, 0.08)
        ));

        List<HybridSearchResult> results = hybridSearchService.search(" passport transcript ", 3);

        assertThat(results).hasSize(3);
        assertThat(results.get(0).chunkId()).isEqualTo(sharedChunkId);
        assertThat(results.get(0).matchedBy()).isEqualTo(SearchMatchType.BOTH);
        assertThat(results.get(0).semanticScore()).isEqualTo(0.78);
        assertThat(results.get(0).keywordScore()).isEqualTo(0.12);
        assertThat(results.get(0).fusedScore()).isGreaterThan(results.get(1).fusedScore());
    }

    @Test
    void rejectsAnInvalidLimitBeforeCallingRetrievers() {
        assertThatThrownBy(() -> hybridSearchService.search("passport", 21))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Search limit must be between 1 and 20.");
    }

    private SemanticSearchResult semanticResult(UUID chunkId, UUID documentId, double score) {
        return new SemanticSearchResult(
                chunkId,
                documentId,
                "demo-handbook.txt",
                0,
                null,
                "Applicants must provide a passport and transcript.",
                score
        );
    }

    private KeywordSearchResult keywordResult(UUID chunkId, UUID documentId, double score) {
        return new KeywordSearchResult(
                chunkId,
                documentId,
                "demo-handbook.txt",
                0,
                null,
                "Applicants must provide a passport and transcript.",
                score
        );
    }
}
