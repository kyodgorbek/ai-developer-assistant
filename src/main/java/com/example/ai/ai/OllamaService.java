package com.example.ai.ai;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/** Talks to the local Ollama HTTP API (POST /api/chat, non-streaming). Shared by Java and Kotlin controllers. */
@Slf4j
@Service
public class OllamaService {

    private final RestClient ollamaRestClient;
    private final OllamaProperties properties;

    public OllamaService(RestClient ollamaRestClient, OllamaProperties properties) {
        this.ollamaRestClient = ollamaRestClient;
        this.properties = properties;
    }

    public AiResponse chat(AiRequest request) {
        List<Message> messages = new ArrayList<>();
        if (request.systemPrompt() != null && !request.systemPrompt().isBlank()) {
            messages.add(new Message("system", request.systemPrompt()));
        }
        messages.add(new Message("user", request.message()));

        OllamaChatRequest body = new OllamaChatRequest(properties.model(), messages, false);
        log.debug("Calling Ollama model {} at {}", properties.model(), properties.baseUrl());

        try {
            OllamaChatResponse response = ollamaRestClient.post()
                    .uri("/api/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(OllamaChatResponse.class);

            if (response == null || response.message() == null || response.message().content() == null) {
                throw new OllamaException("Ollama returned an empty response");
            }
            String model = response.model() != null ? response.model() : properties.model();
            return new AiResponse(model, response.message().content());
        } catch (ResourceAccessException e) {
            log.warn("Ollama is unreachable at {}: {}", properties.baseUrl(), e.getMessage());
            throw new OllamaUnavailableException(
                    "Ollama is not reachable at " + properties.baseUrl() + ". Is it running?", e);
        } catch (RestClientResponseException e) {
            log.warn("Ollama returned HTTP {}", e.getStatusCode().value());
            throw new OllamaException("Ollama returned HTTP " + e.getStatusCode().value()
                    + ". Check that model '" + properties.model() + "' is pulled.", e);
        } catch (RestClientException e) {
            throw new OllamaException("Unexpected error while calling Ollama: " + e.getMessage(), e);
        }
    }

    public record Message(String role, String content) {
    }

    public record OllamaChatRequest(String model, List<Message> messages, boolean stream) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OllamaChatResponse(String model, Message message, Boolean done) {
    }
}
