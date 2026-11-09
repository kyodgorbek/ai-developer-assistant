package com.example.ai.ai;

/** Ollama could not be reached (not running, wrong URL, timeout). Maps to HTTP 503. */
public class OllamaUnavailableException extends OllamaException {

    public OllamaUnavailableException(String message) {
        super(message);
    }

    public OllamaUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
