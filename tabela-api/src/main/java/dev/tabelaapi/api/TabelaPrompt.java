package dev.tabelaapi.api;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * A four-line prompt that is displayed to a single player through a virtual sign.
 *
 * <p>Lines are always normalized to {@link #LINE_COUNT} entries: missing lines become empty
 * strings and more than four lines are rejected. A validator can target one line with
 * {@link Builder#validator(int, TabelaValidation)} or the whole prompt with
 * {@link Builder#validator(TabelaValidation)}.</p>
 */
public final class TabelaPrompt {
    /** Number of lines a Minecraft sign has. */
    public static final int LINE_COUNT = 4;
    /** Timeout used when the prompt does not configure one. */
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(60);

    private final String title;
    private final List<String> lines;
    private final TabelaValidation validation;
    private final TabelaValidation[] lineValidations;
    private final Function<TabelaResult, TabelaAction> complete;
    private final Function<TabelaResult, TabelaAction> cancel;
    private final Duration timeout;
    private final boolean validateAllLines;

    private TabelaPrompt(Builder builder) {
        title = builder.title;
        lines = normalizeLines(builder.lines);
        validation = builder.validation;
        lineValidations = builder.lineValidations.clone();
        complete = builder.complete;
        cancel = builder.cancel;
        timeout = builder.timeout;
        validateAllLines = builder.validateAllLines;
    }

    public String title() { return title; }

    /** Returns exactly {@link #LINE_COUNT} lines. */
    public List<String> lines() { return lines; }

    public TabelaValidation validation() { return validation; }

    public Function<TabelaResult, TabelaAction> complete() { return complete; }

    public Function<TabelaResult, TabelaAction> cancel() { return cancel; }

    /** Configured timeout, or {@link #DEFAULT_TIMEOUT} when the prompt does not set one. */
    public Duration timeout() { return timeout == null ? DEFAULT_TIMEOUT : timeout; }

    /** Timeout configured on the builder, empty when the host should choose one. */
    public Optional<Duration> explicitTimeout() { return Optional.ofNullable(timeout); }

    /** Whether {@link #validation()} applies to every line instead of only the first one. */
    public boolean validateAllLines() { return validateAllLines; }

    /**
     * Returns the validator that guards {@code line}, if any.
     *
     * <p>A per-line validator wins over the prompt-wide one. The prompt-wide validator guards the
     * first line, or every line when {@link #validateAllLines()} is {@code true}.</p>
     */
    public Optional<TabelaValidation> validationFor(int line) {
        if (line < 0 || line >= LINE_COUNT) {
            throw new IllegalArgumentException("Satır indeksi 0 ile " + (LINE_COUNT - 1) + " arasında olmalı");
        }
        if (lineValidations[line] != null) return Optional.of(lineValidations[line]);
        if (validateAllLines || line == 0) return Optional.of(validation);
        return Optional.empty();
    }

    /** Returns a copy of this prompt with different pre-filled lines and identical behaviour. */
    public TabelaPrompt withLines(List<String> newLines) {
        Builder builder = builder()
            .title(title)
            .lines(normalizeLines(newLines).toArray(String[]::new))
            .validator(validation)
            .onComplete(complete)
            .onCancel(cancel)
            .validateAllLines(validateAllLines);
        if (timeout != null) builder.timeout(timeout);
        for (int line = 0; line < LINE_COUNT; line++) {
            if (lineValidations[line] != null) builder.validator(line, lineValidations[line]);
        }
        return builder.build();
    }

    /** Pads or rejects lines so the result has exactly {@link #LINE_COUNT} non-null entries. */
    public static List<String> normalizeLines(List<String> lines) {
        Objects.requireNonNull(lines, "lines");
        if (lines.size() > LINE_COUNT) {
            throw new IllegalArgumentException("Tabela ekranı en fazla " + LINE_COUNT + " satır kabul eder");
        }
        List<String> normalized = new ArrayList<>(LINE_COUNT);
        for (int line = 0; line < LINE_COUNT; line++) {
            String value = line < lines.size() ? lines.get(line) : null;
            if (line < lines.size() && value == null) {
                throw new NullPointerException("Satır " + (line + 1) + " null olamaz");
            }
            normalized.add(value == null ? "" : value);
        }
        return List.copyOf(normalized);
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String title = "Giriş";
        private List<String> lines = List.of("", "", "", "");
        private TabelaValidation validation = value -> TabelaValidationResult.success();
        private final TabelaValidation[] lineValidations = new TabelaValidation[LINE_COUNT];
        private Function<TabelaResult, TabelaAction> complete = ignored -> TabelaAction.close();
        private Function<TabelaResult, TabelaAction> cancel = ignored -> TabelaAction.close();
        private Duration timeout;
        private boolean validateAllLines;

        public Builder title(String title) { this.title = Objects.requireNonNull(title, "title"); return this; }

        /** Accepts fewer than four lines; missing ones are padded with empty strings. */
        public Builder lines(String... lines) { this.lines = List.of(lines); return this; }

        /** Applies {@code validation} to the first line, or to every line when {@code validateAllLines} is set. */
        public Builder validator(TabelaValidation validation) { this.validation = Objects.requireNonNull(validation, "validation"); return this; }

        /** Applies {@code validation} only to the given line. */
        public Builder validator(int line, TabelaValidation validation) {
            if (line < 0 || line >= LINE_COUNT) {
                throw new IllegalArgumentException("Satır indeksi 0 ile " + (LINE_COUNT - 1) + " arasında olmalı");
            }
            lineValidations[line] = Objects.requireNonNull(validation, "validation");
            return this;
        }

        public Builder onComplete(Function<TabelaResult, TabelaAction> complete) { this.complete = Objects.requireNonNull(complete, "complete"); return this; }

        public Builder onCancel(Function<TabelaResult, TabelaAction> cancel) { this.cancel = Objects.requireNonNull(cancel, "cancel"); return this; }

        public Builder timeout(Duration timeout) {
            Objects.requireNonNull(timeout, "timeout");
            if (timeout.isNegative() || timeout.isZero()) {
                throw new IllegalArgumentException("Zaman aşımı pozitif olmalı");
            }
            this.timeout = timeout;
            return this;
        }

        public Builder validateAllLines(boolean validateAllLines) { this.validateAllLines = validateAllLines; return this; }

        public TabelaPrompt build() { return new TabelaPrompt(this); }
    }
}
