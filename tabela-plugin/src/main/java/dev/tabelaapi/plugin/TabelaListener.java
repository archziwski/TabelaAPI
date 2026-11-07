package dev.tabelaapi.plugin;

import dev.tabelaapi.platform.TabelaBukkitService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public final class TabelaListener implements Listener {
    private final TabelaBukkitService service;
    public TabelaListener(TabelaBukkitService service) { this.service = service; }
    @EventHandler public void onQuit(PlayerQuitEvent event) { service.close(event.getPlayer()); }
}
