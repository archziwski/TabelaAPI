package dev.tabelaapi.core;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TabelaRateLimiterTest {
    @Test
    void allows_the_first_open_and_denies_an_immediate_second_one() {
        TabelaRateLimiter limiter = new TabelaRateLimiter(Duration.ofSeconds(5));
        UUID player = UUID.randomUUID();

        assertThat(limiter.allow(player)).isTrue();
        assertThat(limiter.allow(player)).isFalse();
    }

    @Test
    void allows_again_after_the_interval() throws InterruptedException {
        TabelaRateLimiter limiter = new TabelaRateLimiter(Duration.ofMillis(30));
        UUID player = UUID.randomUUID();

        assertThat(limiter.allow(player)).isTrue();
        Thread.sleep(60);

        assertThat(limiter.allow(player)).isTrue();
    }

    @Test
    void tracks_players_separately() {
        TabelaRateLimiter limiter = new TabelaRateLimiter(Duration.ofSeconds(5));

        assertThat(limiter.allow(UUID.randomUUID())).isTrue();
        assertThat(limiter.allow(UUID.randomUUID())).isTrue();
    }
}
