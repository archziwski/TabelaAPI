package dev.tabelaapi.plugin;

import dev.tabelaapi.api.TabelaOpenException;
import dev.tabelaapi.api.TabelaPrompt;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public final class TabelaCommand implements CommandExecutor, TabCompleter {
    private final TabelaPlugin plugin;

    public TabelaCommand(TabelaPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("version")) {
            String version = plugin.messages().get("version").replace("%version%", plugin.getPluginMeta().getVersion());
            sender.sendMessage(version);
            return true;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("tabela.reload")) return deny(sender);
            plugin.reload();
            sender.sendMessage(plugin.messages().message("reloaded"));
            return true;
        }
        if (args[0].equalsIgnoreCase("debug")) {
            if (!sender.hasPermission("tabela.debug")) return deny(sender);
            plugin.toggleDebug();
            sender.sendMessage(plugin.messages().message(plugin.debug() ? "debug-on" : "debug-off"));
            return true;
        }
        if (args[0].equalsIgnoreCase("test") && sender instanceof Player player) {
            if (!sender.hasPermission("tabela.test")) return deny(sender);
            return openTest(player);
        }
        sender.sendMessage(plugin.messages().message("usage"));
        return true;
    }

    private boolean openTest(Player player) {
        try {
            plugin.service().open(player, TabelaPrompt.builder()
                .title("TabelaAPI Test")
                .lines("Bir şey yaz", "", "", "")
                .build());
            player.sendMessage(plugin.messages().message("test-opened"));
        } catch (TabelaOpenException exception) {
            player.sendMessage(plugin.messages().message("open-failed").replace("%reason%", exception.getMessage()));
        }
        return true;
    }

    private boolean deny(CommandSender sender) {
        sender.sendMessage(plugin.messages().message("no-permission"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return args.length == 1 ? List.of("reload", "debug", "test", "version") : List.of();
    }
}
