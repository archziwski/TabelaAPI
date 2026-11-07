package dev.tabelaapi.platform.folia;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/** Centralizes the Folia rule: player operations run on the player's entity scheduler. */
public final class FoliaSchedulerGuard {
    private FoliaSchedulerGuard() { }
    public static void execute(Plugin plugin, Player player, Runnable task) { player.getScheduler().run(plugin, ignored -> task.run(), null); }
}
