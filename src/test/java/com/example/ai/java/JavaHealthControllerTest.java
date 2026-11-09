package com.example.ai.java;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ai.common.HealthResponse;
import org.junit.jupiter.api.Test;

class JavaHealthControllerTest {

    @Test
    void healthReturnsUpForJava() {
        HealthResponse response = new JavaHealthController().health();

        assertThat(response.status()).isEqualTo("UP");
        assertThat(response.language()).isEqualTo("java");
    }
}
