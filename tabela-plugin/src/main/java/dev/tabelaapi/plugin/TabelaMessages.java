package dev.tabelaapi.plugin;

import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/** Command messages loaded from {@code messages_<language>.yml}, with Turkish as the fallback. */
@SuppressWarnings("deprecation")
final class TabelaMessages {
    private static final String FALLBACK = "messages_tr_TR.yml";

    private final YamlConfiguration config;
    private final String prefix;

    private TabelaMessages(YamlConfiguration config) {
        this.config = config;
        this.prefix = color(config.getString("prefix", ""));
    }

    static TabelaMessages load(TabelaPlugin plugin, String language) {
        YamlConfiguration config = read(plugin, "messages_" + language + ".yml");
        if (config == null) config = read(plugin, FALLBACK);
        if (config == null) config = new YamlConfiguration();
        return new TabelaMessages(config);
    }

    /** Returns the raw message, or an empty string when the key is missing. */
    String get(String key) {
        String value = config.getString(key, "");
        return color(value);
    }

    /** Returns the message with the configured prefix in front. */
    String message(String key) {
        return prefix + get(key);
    }

    private static YamlConfiguration read(TabelaPlugin plugin, String resource) {
        InputStream stream = plugin.getResource(resource);
        if (stream == null) return null;
        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            YamlConfiguration config = new YamlConfiguration();
            config.load(reader);
            return config;
        } catch (IOException | InvalidConfigurationException exception) {
            plugin.getLogger().warning("Mesaj dosyası okunamadı: " + resource + " (" + exception.getMessage() + ")");
            return null;
        }
    }

    private static String color(String value) {
        return ChatColor.translateAlternateColorCodes('&', value);
    }
}
