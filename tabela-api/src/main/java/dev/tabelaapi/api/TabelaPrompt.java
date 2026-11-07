package dev.tabelaapi.api;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public final class TabelaPrompt {
    private final String title;
    private final List<String> lines;
    private final TabelaValidation validation;
    private final Function<TabelaResult, TabelaAction> complete;
    private final Function<TabelaResult, TabelaAction> cancel;
    private final Duration timeout;

    private TabelaPrompt(Builder builder) {
        title = builder.title;
        lines = List.copyOf(builder.lines);
        validation = builder.validation;
        complete = builder.complete;
        cancel = builder.cancel;
        timeout = builder.timeout;
    }

    public String title() { return title; }
    public List<String> lines() { return lines; }
    public TabelaValidation validation() { return validation; }
    public Function<TabelaResult, TabelaAction> complete() { return complete; }
    public Function<TabelaResult, TabelaAction> cancel() { return cancel; }
    public Duration timeout() { return timeout; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String title = "Giriş";
        private List<String> lines = List.of("", "", "", "");
        private TabelaValidation validation = value -> TabelaValidationResult.success();
        private Function<TabelaResult, TabelaAction> complete = ignored -> TabelaAction.close();
        private Function<TabelaResult, TabelaAction> cancel = ignored -> TabelaAction.close();
        private Duration timeout = Duration.ofSeconds(60);

        public Builder title(String title) { this.title = Objects.requireNonNull(title); return this; }
        public Builder lines(String... lines) { this.lines = List.of(lines); return this; }
        public Builder validator(TabelaValidation validation) { this.validation = Objects.requireNonNull(validation); return this; }
        public Builder onComplete(Function<TabelaResult, TabelaAction> complete) { this.complete = Objects.requireNonNull(complete); return this; }
        public Builder onCancel(Function<TabelaResult, TabelaAction> cancel) { this.cancel = Objects.requireNonNull(cancel); return this; }
        public Builder timeout(Duration timeout) { this.timeout = Objects.requireNonNull(timeout); return this; }
        public TabelaPrompt build() {
            if (lines.size() != 4) throw new IllegalArgumentException("Tabela ekranı tam olarak 4 satır ister");
            return new TabelaPrompt(this);
        }
    }
}
