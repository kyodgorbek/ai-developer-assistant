package com.example.ai.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Ollama settings bound from the {@code ollama.*} keys in application.yml
 * (overridable with OLLAMA_BASE_URL, OLLAMA_MODEL, OLLAMA_TIMEOUT_SECONDS).
 */
@ConfigurationProperties(prefix = "ollama")
public record OllamaProperties(String baseUrl, String model, Integer timeoutSeconds) {

    public OllamaProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "http://localhost:11434";
        }
        if (model == null || model.isBlank()) {
            model = "qwen2.5-coder:3b";
        }
        if (timeoutSeconds == null || timeoutSeconds <= 0) {
            timeoutSeconds = 120;
        }
    }
}
