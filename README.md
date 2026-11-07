# TabelaAPI

Türk Minecraft sunucuları için paket tabanlı, geçici tabela giriş ekranı API’si.

TabelaAPI gerçek dünyaya tabela bloğu koymaz. Ekran yalnızca ilgili oyuncuya gösterilir; diğer oyuncular tabela görmez ve dünya verisi değişmez.

## Durum

Proje erken geliştirme aşamasındadır. API, oturum yönetimi, Türkçe doğrulayıcılar, plugin komutları, Folia scheduler sınırı ve Velocity bridge sözleşmesi hazırdır. Minecraft sürümüne özel packet transport adapter’ları ayrı modüllerde geliştirilmektedir.

## Örnek kullanım

```java
TabelaAPI.open(player, TabelaPrompt.builder()
    .title("Oyuncu adı")
    .lines("İsmini yaz", "", "", "")
    .validator(TabelaValidators.playerName())
    .onComplete(result -> TabelaAction.close())
    .build());
```

## İlkeler

- SignGUI’den kod veya dosya kopyalanmamıştır.
- Paper birincil platformdur; Folia scheduler kuralları baştan uygulanır.
- Oyuncu girdisi güvenilmez kabul edilir.
- Varsayılan mesajlar Türkçedir.
- API ve plugin sınırları birbirinden ayrıdır.

## Geliştirme

```bash
./gradlew test
./gradlew build
```

## Lisans

MIT. Ayrıntı için [LICENSE](LICENSE) dosyasına bakın.
