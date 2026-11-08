package dev.tabelaapi.api;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TabelaPromptTest {
    private static final TabelaValidation REJECT = value -> TabelaValidationResult.invalid("olmaz");

    @Test
    void pads_missing_lines_with_empty_strings() {
        TabelaPrompt prompt = TabelaPrompt.builder().lines("tek").build();
        assertThat(prompt.lines()).containsExactly("tek", "", "", "");
    }

    @Test
    void rejects_more_than_four_lines() {
        assertThatThrownBy(() -> TabelaPrompt.builder().lines("a", "b", "c", "d", "e").build())
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void prompt_wide_validator_guards_only_the_first_line_by_default() {
        TabelaPrompt prompt = TabelaPrompt.builder().validator(REJECT).build();
        assertThat(prompt.validationFor(0)).containsSame(REJECT);
        assertThat(prompt.validationFor(1)).isEmpty();
    }

    @Test
    void validate_all_lines_applies_the_prompt_wide_validator_everywhere() {
        TabelaPrompt prompt = TabelaPrompt.builder().validator(REJECT).validateAllLines(true).build();
        assertThat(prompt.validationFor(3)).containsSame(REJECT);
    }

    @Test
    void per_line_validator_wins_over_the_prompt_wide_one() {
        TabelaValidation perLine = value -> TabelaValidationResult.success();
        TabelaPrompt prompt = TabelaPrompt.builder().validator(REJECT).validator(2, perLine).build();
        assertThat(prompt.validationFor(0)).containsSame(REJECT);
        assertThat(prompt.validationFor(2)).containsSame(perLine);
    }

    @Test
    void rejects_out_of_range_line_indices() {
        assertThatThrownBy(() -> TabelaPrompt.builder().validator(4, REJECT))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void with_lines_keeps_validators_and_callbacks() {
        TabelaPrompt prompt = TabelaPrompt.builder()
            .lines("a", "", "", "")
            .validator(1, REJECT)
            .onComplete(result -> TabelaAction.message(result.line(0)))
            .build();

        TabelaPrompt copy = prompt.withLines(List.of("x", "y", "z", "w"));

        assertThat(copy.lines()).containsExactly("x", "y", "z", "w");
        assertThat(copy.validationFor(1)).containsSame(REJECT);
        TabelaResult result = new TabelaResult(new TabelaSession(java.util.UUID.randomUUID(), java.util.UUID.randomUUID(), java.time.Instant.now()), copy.lines());
        assertThat(copy.complete().apply(result)).isEqualTo(TabelaAction.message("x"));
    }

    @Test
    void timeout_defaults_and_can_be_overridden() {
        assertThat(TabelaPrompt.builder().build().timeout()).isEqualTo(TabelaPrompt.DEFAULT_TIMEOUT);
        assertThat(TabelaPrompt.builder().build().explicitTimeout()).isEmpty();

        TabelaPrompt prompt = TabelaPrompt.builder().timeout(Duration.ofSeconds(5)).build();
        assertThat(prompt.timeout()).isEqualTo(Duration.ofSeconds(5));
        assertThat(prompt.explicitTimeout()).contains(Duration.ofSeconds(5));
    }

    @Test
    void rejects_non_positive_timeouts() {
        assertThatThrownBy(() -> TabelaPrompt.builder().timeout(Duration.ZERO))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void reopen_pads_its_lines() {
        assertThat(TabelaAction.reopen("yeni").lines()).containsExactly("yeni", "", "", "");
        assertThatThrownBy(() -> TabelaAction.reopen("a", "b", "c", "d", "e"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
