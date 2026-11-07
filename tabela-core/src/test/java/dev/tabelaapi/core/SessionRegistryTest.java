package dev.tabelaapi.core;

import dev.tabelaapi.api.TabelaSession;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class SessionRegistryTest {
    @Test void keeps_one_session_per_player() {
        SessionRegistry registry = new SessionRegistry();
        UUID player = UUID.randomUUID();
        registry.put(new TabelaSession(UUID.randomUUID(), player, Instant.now()));
        registry.put(new TabelaSession(UUID.randomUUID(), player, Instant.now()));
        assertThat(registry.size()).isOne();
    }
}
