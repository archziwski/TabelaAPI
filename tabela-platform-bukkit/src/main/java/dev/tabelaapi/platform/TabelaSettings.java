package dev.tabelaapi.platform;

import org.bukkit.configuration.ConfigurationSection;

import java.time.Duration;
import java.util.Objects;

/** Runtime limits applied by {@link TabelaBukkitService}. */
public record TabelaSettings(
    Duration timeout,
    int maxLineLength,
    int maxActiveSessions,
    Duration rateLimit,
    boolean reopenOnValidationError
) {
    public TabelaSettings {
        Objects.requireNonNull(timeout, "timeout");
        Objects.requireNonNull(rateLimit, "rateLimit");
        if (timeout.isNegative() || timeout.isZero()) {
            throw new IllegalArgumentException("timeout must be positive");
        }
        if (maxLineLength < 1) {
            throw new IllegalArgumentException("maxLineLength must be at least 1");
        }
        if (maxActiveSessions < 1) {
            throw new IllegalArgumentException("maxActiveSessions must be at least 1");
        }
        if (rateLimit.isNegative()) {
            throw new IllegalArgumentException("rateLimit must not be negative");
        }
    }

    public static TabelaSettings defaults() {
        return new TabelaSettings(Duration.ofSeconds(60), 64, 100, Duration.ofMillis(250), true);
    }

    /** Reads the settings from a config section, falling back to {@link #defaults()} values. */
    public static TabelaSettings from(ConfigurationSection section) {
        Objects.requireNonNull(section, "section");
        TabelaSettings defaults = defaults();
        return new TabelaSettings(
            Duration.ofSeconds(Math.max(1, section.getInt("timeout-seconds", (int) defaults.timeout().toSeconds()))),
            Math.max(1, section.getInt("max-line-length", defaults.maxLineLength())),
            Math.max(1, section.getInt("max-active-sessions", defaults.maxActiveSessions())),
            Duration.ofMillis(Math.max(0L, section.getLong("rate-limit-millis", defaults.rateLimit().toMillis()))),
            section.getBoolean("reopen-on-validation-error", defaults.reopenOnValidationError())
        );
    }
}
