package dev.tabelaapi.plugin;

import dev.tabelaapi.platform.TabelaBukkitService;
import dev.tabelaapi.platform.TabelaAPI;
import dev.tabelaapi.platform.UnsupportedSignTransport;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public final class TabelaPlugin extends JavaPlugin {
    private ScheduledExecutorService timeoutExecutor;
    private TabelaBukkitService service;
    private boolean debug;
    @Override public void onEnable() {
        saveDefaultConfig();
        timeoutExecutor = Executors.newSingleThreadScheduledExecutor(runnable -> { Thread thread = new Thread(runnable, "TabelaAPI-Timeouts"); thread.setDaemon(true); return thread; });
        service = new TabelaBukkitService(new UnsupportedSignTransport(), timeoutExecutor);
        TabelaAPI.install(service);
        TabelaCommand command = new TabelaCommand(this);
        getCommand("tabela").setExecutor(command);
        getCommand("tabela").setTabCompleter(command);
        getServer().getPluginManager().registerEvents(new TabelaListener(service), this);
        getLogger().info("TabelaAPI etkin: sürüm packet adapter'ı bekleniyor.");
    }
    @Override public void onDisable() { if (timeoutExecutor != null) timeoutExecutor.shutdownNow(); }
    public TabelaBukkitService service() { return service; }
    public boolean debug() { return debug; }
    public void toggleDebug() { debug = !debug; }
}
