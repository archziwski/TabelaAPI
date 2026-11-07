package dev.tabelaapi.platform;

import dev.tabelaapi.api.*;
import dev.tabelaapi.core.SessionRegistry;
import dev.tabelaapi.core.TabelaRateLimiter;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class TabelaBukkitService {
    private final SignTransport transport;
    private final SessionRegistry sessions = new SessionRegistry();
    private final TabelaRateLimiter rateLimiter = new TabelaRateLimiter(java.time.Duration.ofMillis(250));
    private final ScheduledExecutorService timeoutExecutor;

    public TabelaBukkitService(SignTransport transport, ScheduledExecutorService timeoutExecutor) { this.transport = transport; this.timeoutExecutor = timeoutExecutor; }
    public TabelaSession open(Player player, TabelaPrompt prompt) {
        if (!rateLimiter.allow(player.getUniqueId())) throw new TabelaOpenException("Tabela ekranını çok sık açıyorsun.");
        close(player);
        TabelaSession session = new TabelaSession(UUID.randomUUID(), player.getUniqueId(), Instant.now());
        sessions.put(session);
        try { transport.open(player, prompt); } catch (RuntimeException exception) { sessions.remove(player.getUniqueId()); throw exception; }
        timeoutExecutor.schedule(() -> expire(player, session, prompt), prompt.timeout().toMillis(), TimeUnit.MILLISECONDS);
        return session;
    }
    public void complete(Player player, TabelaSession session, TabelaPrompt prompt, String... lines) {
        if (!isCurrent(player, session)) return;
        TabelaResult result = new TabelaResult(session, java.util.List.of(lines));
        TabelaValidationResult validation = prompt.validation().validate(TabelaText.normalize(result.line(0)));
        if (!validation.valid()) { transport.open(player, prompt); return; }
        apply(player, prompt.complete().apply(result));
    }
    public void cancel(Player player, TabelaCancelReason reason, TabelaPrompt prompt) {
        TabelaSession session = sessions.remove(player.getUniqueId());
        if (session != null) { transport.close(player); prompt.cancel().apply(new TabelaResult(session, java.util.List.of("", "", "", ""))); }
        rateLimiter.clear(player.getUniqueId());
    }
    public void close(Player player) { if (sessions.get(player.getUniqueId()) != null) transport.close(player); sessions.remove(player.getUniqueId()); }
    public int activeSessions() { return sessions.size(); }
    private void expire(Player player, TabelaSession session, TabelaPrompt prompt) { if (isCurrent(player, session)) cancel(player, TabelaCancelReason.TIMEOUT, prompt); }
    private boolean isCurrent(Player player, TabelaSession session) { return session.equals(sessions.get(player.getUniqueId())); }
    private void apply(Player player, TabelaAction action) {
        if (action instanceof TabelaAction.Close) close(player);
        else if (action instanceof TabelaAction.Reopen reopen) transport.open(player, TabelaPrompt.builder().lines(reopen.lines().toArray(String[]::new)).build());
        else if (action instanceof TabelaAction.Message message) player.sendMessage(message.text());
    }
}
