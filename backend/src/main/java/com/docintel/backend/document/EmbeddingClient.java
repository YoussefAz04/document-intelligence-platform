package com.docintel.backend.document;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class EmbeddingClient {

    private static final int EXPECTED_DIMENSION = 384;

    private final RestClient restClient;

    public EmbeddingClient(
            RestClient.Builder restClientBuilder,
            @Value("${docintel.ai-service.base-url}") String baseUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    public List<List<Double>> createEmbeddings(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            throw new IllegalArgumentException("At least one text is required for embedding.");
        }

        try {
            EmbeddingResponse response = restClient.post()
                    .uri("/api/embeddings")
                    .body(new EmbeddingRequest(texts))
                    .retrieve()
                    .body(EmbeddingResponse.class);

            validateResponse(response, texts.size());
            return response.embeddings();
        } catch (RestClientException exception) {
            throw new AiServiceException(
                    "The AI service is unavailable. Start it with: docker compose up -d ai-service",
                    exception
            );
        }
    }

    private void validateResponse(EmbeddingResponse response, int expectedCount) {
        if (response == null || response.embeddings() == null) {
            throw new AiServiceException("The AI service returned an empty response.");
        }
        if (response.dimension() != EXPECTED_DIMENSION) {
            throw new AiServiceException("The AI service returned an unexpected embedding dimension.");
        }
        if (response.embeddings().size() != expectedCount) {
            throw new AiServiceException("The AI service returned an unexpected number of embeddings.");
        }
        if (response.embeddings().stream().anyMatch(vector -> vector.size() != EXPECTED_DIMENSION)) {
            throw new AiServiceException("The AI service returned a malformed embedding.");
        }
    }
}
