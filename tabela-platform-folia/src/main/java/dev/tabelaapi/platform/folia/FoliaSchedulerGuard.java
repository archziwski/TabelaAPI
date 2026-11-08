package dev.tabelaapi.platform.folia;

import dev.tabelaapi.platform.TabelaScheduler;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.time.Duration;
import java.util.Objects;

/**
 * Scheduler for Folia, where player work must run through the owning entity's scheduler.
 *
 * <p>Selected by the plugin when Folia is detected; it replaces the retired no-op guard with a real
 * {@link TabelaScheduler} implementation.</p>
 */
public final class FoliaSchedulerGuard implements TabelaScheduler {
    private static final FoliaSchedulerGuard INSTANCE = new FoliaSchedulerGuard();

    private FoliaSchedulerGuard() { }

    public static FoliaSchedulerGuard instance() { return INSTANCE; }

    @Override
    public void run(Plugin plugin, Player player, Runnable task) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(task, "task");
        player.getScheduler().run(plugin, scheduled -> task.run(), null);
    }

    @Override
    public void runLater(Plugin plugin, Player player, Duration delay, Runnable task) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(delay, "delay");
        Objects.requireNonNull(task, "task");
        player.getScheduler().runDelayed(plugin, scheduled -> task.run(), null, TabelaScheduler.toTicks(delay));
    }
}
