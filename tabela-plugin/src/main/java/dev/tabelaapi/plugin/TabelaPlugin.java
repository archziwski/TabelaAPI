package dev.tabelaapi.plugin;

import dev.tabelaapi.platform.TabelaBukkitService;
import dev.tabelaapi.platform.TabelaAPI;
import dev.tabelaapi.platform.paper.PaperSignTransport;
import org.bukkit.plugin.java.JavaPlugin;

public final class TabelaPlugin extends JavaPlugin {
    private TabelaBukkitService service;
    private PaperSignTransport transport;
    private boolean debug;
    @Override public void onEnable() {
        saveDefaultConfig();
        transport = new PaperSignTransport(this);
        service = new TabelaBukkitService(this, transport);
        TabelaAPI.install(service);
        TabelaCommand command = new TabelaCommand(this);
        getCommand("tabela").setExecutor(command);
        getCommand("tabela").setTabCompleter(command);
        getServer().getPluginManager().registerEvents(new TabelaListener(service), this);
        getLogger().info("TabelaAPI sanal tabela transport'u etkin.");
    }
    @Override public void onDisable() { if (transport != null) transport.shutdown(); }
    public TabelaBukkitService service() { return service; }
    public boolean debug() { return debug; }
    public void toggleDebug() { debug = !debug; }
}
