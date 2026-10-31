package net.earthcomputer.clientcommands.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.earthcomputer.clientcommands.features.Relogger;
import net.earthcomputer.clientcommands.platform.CClientCommandSource;
import net.minecraft.network.chat.Component;

import static net.earthcomputer.clientcommands.compat.ClientCommands.*;

public class RelogCommand {
    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.crelog.failed"));

    public static void register(CommandDispatcher<CClientCommandSource> dispatcher) {
        dispatcher.register(literal("crelog")
            .executes(ctx -> relog()));
    }

    private static int relog() throws CommandSyntaxException {
        if (!Relogger.relog()) {
            throw FAILED_EXCEPTION.create();
        }
        return Command.SINGLE_SUCCESS;
    }

}
