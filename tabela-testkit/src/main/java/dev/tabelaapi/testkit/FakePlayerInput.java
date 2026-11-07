package dev.tabelaapi.testkit;

import dev.tabelaapi.api.TabelaResult;
import dev.tabelaapi.api.TabelaSession;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class FakePlayerInput {
    private FakePlayerInput() { }
    public static TabelaResult result(String... lines) { return new TabelaResult(new TabelaSession(UUID.randomUUID(), UUID.randomUUID(), Instant.now()), List.of(lines)); }
}
