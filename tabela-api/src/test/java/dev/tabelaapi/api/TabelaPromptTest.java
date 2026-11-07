package dev.tabelaapi.api;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TabelaPromptTest {
    @Test void prompt_requires_four_lines() {
        assertThatThrownBy(() -> TabelaPrompt.builder().lines("tek").build()).isInstanceOf(IllegalArgumentException.class);
    }
}
