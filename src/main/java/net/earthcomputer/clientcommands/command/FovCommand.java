package net.earthcomputer.clientcommands.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.clientcommands.platform.CClientCommandSource;
import net.minecraft.network.chat.Component;

import static com.mojang.brigadier.arguments.IntegerArgumentType.*;
import static net.earthcomputer.clientcommands.platform.ClientCommands.*;

public class FovCommand {

    public static void register(CommandDispatcher<CClientCommandSource> dispatcher) {
        dispatcher.register(literal("cfov")
            .then(argument("fov", integer(0, 360))
                .executes(ctx -> setFov(ctx.getSource(), getInteger(ctx, "fov"))))
            .then(literal("normal")
                .executes(ctx -> setFov(ctx.getSource(), 70)))
            .then(literal("quakePro")
                .executes(ctx -> setFov(ctx.getSource(), 110))));
    }

    private static int setFov(CClientCommandSource source, int fov) {
        source.getClient().options.fov().value = fov;

        Component feedback = Component.translatable("commands.cfov.success", fov);
        source.sendFeedback(feedback);

        return Command.SINGLE_SUCCESS;
    }

}
