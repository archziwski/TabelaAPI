package dev.tabelaapi.api;

public class TabelaOpenException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public TabelaOpenException(String message) { super(message); }
    public TabelaOpenException(String message, Throwable cause) { super(message, cause); }
}
