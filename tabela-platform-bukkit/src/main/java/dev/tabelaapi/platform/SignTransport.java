package dev.tabelaapi.platform;

import dev.tabelaapi.api.TabelaPrompt;
import org.bukkit.entity.Player;
import java.util.function.Consumer;

public interface SignTransport {
    void open(Player player, TabelaPrompt prompt, Consumer<String[]> result);
    void close(Player player);
}
