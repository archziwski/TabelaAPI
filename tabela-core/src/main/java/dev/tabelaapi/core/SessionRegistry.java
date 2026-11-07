package dev.tabelaapi.core;

import dev.tabelaapi.api.TabelaSession;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SessionRegistry {
    private final Map<UUID, TabelaSession> sessions = new ConcurrentHashMap<>();
    public TabelaSession put(TabelaSession session) { return sessions.put(session.playerId(), session); }
    public TabelaSession remove(UUID playerId) { return sessions.remove(playerId); }
    public TabelaSession get(UUID playerId) { return sessions.get(playerId); }
    public int size() { return sessions.size(); }
    public void clear() { sessions.clear(); }
}
