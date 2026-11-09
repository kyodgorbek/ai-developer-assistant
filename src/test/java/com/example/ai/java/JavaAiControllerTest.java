package com.example.ai.java;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.ai.ai.AiRequest;
import com.example.ai.ai.AiResponse;
import com.example.ai.ai.OllamaException;
import com.example.ai.ai.OllamaService;
import com.example.ai.ai.OllamaUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({JavaAiController.class, JavaHealthController.class})
class JavaAiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OllamaService ollamaService;

    @Test
    void healthEndpointReturnsUp() throws Exception {
        mockMvc.perform(get("/api/v1/java/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.language").value("java"));
    }

    @Test
    void chatReturnsAnswerFromService() throws Exception {
        when(ollamaService.chat(new AiRequest("Explain records", null)))
                .thenReturn(new AiResponse("qwen2.5-coder:3b", "Records are immutable data carriers."));

        mockMvc.perform(post("/api/v1/java/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Explain records\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model").value("qwen2.5-coder:3b"))
                .andExpect(jsonPath("$.answer").value("Records are immutable data carriers."));

        verify(ollamaService).chat(new AiRequest("Explain records", null));
    }

    @Test
    void blankMessageIsRejectedWithBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/java/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details[0]").value("message: message must not be blank"));

        verifyNoInteractions(ollamaService);
    }

    @Test
    void malformedJsonIsRejectedWithBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/java/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not json"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(ollamaService);
    }

    @Test
    void ollamaDownReturnsServiceUnavailable() throws Exception {
        when(ollamaService.chat(new AiRequest("hi", null)))
                .thenThrow(new OllamaUnavailableException("Ollama is not reachable"));

        mockMvc.perform(post("/api/v1/java/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"hi\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("Ollama is not reachable"));
    }

    @Test
    void ollamaErrorReturnsBadGateway() throws Exception {
        when(ollamaService.chat(new AiRequest("hi", null)))
                .thenThrow(new OllamaException("Ollama returned HTTP 500"));

        mockMvc.perform(post("/api/v1/java/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"hi\"}"))
                .andExpect(status().isBadGateway());
    }
}
