package dev.tabelaapi.platform;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.time.Duration;

/**
 * Schedules player-bound work with the rules of the running server.
 *
 * <p>Paper and Spigot run everything on the main thread, while Folia requires the owning entity's
 * scheduler. Platform modules provide the matching implementation so the service stays free of
 * platform-specific scheduling calls.</p>
 */
public interface TabelaScheduler {
    /** Runs {@code task} on the thread that owns {@code player}. */
    void run(Plugin plugin, Player player, Runnable task);

    /** Runs {@code task} after {@code delay} on the thread that owns {@code player}. */
    void runLater(Plugin plugin, Player player, Duration delay, Runnable task);

    /** Converts a duration to whole server ticks, rounding up with a minimum of one tick. */
    static long toTicks(Duration delay) {
        long ticks = (delay.toMillis() + 49L) / 50L;
        return Math.max(1L, ticks);
    }
}
