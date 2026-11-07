package dev.tabelaapi.api;

import java.time.Instant;
import java.util.UUID;

public record TabelaSession(UUID id, UUID playerId, Instant openedAt) {
    public TabelaSession {
        if (id == null || playerId == null || openedAt == null) throw new IllegalArgumentException("session fields are required");
    }
}
