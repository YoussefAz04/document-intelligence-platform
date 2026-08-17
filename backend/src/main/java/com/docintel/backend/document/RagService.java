package com.docintel.backend.document;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;

@Service
public class RagService {

    private static final int DEFAULT_SOURCE_LIMIT = 5;
    private static final Pattern SOURCE_REFERENCE_PATTERN = Pattern.compile("\\[(S[1-9][0-9]*)]");

    private final HybridSearchService hybridSearchService;
    private final AnswerGenerationClient answerGenerationClient;

    public RagService(
            HybridSearchService hybridSearchService,
            AnswerGenerationClient answerGenerationClient
    ) {
        this.hybridSearchService = hybridSearchService;
        this.answerGenerationClient = answerGenerationClient;
    }

    public RagAnswerResponse answer(String question, Integer requestedLimit) {
        String cleanQuestion = question.trim();
        int limit = requestedLimit == null ? DEFAULT_SOURCE_LIMIT : requestedLimit;
        List<HybridSearchResult> retrievedSources = hybridSearchService.search(cleanQuestion, limit);

        if (retrievedSources.isEmpty()) {
            return new RagAnswerResponse(
                    cleanQuestion,
                    "I could not find relevant information in the available documents.",
                    null,
                    List.of()
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

        GenerationResponse generation = answerGenerationClient.generate(cleanQuestion, generationSources);
        List<RagCitation> citations = findCitations(generation.answer(), sourcesById);

        return new RagAnswerResponse(
                cleanQuestion,
                generation.answer(),
                generation.model(),
                citations
        );
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
