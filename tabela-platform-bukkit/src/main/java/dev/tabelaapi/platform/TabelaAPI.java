package dev.tabelaapi.platform;

import dev.tabelaapi.api.TabelaPrompt;
import dev.tabelaapi.api.TabelaSession;
import org.bukkit.entity.Player;

import java.util.Objects;

/** Public Bukkit-facing entry point. The plugin or host application supplies the service. */
public final class TabelaAPI {
    private static volatile TabelaBukkitService service;
    private TabelaAPI() { }
    public static void install(TabelaBukkitService newService) { service = Objects.requireNonNull(newService); }
    public static TabelaSession open(Player player, TabelaPrompt prompt) {
        TabelaBukkitService current = service;
        if (current == null) throw new IllegalStateException("TabelaAPI henüz başlatılmadı");
        return current.open(player, prompt);
    }
}
