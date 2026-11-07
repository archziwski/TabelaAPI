package dev.tabelaapi.platform;

import dev.tabelaapi.api.TabelaOpenException;
import dev.tabelaapi.api.TabelaPrompt;
import org.bukkit.entity.Player;

public final class UnsupportedSignTransport implements SignTransport {
    @Override public void open(Player player, TabelaPrompt prompt) { throw new TabelaOpenException("Bu Minecraft sürümü için tabela paket taşıyıcısı etkin değil."); }
    @Override public void close(Player player) { }
}
