package net.earthcomputer.clientcommands;

import net.earthcomputer.clientcommands.features.CommandExecutionCustomPayload;
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
    private static int tickCount;

    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();

    public ClientCommandsNeoForge(IEventBus modBus) {
        LOGGER.info("[clientcommands] constructing mod entrypoint");
        new ClientCommands().onInitializeClient();
        new ClientCommandsServer().onInitialize();

        modBus.addListener(net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent.class, event -> {
            event.registrar("1").playToServer(CommandExecutionCustomPayload.TYPE, CommandExecutionCustomPayload.CODEC, (payload, context) -> {});
            ClientCommandsServer.registerPayloads(event);
        });

        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Pre.class, event -> {
            if (tickCount++ == 0) {
                LOGGER.info("[clientcommands] first client tick, level={}",
                    Minecraft.getInstance().level != null);
                net.earthcomputer.clientcommands.platform.PlatformSelfCheck.runOnce();
            }
            ClientCommands.setupScrambleWindowTitle();
        });
        NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.client.event.RegisterClientCommandsEvent.class,
            net.earthcomputer.clientcommands.platform.ClientCommandRegistrar::register);
    }
}
