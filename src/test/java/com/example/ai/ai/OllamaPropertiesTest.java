package com.example.ai.ai;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OllamaPropertiesTest {

    @Test
    void appliesDefaultsWhenValuesMissing() {
        OllamaProperties props = new OllamaProperties(null, " ", null);

        assertThat(props.baseUrl()).isEqualTo("http://localhost:11434");
        assertThat(props.model()).isEqualTo("qwen2.5-coder:3b");
        assertThat(props.timeoutSeconds()).isEqualTo(120);
    }
}
