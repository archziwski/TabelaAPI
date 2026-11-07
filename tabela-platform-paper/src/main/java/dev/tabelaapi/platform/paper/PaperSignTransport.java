package dev.tabelaapi.platform.paper;

import dev.tabelaapi.api.TabelaPrompt;
import dev.tabelaapi.platform.SignTransport;
import org.bukkit.entity.Player;

/** Packet boundary. Version-specific packet implementations belong in the legacy adapter module. */
public final class PaperSignTransport implements SignTransport {
    @Override public void open(Player player, TabelaPrompt prompt) { throw new UnsupportedOperationException("Paper packet adapter henüz sürüm modülüne bağlanmadı"); }
    @Override public void close(Player player) { }
}
