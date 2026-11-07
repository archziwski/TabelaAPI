package dev.tabelaapi.core;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TabelaRateLimiter {
    private final Map<UUID, Long> lastOpen = new ConcurrentHashMap<>();
    private final long intervalMillis;
    public TabelaRateLimiter(Duration interval) { intervalMillis = interval.toMillis(); }
    public boolean allow(UUID playerId) {
        long now = System.currentTimeMillis();
        Long previous = lastOpen.put(playerId, now);
        return previous == null || now - previous >= intervalMillis;
    }
    public void clear(UUID playerId) { lastOpen.remove(playerId); }
}
