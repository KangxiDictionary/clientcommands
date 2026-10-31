package net.earthcomputer.clientcommands.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.earthcomputer.clientcommands.platform.CClientCommandSource;

import static net.earthcomputer.clientcommands.platform.ClientCommands.*;

// Might be implemented later
public class PlayerInfoCommand {
    public static void register(CommandDispatcher<CClientCommandSource> dispatcher) {
        LiteralCommandNode<CClientCommandSource> cplayerinfo = dispatcher.register(literal("cplayerinfo"));
    }
}
