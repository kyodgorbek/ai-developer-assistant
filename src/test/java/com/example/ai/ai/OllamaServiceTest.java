package com.example.ai.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class OllamaServiceTest {

    private static final String BASE_URL = "http://localhost:11434";

    private MockRestServiceServer server;
    private OllamaService service;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        service = new OllamaService(builder.build(), new OllamaProperties(BASE_URL, "qwen2.5-coder:3b", 30));
    }

    @Test
    void returnsAnswerAndSendsNonStreamingRequest() {
        server.expect(requestTo(BASE_URL + "/api/chat"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.model").value("qwen2.5-coder:3b"))
                .andExpect(jsonPath("$.stream").value(false))
                .andExpect(jsonPath("$.messages[0].role").value("system"))
                .andExpect(jsonPath("$.messages[1].role").value("user"))
                .andExpect(jsonPath("$.messages[1].content").value("What is a record?"))
                .andRespond(withSuccess("""
                        {"model":"qwen2.5-coder:3b","created_at":"2025-01-01T00:00:00Z",
                         "message":{"role":"assistant","content":"A compact immutable class."},
                         "done":true,"total_duration":123}
                        """, MediaType.APPLICATION_JSON));

        AiResponse response = service.chat(new AiRequest("What is a record?", "You are a Java tutor"));

        assertThat(response.model()).isEqualTo("qwen2.5-coder:3b");
        assertThat(response.answer()).isEqualTo("A compact immutable class.");
        server.verify();
    }

    @Test
    void connectionFailureBecomesOllamaUnavailableException() {
        server.expect(requestTo(BASE_URL + "/api/chat"))
                .andRespond(request -> {
                    throw new IOException("Connection refused");
                });

        assertThatThrownBy(() -> service.chat(new AiRequest("hi", null)))
                .isInstanceOf(OllamaUnavailableException.class)
                .hasMessageContaining("not reachable");
    }

    @Test
    void httpErrorBecomesOllamaException() {
        server.expect(requestTo(BASE_URL + "/api/chat")).andRespond(withServerError());

        assertThatThrownBy(() -> service.chat(new AiRequest("hi", null)))
                .isInstanceOf(OllamaException.class)
                .isNotInstanceOf(OllamaUnavailableException.class)
                .hasMessageContaining("HTTP 500");
    }

    @Test
    void emptyBodyBecomesOllamaException() {
        server.expect(requestTo(BASE_URL + "/api/chat"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> service.chat(new AiRequest("hi", null)))
                .isInstanceOf(OllamaException.class)
                .hasMessageContaining("empty response");
    }
}
