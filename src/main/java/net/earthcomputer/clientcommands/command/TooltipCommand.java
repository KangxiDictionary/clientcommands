package net.earthcomputer.clientcommands.command;

import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.clientcommands.platform.CClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

import static dev.xpple.clientarguments.arguments.CItemArgument.*;
import static net.earthcomputer.clientcommands.command.ClientCommandHelper.*;
import static net.earthcomputer.clientcommands.compat.ClientCommands.*;

public class TooltipCommand {

    private static final Flag<Boolean> FLAG_ADVANCED = Flag.ofFlag("advanced").build();

    public static void register(CommandDispatcher<CClientCommandSource> dispatcher, CommandBuildContext context) {
        var ctooltip = dispatcher.register(literal("ctooltip")
            .then(literal("held")
                .executes(ctx -> showTooltip(ctx.getSource(), ctx.getSource().getClientPlayer().getMainHandItem(), "held")))
            .then(literal("stack")
                .then(argument("stack", itemStack(context))
                    .executes(ctx -> showTooltip(ctx.getSource(), getItemStackArgument(ctx, "stack").createItemStack(1), "stack")))));
        FLAG_ADVANCED.addToCommand(dispatcher, ctooltip, ctx -> true);
    }

    private static int showTooltip(CClientCommandSource source, ItemStack stack, String type) {
        source.sendFeedback(Component.translatable("commands.ctooltip.header." + type));

        TooltipFlag flag = getFlag(source, FLAG_ADVANCED) ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL;

        List<Component> tooltip = stack.getTooltipLines(Item.TooltipContext.of(source.getClientLevel()), source.getClientPlayer(), flag);
        for (Component line : tooltip) {
            source.sendFeedback(line);
        }

        return tooltip.size();
    }
}
