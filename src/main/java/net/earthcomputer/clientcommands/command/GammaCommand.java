package net.earthcomputer.clientcommands.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.clientcommands.compat.CClientCommandSource;
import net.minecraft.network.chat.Component;

import static com.mojang.brigadier.arguments.DoubleArgumentType.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.*;

public class GammaCommand {

    public static void register(CommandDispatcher<CClientCommandSource> dispatcher) {
        dispatcher.register(literal("cgamma")
            .then(argument("gamma", doubleArg())
                .executes(ctx -> setGamma(ctx.getSource(), getDouble(ctx, "gamma")))));
    }

    private static int setGamma(CClientCommandSource source, double gamma) {
        source.getClient().options.gamma().value = gamma;

        Component feedback = Component.translatable("commands.cgamma.success", gamma);
        source.sendFeedback(feedback);

        return Command.SINGLE_SUCCESS;
    }

}
