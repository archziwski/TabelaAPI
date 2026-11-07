package dev.tabelaapi.plugin;

import dev.tabelaapi.api.TabelaPrompt;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import java.util.List;

@SuppressWarnings("deprecation")
public final class TabelaCommand implements CommandExecutor, TabCompleter {
    private final TabelaPlugin plugin;
    public TabelaCommand(TabelaPlugin plugin) { this.plugin = plugin; }
    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("version")) { sender.sendMessage(color("&8[&6Tabela&8] &fTabelaAPI &6" + plugin.getDescription().getVersion())); return true; }
        if (args[0].equalsIgnoreCase("reload")) { if (!sender.hasPermission("tabela.reload")) return deny(sender); plugin.reloadConfig(); sender.sendMessage(color("&aAyarlar yenilendi.")); return true; }
        if (args[0].equalsIgnoreCase("debug")) { if (!sender.hasPermission("tabela.debug")) return deny(sender); plugin.toggleDebug(); sender.sendMessage(color(plugin.debug() ? "&aDebug modu açıldı." : "&eDebug modu kapatıldı.")); return true; }
        if (args[0].equalsIgnoreCase("test") && sender instanceof Player player) { if (!sender.hasPermission("tabela.test")) return deny(sender); sender.sendMessage(color("&aTest tabela ekranı açılıyor.")); plugin.service().open(player, TabelaPrompt.builder().title("TabelaAPI Test").lines("Bir şey yaz", "", "", "").build()); return true; }
        sender.sendMessage(color("&cKullanım: /tabela <reload|debug|test|version>")); return true;
    }
    private boolean deny(CommandSender sender) { sender.sendMessage(color("&cBu komut için yetkin yok.")); return true; }
    private String color(String value) { return ChatColor.translateAlternateColorCodes('&', value); }
    @Override public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) { return args.length == 1 ? List.of("reload", "debug", "test", "version") : List.of(); }
}
