package dev.tabelaapi.plugin;

import dev.tabelaapi.platform.TabelaBukkitService;
import dev.tabelaapi.platform.TabelaAPI;
import dev.tabelaapi.platform.UnsupportedSignTransport;
import org.bukkit.plugin.java.JavaPlugin;

public final class TabelaPlugin extends JavaPlugin {
    private TabelaBukkitService service;
    private boolean debug;
    @Override public void onEnable() {
        saveDefaultConfig();
        service = new TabelaBukkitService(this, new UnsupportedSignTransport());
        TabelaAPI.install(service);
        TabelaCommand command = new TabelaCommand(this);
        getCommand("tabela").setExecutor(command);
        getCommand("tabela").setTabCompleter(command);
        getServer().getPluginManager().registerEvents(new TabelaListener(service), this);
        getLogger().info("TabelaAPI etkin: sürüm packet adapter'ı bekleniyor.");
    }
    @Override public void onDisable() { }
    public TabelaBukkitService service() { return service; }
    public boolean debug() { return debug; }
    public void toggleDebug() { debug = !debug; }
}
