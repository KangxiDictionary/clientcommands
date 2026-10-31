# NeoForge port notes

This branch (`neoforge-clean`) rebuilds the loader-facing layer from scratch while keeping the
upstream command and algorithm code. Read this before touching the platform code.

## Layout

| Path | Responsibility |
| --- | --- |
| `platform/` | The only place that talks to NeoForge. `CClientCommandSource` (interface), `ClientCommandRegistrar`, `SimpleClientCommandSource`, `PlatformSelfCheck`. |
| `mixin/platform/` | Makes NeoForge's `ClientCommandSourceStack` and `ClientSuggestionProvider` implement the mod's source interface, and carries flag/alias state. |
| `command/`, `features/`, `render/` | Upstream implementations. Logic is unchanged; only the source accessors were renamed. |
| `compat/` | Self-contained replacements for the small Fabric utilities upstream used (`CEvent`, `CNetworking`, `CPlatform`, tick/connection events). No Fabric imports remain. |
| `mixin/` | Only mixins that need Minecraft internals. |

## Command registration

Commands are declared with the same `CommandDispatcher<CClientCommandSource>` builder used upstream,
then merged into NeoForge's dispatcher:

1. NeoForge fires `RegisterClientCommandsEvent` on `NeoForge.EVENT_BUS` every time the client
   receives the server command tree (`ClientCommandHandler.mergeServerCommands`).
2. `ClientCommandRegistrar` registers the mod's commands once and copies the root children into the
   dispatcher carried by the event.
3. NeoForge copies that dispatcher into its static client dispatcher (used by `runCommand`) and
   merges the server commands on top. Completion uses `ClientSuggestionProvider`, execution uses
   `ClientCommandSourceStack`; both implement `CClientCommandSource`.

Do not reintroduce a private dispatcher for execution, and do not hook `sendCommand`.

## Access transformer

`src/main/resources/META-INF/accesstransformer.cfg` is the single source of truth. It is declared to
NeoForge in `build.gradle` (`accessTransformers.from`) so it applies in development as well as at
runtime.

* fields: `public <class> <name>`
* methods: `public <class> <name>(<params>)<ret>` — the return type is mandatory for the parser.
* widening a method that an inaccessible subclass overrides will break NFRT's recompile step; widen
  the subtype (or drop the entry) instead.

## Configuration

Settings are a NeoForge `ModConfigSpec` (`platform/ConfigSpecs`) registered as a client config, so
the file is the standard `config/clientcommands-client.toml`. `Configs` keeps the mutable static
fields used by the rest of the mod and syncs them from the spec on load/reload; `/cconfig` writes
through to the spec. The built-in `ConfigurationScreen` is registered as the in-game config GUI.

## Dependencies

The seedfinding libraries are bundled with NeoForge JarJar through the `jarJar` configuration.
Transitive libraries must be listed explicitly: JarJar requires Maven coordinates for every embedded
jar and rejects bare file dependencies. Do not shade dependencies into the main jar (it used to
shadow Kotlin and other classes).

## Verification

`PlatformSelfCheck` runs once on the first client tick and logs whether the access transformer was
applied and whether both command sources expose the interface. Command registration logs
`registered N root commands` and `merged N root commands into the client dispatcher`, and every
command is registered in isolation so one failure cannot hide the rest.

`./gradlew runClient` and `./gradlew runServer` launch the development client and server.

Render code cannot be verified headlessly: it is reached through the `LevelRenderer.submitFeatures`
injection, and logs once when a submission actually happens.
