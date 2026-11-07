package dev.tabelaapi.api;

import java.util.List;

public sealed interface TabelaAction permits TabelaAction.Close, TabelaAction.Reopen, TabelaAction.Message {
    record Close() implements TabelaAction { }
    record Reopen(List<String> lines) implements TabelaAction { public Reopen { lines = List.copyOf(lines); } }
    record Message(String text) implements TabelaAction { }

    static Close close() { return new Close(); }
    static Reopen reopen(String... lines) { return new Reopen(List.of(lines)); }
    static Message message(String text) { return new Message(text); }
}
