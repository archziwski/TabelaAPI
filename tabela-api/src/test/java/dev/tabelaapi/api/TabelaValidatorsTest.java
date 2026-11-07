package dev.tabelaapi.api;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class TabelaValidatorsTest {
    @Test void supports_turkish_text_and_legacy_colors() {
        assertThat(TabelaText.normalize(" &aÇağrı İsim ")).isEqualTo("Çağrı İsim");
    }
    @Test void validates_player_names() {
        assertThat(TabelaValidators.playerName().validate("Oyuncu_123").valid()).isTrue();
        assertThat(TabelaValidators.playerName().validate("çok-uzun-isim").valid()).isFalse();
    }
}
