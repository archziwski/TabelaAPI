# Değişiklik günlüğü

Bu dosya [Keep a Changelog](https://keepachangelog.com/tr/1.1.0/) biçimini izler ve
[Semantic Versioning](https://semver.org/lang/tr/) kullanır.

## [0.1.2] - 2026-11-08

### Düzeltildi

- Sanal tabela paketi artık dünya (world) eşleşmesini de kontrol ediyor; farklı dünyadaki aynı
  koordinat yanlış tabelayla eşleşmiyor.
- Eksik veya `null` gelen tabela satırları boş dize ile tamamlanıyor; `NullPointerException` oluşmuyor.
- Satırlar API seviyesinde her zaman dört satıra tamamlanıyor; `TabelaAction.Reopen` dörtten az
  satırla artık istisna atmıyor.
- Doğrulama hatasından sonra ekran yeniden açılırken zaman aşımı yeniden kuruluyor; her tur kendi
  zamanlayıcısını taşıyor.
- Satır başına doğrulayıcı desteği eklendi (`TabelaPrompt.Builder#validator(int, TabelaValidation)`).
- Rate limit iptal sonrası sıfırlanmıyor; artık tek ve tutarlı bir politika uygulanıyor.
- Oyuncu çıkışında oturum `DISCONNECTED` nedeniyle iptal ediliyor ve callback çalıştırılıyor.
- Plugin kapanışında açık oturumlar iptal ediliyor ve statik `TabelaAPI` servisi sıfırlanıyor.

### Eklendi

- `config.yml` değerleri (zaman aşımı, satır uzunluğu, oturum sayısı, rate limit, doğrulama hatasında
  yeniden açma, dil) çalışma zamanında okunuyor.
- Komut mesajları `messages_<language>.yml` dosyasından yükleniyor.
- `TabelaScheduler` SPI ve Paper/Bukkit ile Folia uygulamaları; servis katmanı platforma özgü
  scheduler çağrısı içermiyor.
- `TabelaLineValidation` ile doğrulama döngüsü test edilebilir hale getirildi.

## [0.1.1] - 2026-11-07

Sanal tabela transport'u Paper ve Folia için tamamlandı.

## [0.1.0-alpha] - 2026-11-07

İlk deneysel sürüm: PacketEvents tabanlı sanal tabela transport’u, oturum modeli, doğrulayıcılar ve
örnek plugin.
