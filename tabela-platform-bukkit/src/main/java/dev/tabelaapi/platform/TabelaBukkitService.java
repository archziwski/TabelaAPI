package dev.tabelaapi.platform;

import dev.tabelaapi.api.TabelaAction;
import dev.tabelaapi.api.TabelaCancelReason;
import dev.tabelaapi.api.TabelaLineValidation;
import dev.tabelaapi.api.TabelaOpenException;
import dev.tabelaapi.api.TabelaPrompt;
import dev.tabelaapi.api.TabelaResult;
import dev.tabelaapi.api.TabelaSession;
import dev.tabelaapi.api.TabelaValidationResult;
import dev.tabelaapi.core.SessionRegistry;
import dev.tabelaapi.core.TabelaRateLimiter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Owns sign sessions: rate limiting, timeouts, validation and cleanup.
 *
 * <p>The service only talks to Bukkit's {@link Player} and the {@link SignTransport} SPI, so it runs
 * unchanged on Paper, Spigot and Folia. Scheduling is delegated to a {@link TabelaScheduler}.</p>
 */
public final class TabelaBukkitService {
    private final Plugin plugin;
    private final SignTransport transport;
    private final TabelaScheduler scheduler;
    private final SessionRegistry sessions = new SessionRegistry();
    private final ConcurrentMap<UUID, Long> rounds = new ConcurrentHashMap<>();
    private volatile TabelaSettings settings;
    private volatile TabelaRateLimiter rateLimiter;

    public TabelaBukkitService(Plugin plugin, SignTransport transport) {
        this(plugin, transport, TabelaSettings.defaults(), BukkitMainThreadScheduler.instance());
    }

    public TabelaBukkitService(Plugin plugin, SignTransport transport, TabelaSettings settings, TabelaScheduler scheduler) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.transport = Objects.requireNonNull(transport, "transport");
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
        this.settings = Objects.requireNonNull(settings, "settings");
        this.rateLimiter = new TabelaRateLimiter(settings.rateLimit());
    }

    /** Applies new limits, for example after {@code /tabela reload}. */
    public void settings(TabelaSettings newSettings) {
        this.settings = Objects.requireNonNull(newSettings, "settings");
        this.rateLimiter = new TabelaRateLimiter(newSettings.rateLimit());
    }

    public TabelaSettings settings() { return settings; }

    public TabelaSession open(Player player, TabelaPrompt prompt) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(prompt, "prompt");
        if (!rateLimiter.allow(player.getUniqueId())) {
            throw new TabelaOpenException("Tabela ekranını çok sık açıyorsun.");
        }
        cancel(player, TabelaCancelReason.REPLACED);
        if (sessions.size() >= settings.maxActiveSessions()) {
            throw new TabelaOpenException("Aynı anda en fazla " + settings.maxActiveSessions() + " tabela açılabilir.");
        }
        TabelaSession session = new TabelaSession(UUID.randomUUID(), player.getUniqueId(), Instant.now());
        sessions.put(new SessionRegistry.Entry(session, prompt));
        try {
            show(player, session, prompt);
        } catch (RuntimeException exception) {
            discard(player);
            throw exception;
        }
        return session;
    }

    /** Closes the player's session and reports the given reason to its cancel callback. */
    public void cancel(Player player, TabelaCancelReason reason) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(reason, "reason");
        SessionRegistry.Entry entry = sessions.remove(player.getUniqueId());
        rounds.remove(player.getUniqueId());
        if (entry == null) return;
        try {
            transport.close(player);
        } catch (RuntimeException ignored) {
            // The player may already be disconnecting; the session is gone either way.
        }
        entry.prompt().cancel().apply(new TabelaResult(entry.session(), TabelaPrompt.normalizeLines(List.of())));
    }

    /** Closes the player's session without invoking the cancel callback. */
    public void close(Player player) {
        Objects.requireNonNull(player, "player");
        discard(player);
    }

    /** Cancels every open session; call from {@code onDisable}. */
    public void shutdown() {
        for (Map.Entry<UUID, SessionRegistry.Entry> active : sessions.entries().entrySet()) {
            Player player = plugin.getServer().getPlayer(active.getKey());
            if (player != null) {
                cancel(player, TabelaCancelReason.SERVER_SHUTDOWN);
                continue;
            }
            SessionRegistry.Entry entry = sessions.remove(active.getKey());
            rounds.remove(active.getKey());
            if (entry != null) {
                entry.prompt().cancel().apply(new TabelaResult(entry.session(), TabelaPrompt.normalizeLines(List.of())));
            }
        }
        rounds.clear();
    }

    public int activeSessions() { return sessions.size(); }

    private void show(Player player, TabelaSession session, TabelaPrompt prompt) {
        long round = rounds.merge(player.getUniqueId(), 1L, Long::sum);
        transport.open(player, prompt, lines -> complete(player, session, prompt, round, lines));
        scheduler.runLater(plugin, player, timeoutOf(prompt), () -> expire(player, session, round));
    }

    private void complete(Player player, TabelaSession session, TabelaPrompt prompt, long round, String[] lines) {
        if (!isCurrent(player, session) || !isRound(player, round)) return;
        List<String> values = normalize(lines);
        Optional<TabelaValidationResult> failure = TabelaLineValidation.firstFailure(prompt, values, settings.maxLineLength());
        if (failure.isPresent()) {
            player.sendMessage(failure.get().message());
            if (settings.reopenOnValidationError()) retry(player, session, prompt);
            else cancel(player, TabelaCancelReason.FAILED);
            return;
        }
        TabelaResult result = new TabelaResult(session, values);
        TabelaAction action = prompt.complete().apply(result);
        if (action == null) {
            discard(player);
            return;
        }
        apply(player, prompt, action);
    }

    private void apply(Player player, TabelaPrompt prompt, TabelaAction action) {
        if (action instanceof TabelaAction.Close) {
            discard(player);
            return;
        }
        if (action instanceof TabelaAction.Message message) {
            player.sendMessage(message.text());
            discard(player);
            return;
        }
        if (action instanceof TabelaAction.Reopen reopen) {
            SessionRegistry.Entry entry = sessions.get(player.getUniqueId());
            if (entry == null) return;
            TabelaPrompt next = prompt.withLines(reopen.lines());
            sessions.put(new SessionRegistry.Entry(entry.session(), next));
            retry(player, entry.session(), next);
        }
    }

    private void retry(Player player, TabelaSession session, TabelaPrompt prompt) {
        try {
            show(player, session, prompt);
        } catch (RuntimeException exception) {
            cancel(player, TabelaCancelReason.FAILED);
        }
    }

    private void expire(Player player, TabelaSession session, long round) {
        if (!isRound(player, round) || !isCurrent(player, session)) return;
        cancel(player, TabelaCancelReason.TIMEOUT);
    }

    private void discard(Player player) {
        SessionRegistry.Entry entry = sessions.remove(player.getUniqueId());
        rounds.remove(player.getUniqueId());
        if (entry != null) transport.close(player);
    }

    private Duration timeoutOf(TabelaPrompt prompt) {
        return prompt.explicitTimeout().orElse(settings.timeout());
    }

    private boolean isCurrent(Player player, TabelaSession session) {
        SessionRegistry.Entry entry = sessions.get(player.getUniqueId());
        return entry != null && entry.session().equals(session);
    }

    private boolean isRound(Player player, long round) {
        Long current = rounds.get(player.getUniqueId());
        return current != null && current == round;
    }

    private static List<String> normalize(String[] lines) {
        List<String> values = new ArrayList<>(TabelaPrompt.LINE_COUNT);
        for (int line = 0; line < TabelaPrompt.LINE_COUNT; line++) {
            values.add(lines != null && line < lines.length && lines[line] != null ? lines[line] : "");
        }
        return List.copyOf(values);
    }
}
