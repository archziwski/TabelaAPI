package dev.tabelaapi.api;

import java.util.regex.Pattern;

public final class TabelaText {
    private static final Pattern LEGACY = Pattern.compile("(?i)[&§][0-9a-fk-or]");
    private TabelaText() { }
    public static String stripLegacyColors(String value) { return LEGACY.matcher(value == null ? "" : value).replaceAll(""); }
    public static String normalize(String value) { return value == null ? "" : stripLegacyColors(value).trim(); }
}
