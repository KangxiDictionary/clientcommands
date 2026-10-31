# clientcommands (NeoForge port)

Adds several useful client-side commands to Minecraft.

This repository is the **NeoForge port** of [Earthcomputer/clientcommands](https://github.com/Earthcomputer/clientcommands).
The command and algorithm code is kept from upstream; the loader-facing layer was rebuilt against NeoForge
(see [PORTING.md](PORTING.md) for the architecture and the verification protocol).

- Minecraft: **26.3**
- NeoForge: **26.3.0.99**
- Branch: `neoforge-clean`

## Installation

1. Install [NeoForge](https://neoforged.net/) for Minecraft 26.3.
2. Put `clientcommands-<version>.jar` into the `mods` folder.
   - The seedfinding libraries are bundled with NeoForge JarJar, so no extra downloads are required.

## Building

```sh
./gradlew build
```

The mod jar is written to `build/libs`. A Java 25 toolchain is required (Gradle can auto-detect one,
or point `org.gradle.java.installations.paths` at a JDK 25 installation).

For development, `./gradlew runClient` and `./gradlew runServer` start a dev environment.

## Credits

- **Earthcomputer** — original author of clientcommands.
- **KangxiDictionary** — NeoForge port.

Upstream project: <https://github.com/Earthcomputer/clientcommands>

## Contributing

To contribute translations, see the [translation contribution guidelines](docs/TRANSLATING.md).

For other contributions, see the [contribution guidelines](docs/CONTRIBUTING.md).

## Social

Upstream Discord: https://discord.gg/Jg7Bun7
Upstream Patreon: https://www.patreon.com/earthcomputer
