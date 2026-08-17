package com.docintel.backend.document;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class RagService {

    private static final int DEFAULT_SOURCE_LIMIT = 5;
    private static final Pattern SOURCE_REFERENCE_PATTERN = Pattern.compile("\\[(S[1-9][0-9]*)]");

    private final HybridSearchService hybridSearchService;
    private final AnswerGenerationClient answerGenerationClient;
    private final RagInteractionService interactionService;

    public RagService(
            HybridSearchService hybridSearchService,
            AnswerGenerationClient answerGenerationClient,
            RagInteractionService interactionService
    ) {
        this.hybridSearchService = hybridSearchService;
        this.answerGenerationClient = answerGenerationClient;
        this.interactionService = interactionService;
    }

    public RagAnswerResponse answer(String question, Integer requestedLimit) {
        long startedAt = System.nanoTime();
        String cleanQuestion = question.trim();
        int limit = requestedLimit == null ? DEFAULT_SOURCE_LIMIT : requestedLimit;
        long retrievalStartedAt = System.nanoTime();
        List<HybridSearchResult> retrievedSources = hybridSearchService.search(cleanQuestion, limit);
        long retrievalDurationMs = elapsedMillis(retrievalStartedAt);

        if (retrievedSources.isEmpty()) {
            String answer = "I could not find relevant information in the available documents.";
            RagTelemetry telemetry = new RagTelemetry(
                    RagConfidence.LOW,
                    0,
                    0,
                    retrievalDurationMs,
                    0,
                    elapsedMillis(startedAt)
            );
            UUID interactionId = interactionService.record(cleanQuestion, answer, null, telemetry);
            return new RagAnswerResponse(
                    interactionId,
                    cleanQuestion,
                    answer,
                    null,
                    List.of(),
                    telemetry
            );
        }

        Map<String, HybridSearchResult> sourcesById = new LinkedHashMap<>();
        List<GenerationSource> generationSources = IntStream.range(0, retrievedSources.size())
                .mapToObj(index -> {
                    String sourceId = "S" + (index + 1);
                    HybridSearchResult result = retrievedSources.get(index);
                    sourcesById.put(sourceId, result);
                    return new GenerationSource(
                            sourceId,
                            result.filename(),
                            result.pageNumber(),
                            result.content()
                    );
                })
                .toList();

        long generationStartedAt = System.nanoTime();
        GenerationResponse generation = answerGenerationClient.generate(cleanQuestion, generationSources);
        long generationDurationMs = elapsedMillis(generationStartedAt);
        List<RagCitation> citations = findCitations(generation.answer(), sourcesById);
        RagTelemetry telemetry = new RagTelemetry(
                confidence(retrievedSources, citations),
                retrievedSources.size(),
                citations.size(),
                retrievalDurationMs,
                generationDurationMs,
                elapsedMillis(startedAt)
        );
        UUID interactionId = interactionService.record(
                cleanQuestion,
                generation.answer(),
                generation.model(),
                telemetry
        );

        return new RagAnswerResponse(
                interactionId,
                cleanQuestion,
                generation.answer(),
                generation.model(),
                citations,
                telemetry
        );
    }

    private RagConfidence confidence(
            List<HybridSearchResult> retrievedSources,
            List<RagCitation> citations
    ) {
        if (citations.isEmpty()) {
            return RagConfidence.LOW;
        }

        Set<UUID> citedChunkIds = citations.stream()
                .map(RagCitation::chunkId)
                .collect(java.util.stream.Collectors.toSet());
        List<HybridSearchResult> citedResults = retrievedSources.stream()
                .filter(result -> citedChunkIds.contains(result.chunkId()))
                .toList();
        boolean hasHybridMatch = citedResults.stream()
                .anyMatch(result -> result.matchedBy() == SearchMatchType.BOTH);
        double strongestSemanticScore = citedResults.stream()
                .map(HybridSearchResult::semanticScore)
                .filter(java.util.Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0);

        if ((hasHybridMatch && strongestSemanticScore >= 0.60) || citations.size() >= 2) {
            return RagConfidence.HIGH;
        }
        return RagConfidence.MEDIUM;
    }

    private long elapsedMillis(long startedAt) {
        return Math.max(0, (System.nanoTime() - startedAt) / 1_000_000);
    }

    private List<RagCitation> findCitations(
            String answer,
            Map<String, HybridSearchResult> sourcesById
    ) {
        Set<String> referencedSourceIds = new LinkedHashSet<>();
        Matcher matcher = SOURCE_REFERENCE_PATTERN.matcher(answer);
        while (matcher.find()) {
            String sourceId = matcher.group(1);
            if (sourcesById.containsKey(sourceId)) {
                referencedSourceIds.add(sourceId);
            }
        }

        return referencedSourceIds.stream()
                .map(sourceId -> RagCitation.from(sourceId, sourcesById.get(sourceId)))
                .toList();
    }
}
