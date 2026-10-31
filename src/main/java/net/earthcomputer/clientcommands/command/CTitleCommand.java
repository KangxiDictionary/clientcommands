package net.earthcomputer.clientcommands.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.clientcommands.platform.CClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;

import static com.mojang.brigadier.arguments.IntegerArgumentType.*;
import static dev.xpple.clientarguments.arguments.CComponentArgument.*;
import static net.earthcomputer.clientcommands.command.ClientCommandHelper.*;
import static net.earthcomputer.clientcommands.compat.ClientCommands.*;

public class CTitleCommand {

    public static void register(CommandDispatcher<CClientCommandSource> dispatcher, CommandBuildContext context) {
        dispatcher.register(literal("ctitle")
            .then(literal("clear")
                .executes(ctx -> executeClear(ctx.getSource())))
            .then(literal("reset")
                .executes(ctx -> executeReset(ctx.getSource())))
            .then(literal("title")
                .then(argument("title", component(context))
                    .executes(ctx -> executeTitle(ctx.getSource(), getComponent(ctx, "title")))))
            .then(literal("subtitle")
                .then(argument("title", component(context))
                    .executes(ctx -> executeSubtitle(ctx.getSource(), getComponent(ctx, "title")))))
            .then(literal("actionbar")
                .then(argument("title", component(context))
                    .executes(ctx -> executeActionBar(ctx.getSource(), getComponent(ctx, "title")))))
            .then(literal("times")
                .then(argument("fadeIn", integer(0))
                    .then(argument("stay", integer(0))
                        .then(argument("fadeOut", integer(0))
                            .executes(ctx -> executeTimes(ctx.getSource(), getInteger(ctx, "fadeIn"), getInteger(ctx, "stay"), getInteger(ctx, "fadeOut"))))))));
    }

    private static int executeClear(CClientCommandSource source) {
        source.getClient().gui.hud.clearTitles();

        source.sendFeedback(Component.translatable("commands.ctitle.cleared"));
        return Command.SINGLE_SUCCESS;
    }

    private static int executeReset(CClientCommandSource source) {
        source.getClient().gui.hud.clearTitles();
        source.getClient().gui.hud.resetTitleTimes();

        source.sendFeedback(Component.translatable("commands.ctitle.reset"));
        return Command.SINGLE_SUCCESS;
    }

    private static int executeTitle(CClientCommandSource source, Component title) {
        source.getClient().gui.hud.setTitle(title);

        sendFeedback(Component.translatable("commands.ctitle.show.title"));
        return Command.SINGLE_SUCCESS;
    }

    private static int executeSubtitle(CClientCommandSource source, Component title) {
        source.getClient().gui.hud.setSubtitle(title);

        sendFeedback(Component.translatable("commands.ctitle.show.subtitle"));
        return Command.SINGLE_SUCCESS;
    }

    private static int executeActionBar(CClientCommandSource source, Component title) {
        source.getClient().gui.hud.setOverlayMessage(title, false);

        sendFeedback(Component.translatable("commands.ctitle.show.actionbar"));
        return Command.SINGLE_SUCCESS;
    }

    private static int executeTimes(CClientCommandSource source, int fadeIn, int stay, int fadeOut) {
        source.getClient().gui.hud.setTimes(fadeIn, stay, fadeOut);

        sendFeedback(Component.translatable("commands.ctitle.times"));
        return Command.SINGLE_SUCCESS;
    }
}
