package dev.tabelaapi.core;

import dev.tabelaapi.api.TabelaPrompt;
import dev.tabelaapi.api.TabelaSession;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Keeps at most one active prompt per player. */
public final class SessionRegistry {
    /** A running session together with the prompt that is currently displayed for it. */
    public record Entry(TabelaSession session, TabelaPrompt prompt) {
        public Entry {
            if (session == null || prompt == null) throw new IllegalArgumentException("session and prompt are required");
        }
    }

    private final Map<UUID, Entry> sessions = new ConcurrentHashMap<>();

    public Entry put(Entry entry) { return sessions.put(entry.session().playerId(), entry); }

    public Entry remove(UUID playerId) { return sessions.remove(playerId); }

    public Entry get(UUID playerId) { return sessions.get(playerId); }

    public Map<UUID, Entry> entries() { return Map.copyOf(sessions); }

    public int size() { return sessions.size(); }

    public void clear() { sessions.clear(); }
}
