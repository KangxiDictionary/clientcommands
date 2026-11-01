# Server behaviour

This NeoForge build of clientcommands is **client only**. No server-side component is shipped, and
none is required: install the jar in the client `mods` folder and every command runs locally.

## Commands that upstream gated behind a server install

Upstream's Fabric build refuses commands that could be abused (for example `/cfindblock`,
`/careastats`, `/csignsearch`, `/cfind`, `/cglow entities`, `/cgetdata entity`, `/cghostblock`) unless
the server also has the mod or the player is an operator. That opt-in exists to satisfy Modrinth's
content rules, not for technical reasons.

This port drops the opt-in entirely, per its "client first" goal:

* the commands are always registered and always attempt to run;
* the server does not need the mod, and nothing is sent to it;
* whatever the server decides to do with the resulting packets (if anything) is unchanged — the mod
  only changes what the client asks for.

## What that means in practice

* Singleplayer and LAN worlds: identical to upstream with the server mod installed.
* Multiplayer without the mod on the server: the commands still try; some of them may return no data
  because the information is not available client side, and normal server permission checks still
  apply to anything that touches the server.
* There is no "requires clientcommands on the server" error any more.

## If you want the gated behaviour back

Use the upstream Fabric build on both sides, or add your own permission gate. The client-side code
for this build deliberately has no knowledge of the server's installed mods.
