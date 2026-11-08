# TabelaAPI

Türk Minecraft sunucuları için, oyunculara geçici sanal tabela girişi açmayı amaçlayan Java API/plugin projesi.

> **Geliştirme durumu:** deneysel. Paper/Folia packet transport’u PacketEvents üzerinden eklendi; gerçek sunucuda Folia 26.2 entegrasyon testi henüz yapılmadı. Canlı sistemde kullanmadan önce test sunucusunda doğrulayın.

## Proje

TabelaAPI geliştiricilerin bir oyuncudan satır satır metin istemesini sağlar. Geçici tabela görüntüsü yalnızca ilgili oyuncuya gönderilen packet’lerle oluşturulur; dünyaya tabela bloğu koyulmaz. Tabela girişi PacketEvents kullanır.

## Platform durumu

- Java 21 ile derlenir; bu nedenle sunucu da Java 21 çalıştırmalıdır.
- Desteklenen minimum sürüm Paper/Folia 1.20.6’dır (`api-version: 1.20`).
- Paper/Folia scheduler ve sanal tabela transport’u kaynak kodda bulunur; servis katmanı platforma özgü scheduler çağrısı içermez.
- PacketEvents 2.13+ sunucu plugin’i ayrıca kurulmalıdır (Folia 26.2 için 2.13.0).
- Bukkit/Spigot, Velocity ve 1.20.6 öncesi Minecraft sürümleri için üretime hazır adapter’lar henüz tamamlanmadı. Spigot’ta sanal tabela transport’u bulunmadığından plugin yüklenmez.
- API, oturum modelleri, doğrulayıcılar ve örnek plugin kodu geliştirme aşamasındadır.

## Yapılandırma

`config.yml` içindeki `timeout-seconds`, `max-line-length`, `max-active-sessions`, `rate-limit-millis`,
`reopen-on-validation-error` ve `language` değerleri çalışma zamanında okunur; `/tabela reload`
ayarları yeniden yükler. Komut mesajları `messages_<language>.yml` dosyasından gelir ve dosya yoksa
`messages_tr_TR.yml` kullanılır.

## Kaynak koddan derleme

Java 21 kurulu bir ortamda:

```bash
./gradlew build
```

Windows:

```powershell
./gradlew.bat build
```

Testleri ayrıca çalıştırmak için `./gradlew test` kullanın. Build’den sonra plugin paketi `tabela-plugin/build/libs/TabelaAPI-*.jar` yolunda oluşur. `tabela-platform-folia-*.jar` gibi alt modül JAR’ları tek başlarına plugin değildir ve `plugins/` klasörüne konmamalıdır.

Kurulumda PacketEvents plugin’i ve yalnızca `tabela-plugin/build/libs/TabelaAPI-*.jar` dosyasını `plugins/` klasörüne koyun. `tabela-platform-folia` ayrı bir plugin değildir. CaptchaPlus kullanıyorsanız CaptchaPlus JAR’ını da ekleyin. PacketEvents, TabelaAPI, CaptchaPlus sırası plugin bağımlılıkları tarafından yönetilir.

## API taslağı

```java
TabelaAPI.open(player, TabelaPrompt.builder()
    .title("Oyuncu adı")
    .lines("İsmini yaz", "", "", "")
    .validator(0, TabelaValidators.playerName())
    .onComplete(result -> TabelaAction.close())
    .build());
```

## Geliştirme

- [CONTRIBUTING.md](CONTRIBUTING.md): yerel geliştirme ve PR süreci
- [SECURITY.md](SECURITY.md): güvenlik açığı bildirimi
- [CHANGELOG.md](CHANGELOG.md): sürüm geçmişi
- [Code of Conduct](CODE_OF_CONDUCT.md)
- [Issues](https://github.com/archziwski/TabelaAPI/issues)
- [Releases](https://github.com/archziwski/TabelaAPI/releases)

## Lisans

MIT. Ayrıntılar için [LICENSE](LICENSE) dosyasına bakın.
