package dev.tabelaapi.platform;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.time.Duration;
import java.util.Objects;

/** Scheduler for Bukkit, Spigot and non-Folia Paper servers, where all player work runs on the main thread. */
public final class BukkitMainThreadScheduler implements TabelaScheduler {
    private static final BukkitMainThreadScheduler INSTANCE = new BukkitMainThreadScheduler();

    private BukkitMainThreadScheduler() { }

    public static BukkitMainThreadScheduler instance() { return INSTANCE; }

    @Override
    public void run(Plugin plugin, Player player, Runnable task) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(task, "task");
        if (!plugin.isEnabled()) return;
        plugin.getServer().getScheduler().runTask(plugin, () -> runIfOnline(player, task));
    }

    @Override
    public void runLater(Plugin plugin, Player player, Duration delay, Runnable task) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(delay, "delay");
        Objects.requireNonNull(task, "task");
        if (!plugin.isEnabled()) return;
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> runIfOnline(player, task), TabelaScheduler.toTicks(delay));
    }

    private static void runIfOnline(Player player, Runnable task) {
        if (player.isOnline()) task.run();
    }
}
