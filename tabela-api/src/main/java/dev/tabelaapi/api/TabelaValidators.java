package dev.tabelaapi.api;

import java.util.regex.Pattern;

public final class TabelaValidators {
    private TabelaValidators() { }
    public static TabelaValidation notBlank() { return value -> TabelaText.normalize(value).isBlank() ? TabelaValidationResult.invalid("Bu alan boş bırakılamaz.") : TabelaValidationResult.success(); }
    public static TabelaValidation maxLength(int max) { return value -> TabelaText.normalize(value).length() > max ? TabelaValidationResult.invalid("En fazla " + max + " karakter kullanabilirsin.") : TabelaValidationResult.success(); }
    public static TabelaValidation regex(String expression, String message) {
        Pattern pattern = Pattern.compile(expression);
        return value -> pattern.matcher(TabelaText.normalize(value)).matches() ? TabelaValidationResult.success() : TabelaValidationResult.invalid(message);
    }
    public static TabelaValidation playerName() { return regex("[A-Za-z0-9_]{3,16}", "Geçerli bir Minecraft oyuncu adı yazmalısın."); }
    public static TabelaValidation integer() { return regex("-?[0-9]+", "Yalnızca tam sayı yazmalısın."); }
    public static TabelaValidation ipAddress() { return regex("(?:[0-9]{1,3}\\.){3}[0-9]{1,3}", "Geçerli bir IPv4 adresi yazmalısın."); }
    public static TabelaValidation url() { return regex("https?://.+", "Geçerli bir http veya https bağlantısı yazmalısın."); }
    public static TabelaValidation all(TabelaValidation... validations) { return value -> { for (TabelaValidation validation : validations) { TabelaValidationResult result = validation.validate(value); if (!result.valid()) return result; } return TabelaValidationResult.success(); }; }
}
