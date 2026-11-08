package dev.tabelaapi.core;

import dev.tabelaapi.api.TabelaPrompt;
import dev.tabelaapi.api.TabelaSession;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SessionRegistryTest {
    private static SessionRegistry.Entry entry(UUID player, TabelaPrompt prompt) {
        return new SessionRegistry.Entry(new TabelaSession(UUID.randomUUID(), player, Instant.now()), prompt);
    }

    @Test
    void keeps_one_session_per_player() {
        SessionRegistry registry = new SessionRegistry();
        UUID player = UUID.randomUUID();
        TabelaPrompt first = TabelaPrompt.builder().title("bir").build();
        TabelaPrompt second = TabelaPrompt.builder().title("iki").build();

        registry.put(entry(player, first));
        registry.put(entry(player, second));

        assertThat(registry.size()).isOne();
        assertThat(registry.get(player).prompt().title()).isEqualTo("iki");
    }

    @Test
    void remove_returns_the_stored_entry() {
        SessionRegistry registry = new SessionRegistry();
        UUID player = UUID.randomUUID();
        TabelaPrompt prompt = TabelaPrompt.builder().build();
        registry.put(entry(player, prompt));

        assertThat(registry.remove(player).prompt()).isSameAs(prompt);
        assertThat(registry.get(player)).isNull();
    }

    @Test
    void entries_snapshot_reflects_sessions() {
        SessionRegistry registry = new SessionRegistry();
        registry.put(entry(UUID.randomUUID(), TabelaPrompt.builder().build()));

        assertThat(registry.entries()).hasSize(1);
        registry.clear();
        assertThat(registry.entries()).isEmpty();
    }
}
