package dev.tabelaapi.api;

import java.util.List;
import java.util.Objects;

public record TabelaResult(TabelaSession session, List<String> lines) {
    public TabelaResult {
        Objects.requireNonNull(session, "session");
        lines = List.copyOf(lines);
    }

    public String line(int index) { return lines.get(index); }
    public String[] linesArray() { return lines.toArray(String[]::new); }
    public List<String> linesWithoutFormatting() {
        return lines.stream().map(TabelaText::stripLegacyColors).toList();
    }
}
