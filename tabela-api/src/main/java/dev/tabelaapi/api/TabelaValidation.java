package dev.tabelaapi.api;

@FunctionalInterface
public interface TabelaValidation {
    TabelaValidationResult validate(String value);

    static TabelaValidationResult ok() { return TabelaValidationResult.success(); }
    static TabelaValidationResult invalid(String message) { return TabelaValidationResult.invalid(message); }
}
