package dev.tabelaapi.plugin;

import dev.tabelaapi.platform.BukkitMainThreadScheduler;
import dev.tabelaapi.platform.TabelaAPI;
import dev.tabelaapi.platform.TabelaBukkitService;
import dev.tabelaapi.platform.TabelaSettings;
import dev.tabelaapi.platform.TabelaScheduler;
import dev.tabelaapi.platform.folia.FoliaSchedulerGuard;
import dev.tabelaapi.platform.paper.PaperSignTransport;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class TabelaPlugin extends JavaPlugin {
    private TabelaBukkitService service;
    private PaperSignTransport transport;
    private TabelaMessages messages;
    private boolean debug;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        messages = loadMessages();
        transport = new PaperSignTransport(this);
        service = new TabelaBukkitService(this, transport, TabelaSettings.from(getConfig()), scheduler());
        TabelaAPI.install(service);

        TabelaCommand command = new TabelaCommand(this);
        PluginCommand tabela = getCommand("tabela");
        if (tabela != null) {
            tabela.setExecutor(command);
            tabela.setTabCompleter(command);
        }
        getServer().getPluginManager().registerEvents(new TabelaListener(service), this);
        getLogger().info("TabelaAPI sanal tabela transport'u etkin.");
    }

    @Override
    public void onDisable() {
        if (service != null) service.shutdown();
        TabelaAPI.reset();
        if (transport != null) transport.shutdown();
    }

    /** Re-reads config.yml and messages without restarting the server. */
    public void reload() {
        reloadConfig();
        messages = loadMessages();
        if (service != null) service.settings(TabelaSettings.from(getConfig()));
    }

    public TabelaBukkitService service() { return service; }

    public TabelaMessages messages() { return messages; }

    public boolean debug() { return debug; }

    public void toggleDebug() { debug = !debug; }

    private TabelaMessages loadMessages() {
        return TabelaMessages.load(this, getConfig().getString("language", "tr_TR"));
    }

    private static TabelaScheduler scheduler() {
        return isFolia() ? FoliaSchedulerGuard.instance() : BukkitMainThreadScheduler.instance();
    }

    private static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException exception) {
            return false;
        }
    }
}
