package dev.tabelaapi.platform;

import dev.tabelaapi.api.TabelaOpenException;
import dev.tabelaapi.api.TabelaPrompt;
import org.bukkit.entity.Player;
import java.util.function.Consumer;

public final class UnsupportedSignTransport implements SignTransport {
    @Override public void open(Player player, TabelaPrompt prompt, Consumer<String[]> result) { throw new TabelaOpenException("Tabela packet taşıyıcısı etkin değil."); }
    @Override public void close(Player player) { }
}
