package com.example.ai;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.ai.ai.AiRequest;
import com.example.ai.ai.AiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.example.ai.ai.OllamaService;

/** Full application context; Ollama itself is mocked, so no AI model is needed. */
@SpringBootTest
@AutoConfigureMockMvc
class ApplicationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OllamaService ollamaService;

    @Test
    void bothHealthEndpointsAreExposed() throws Exception {
        mockMvc.perform(get("/api/v1/java/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.language").value("java"));
        mockMvc.perform(get("/api/v1/kotlin/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.language").value("kotlin"));
    }

    @Test
    void javaAndKotlinControllersShareTheSameService() throws Exception {
        when(ollamaService.chat(new AiRequest("ping", null)))
                .thenReturn(new AiResponse("test-model", "pong"));

        for (String path : new String[] {"/api/v1/java/ai/chat", "/api/v1/kotlin/ai/chat"}) {
            mockMvc.perform(post(path)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"message\":\"ping\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.answer").value("pong"));
        }
    }
}
