package net.earthcomputer.clientcommands.command;

import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.clientcommands.features.TwoPlayerGame;
import net.earthcomputer.clientcommands.compat.CClientCommandSource;

public class ChessCommand {
    public static void register(CommandDispatcher<CClientCommandSource> dispatcher) {
        dispatcher.register(TwoPlayerGame.CHESS_TYPE.createCommandTree());
    }
}
