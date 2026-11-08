package dev.tabelaapi.api;

import java.util.List;

public sealed interface TabelaAction permits TabelaAction.Close, TabelaAction.Reopen, TabelaAction.Message {
    record Close() implements TabelaAction { }

    /** Reopens the editor with the given lines; missing lines are padded with empty strings. */
    record Reopen(List<String> lines) implements TabelaAction {
        public Reopen { lines = TabelaPrompt.normalizeLines(List.copyOf(lines)); }
    }

    record Message(String text) implements TabelaAction { }

    static Close close() { return new Close(); }
    static Reopen reopen(String... lines) { return new Reopen(List.of(lines)); }
    static Message message(String text) { return new Message(text); }
}
