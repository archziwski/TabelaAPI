package dev.tabelaapi.platform;

import dev.tabelaapi.api.TabelaPrompt;
import org.bukkit.entity.Player;

public interface SignTransport {
    void open(Player player, TabelaPrompt prompt);
    void close(Player player);
}
