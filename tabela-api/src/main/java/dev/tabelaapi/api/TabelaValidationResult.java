package dev.tabelaapi.api;

public record TabelaValidationResult(boolean valid, String message) {
    public static TabelaValidationResult success() { return new TabelaValidationResult(true, ""); }
    public static TabelaValidationResult invalid(String message) { return new TabelaValidationResult(false, message); }
}
