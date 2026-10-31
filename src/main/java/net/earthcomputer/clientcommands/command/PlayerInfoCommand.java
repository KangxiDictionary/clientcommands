package net.earthcomputer.clientcommands.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.earthcomputer.clientcommands.compat.CClientCommandSource;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.*;

// Might be implemented later
public class PlayerInfoCommand {
    public static void register(CommandDispatcher<CClientCommandSource> dispatcher) {
        LiteralCommandNode<CClientCommandSource> cplayerinfo = dispatcher.register(literal("cplayerinfo"));
    }
}
