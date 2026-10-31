package net.earthcomputer.clientcommands.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.clientcommands.features.ClientTimeModifier;
import net.earthcomputer.clientcommands.platform.CClientCommandSource;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;

import static dev.xpple.clientarguments.arguments.CTimeArgument.*;
import static net.earthcomputer.clientcommands.platform.ClientCommands.*;

public class CTimeCommand {

    public static void register(CommandDispatcher<CClientCommandSource> dispatcher) {
        dispatcher.register(literal("ctime")
            .then(literal("query")
                .then(literal("day")
                    .executes(ctx -> executeQueryDay(ctx.getSource())))
                .then(literal("daytime")
                    .executes(ctx -> executeQueryDayTime(ctx.getSource())))
                .then(literal("gametime")
                    .executes(ctx -> executeQueryGameTime(ctx.getSource()))))
            .then(literal("set")
                .then(literal("day")
                    .executes(ctx -> executeSetTime(ctx.getSource(), 1000)))
                .then(literal("noon")
                    .executes(ctx -> executeSetTime(ctx.getSource(), 6000)))
                .then(literal("night")
                    .executes(ctx -> executeSetTime(ctx.getSource(), 13000)))
                .then(literal("midnight")
                    .executes(ctx -> executeSetTime(ctx.getSource(), 18000)))
                .then(argument("time", time())
                    .executes(ctx -> executeSetTime(ctx.getSource(), getTime(ctx, "time")))))
             .then(literal("reset")
                     .executes(ctx -> executeResetTime(ctx.getSource())))
        );
    }

    private static int executeQueryDay(CClientCommandSource source) {
        return executeQuery(source, (int) (source.getClientLevel().getOverworldClockTime() / SharedConstants.TICKS_PER_GAME_DAY % 2147483647L));
    }

    private static int executeQueryDayTime(CClientCommandSource source) {
        return executeQuery(source, (int) (source.getClientLevel().getOverworldClockTime() % SharedConstants.TICKS_PER_GAME_DAY));
    }

    private static int executeQueryGameTime(CClientCommandSource source) {
        return executeQuery(source, (int) (source.getClientLevel().getGameTime() % 2147483647L));
    }

    private static int executeQuery(CClientCommandSource source, int time) {
        source.sendFeedback(Component.translatable("commands.time.query.timeline", "minecraft:overworld", time));
        return time;
    }

    private static int executeSetTime(CClientCommandSource source, int time) {
        ClientTimeModifier.lock(time);
        return executeQueryDayTime(source);
    }

    private static int executeResetTime(CClientCommandSource source) {
        ClientTimeModifier.none();
        source.sendFeedback(Component.translatable("commands.ctime.reset.success"));
        return Command.SINGLE_SUCCESS;
    }
}
