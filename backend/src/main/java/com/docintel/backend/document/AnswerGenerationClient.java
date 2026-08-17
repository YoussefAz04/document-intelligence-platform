package com.docintel.backend.document;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class AnswerGenerationClient {

    private final RestClient restClient;

    public AnswerGenerationClient(
            RestClient.Builder restClientBuilder,
            @Value("${docintel.ai-service.base-url}") String baseUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    public GenerationResponse generate(String question, List<GenerationSource> sources) {
        try {
            GenerationResponse response = restClient.post()
                    .uri("/api/generate")
                    .body(new GenerationRequest(question, sources))
                    .retrieve()
                    .body(GenerationResponse.class);

            if (response == null || response.answer() == null || response.answer().isBlank()) {
                throw new AiServiceException("The AI service returned an empty answer.");
            }
            return response;
        } catch (RestClientException exception) {
            throw new AiServiceException(
                    "Answer generation failed. Check that OPENAI_API_KEY is configured and restart the AI service.",
                    exception
            );
        }
    }
}
