package net.earthcomputer.clientcommands;

import net.earthcomputer.clientcommands.compat.CClientTickEvents;
import net.earthcomputer.clientcommands.server.ClientCommandsServer;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = ClientCommandsNeoForge.MOD_ID, dist = Dist.CLIENT)
public class ClientCommandsNeoForge {
    public static final String MOD_ID = "clientcommands";

    public ClientCommandsNeoForge(IEventBus modBus) {
        new ClientCommands().onInitializeClient();
        new ClientCommandsServer().onInitialize();

        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Pre.class, event ->
            CClientTickEvents.START_CLIENT_TICK.invoker().onStartTick(Minecraft.getInstance()));
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event ->
            CClientTickEvents.END_CLIENT_TICK.invoker().onEndTick(Minecraft.getInstance()));
    }
}
