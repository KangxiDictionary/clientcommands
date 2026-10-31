package net.earthcomputer.clientcommands.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.clientcommands.platform.CClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

import static net.earthcomputer.clientcommands.platform.ClientCommands.*;

public class ShrugCommand {

    public static void register(CommandDispatcher<CClientCommandSource> dispatcher) {
        dispatcher.register(literal("cshrug")
            .executes(ctx -> shrug(ctx.getSource())));
    }

    private static int shrug(CClientCommandSource source) {
        ClientPacketListener packetListener = Minecraft.getInstance().getConnection();
        if (packetListener != null) {
            packetListener.sendChat("¯\\_(ツ)_/¯");
        }
        return Command.SINGLE_SUCCESS;
    }

}
