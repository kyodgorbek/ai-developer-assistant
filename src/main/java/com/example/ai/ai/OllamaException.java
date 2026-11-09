package com.example.ai.ai;

/** Ollama answered, but with an error or an unusable body. Maps to HTTP 502. */
public class OllamaException extends RuntimeException {

    public OllamaException(String message) {
        super(message);
    }

    public OllamaException(String message, Throwable cause) {
        super(message, cause);
    }
}
