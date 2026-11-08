package dev.tabelaapi.api;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Runs a prompt's validators over submitted sign lines. */
public final class TabelaLineValidation {
    private TabelaLineValidation() { }

    /**
     * Returns the first failure for {@code lines}, or empty when every guarded line is valid.
     *
     * <p>{@code maxLineLength} is an additional limit applied to every line; use a value below one
     * to disable it.</p>
     */
    public static Optional<TabelaValidationResult> firstFailure(TabelaPrompt prompt, List<String> lines, int maxLineLength) {
        Objects.requireNonNull(prompt, "prompt");
        Objects.requireNonNull(lines, "lines");
        for (int line = 0; line < TabelaPrompt.LINE_COUNT && line < lines.size(); line++) {
            String value = lines.get(line) == null ? "" : lines.get(line);
            if (maxLineLength > 0 && TabelaText.normalize(value).length() > maxLineLength) {
                return Optional.of(TabelaValidationResult.invalid(
                    "Satır " + (line + 1) + " en fazla " + maxLineLength + " karakter olabilir."));
            }
            Optional<TabelaValidation> validation = prompt.validationFor(line);
            if (validation.isEmpty()) continue;
            TabelaValidationResult result = validation.get().validate(value);
            if (!result.valid()) {
                String message = result.message() == null || result.message().isBlank()
                    ? "Girdi geçersiz." : result.message();
                return Optional.of(TabelaValidationResult.invalid(message));
            }
        }
        return Optional.empty();
    }
}
