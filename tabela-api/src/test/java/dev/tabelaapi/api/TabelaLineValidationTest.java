package dev.tabelaapi.api;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TabelaLineValidationTest {
    @Test
    void returns_empty_when_every_guarded_line_is_valid() {
        TabelaPrompt prompt = TabelaPrompt.builder().validator(TabelaValidators.notBlank()).build();
        assertThat(TabelaLineValidation.firstFailure(prompt, List.of("Ali", "", "", ""), 0)).isEmpty();
    }

    @Test
    void returns_the_validator_message() {
        TabelaPrompt prompt = TabelaPrompt.builder().validator(TabelaValidators.notBlank()).build();
        assertThat(TabelaLineValidation.firstFailure(prompt, List.of("   ", "", "", ""), 0))
            .hasValueSatisfying(result -> {
                assertThat(result.valid()).isFalse();
                assertThat(result.message()).isEqualTo("Bu alan boş bırakılamaz.");
            });
    }

    @Test
    void skips_lines_without_a_validator() {
        TabelaPrompt prompt = TabelaPrompt.builder().validator(TabelaValidators.notBlank()).build();
        assertThat(TabelaLineValidation.firstFailure(prompt, List.of("Ali", "  ", " ", ""), 0)).isEmpty();
    }

    @Test
    void enforces_the_configured_max_line_length_on_every_line() {
        TabelaPrompt prompt = TabelaPrompt.builder().build();
        assertThat(TabelaLineValidation.firstFailure(prompt, List.of("Ali", "", "uzun", ""), 3))
            .hasValueSatisfying(result -> assertThat(result.message()).contains("Satır 3"));
    }

    @Test
    void replaces_a_blank_failure_message() {
        TabelaPrompt prompt = TabelaPrompt.builder().validator(value -> TabelaValidationResult.invalid(" ")).build();
        assertThat(TabelaLineValidation.firstFailure(prompt, List.of("x", "", "", ""), 0))
            .hasValueSatisfying(result -> assertThat(result.message()).isEqualTo("Girdi geçersiz."));
    }

    @Test
    void handles_null_lines_from_the_packet() {
        TabelaPrompt prompt = TabelaPrompt.builder().validator(TabelaValidators.notBlank()).build();
        List<String> lines = Arrays.asList(null, null, null, null);
        assertThat(TabelaLineValidation.firstFailure(prompt, lines, 0)).isPresent();
    }
}
