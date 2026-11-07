# TabelaAPI

A Java API and plugin project for collecting text from players through a temporary virtual sign screen on Turkish Minecraft servers.

> **Development status:** experimental. A Paper/Folia sign transport using PacketEvents is implemented; it has not yet been integration-tested on a live Folia 26.2 server. Verify it on a test server before live use.

## Project

TabelaAPI lets plugin developers request text lines from a player. A temporary sign view is sent only to that player through packets; no sign block is placed in the world. Sign input uses PacketEvents.

## Platform status

- Builds with Java 21.
- Paper/Folia scheduler support and virtual-sign transport are present in source.
- PacketEvents 2.13+ must also be installed as a server plugin (2.13.0 for Folia 26.2).
- Production-ready adapters for Bukkit/Spigot, Velocity, and legacy Minecraft versions are not complete.
- The API, session models, validators, and example plugin are under development.

## Build from source

With Java 21 installed:

```bash
./gradlew build
```

On Windows:

```powershell
./gradlew.bat build
```

Run tests with `./gradlew test`. The plugin package is written to `tabela-plugin/build/libs/TabelaAPI-*.jar`. Module jars such as `tabela-platform-folia-*.jar` are not plugins and must not be put in the server's `plugins/` folder.

Install PacketEvents and only the `TabelaAPI-*.jar` plugin package from `tabela-plugin/build/libs/`. Add CaptchaPlus as well if you use it. Plugin dependencies handle load order.

## API sketch

```java
TabelaAPI.open(player, TabelaPrompt.builder()
    .title("Player name")
    .lines("Enter your name", "", "", "")
    .validator(TabelaValidators.playerName())
    .onComplete(result -> TabelaAction.close())
    .build());
```

## Project links

- [Contributing](CONTRIBUTING.md)
- [Security policy](SECURITY.md)
- [Changelog](CHANGELOG.md)
- [Code of Conduct](CODE_OF_CONDUCT.md)
- [Issues](https://github.com/archziwski/TabelaAPI/issues)
- [Releases](https://github.com/archziwski/TabelaAPI/releases)

## License

MIT. See [LICENSE](LICENSE).
