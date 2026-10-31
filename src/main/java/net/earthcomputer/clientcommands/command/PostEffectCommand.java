package net.earthcomputer.clientcommands.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.clientcommands.platform.CClientCommandSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import static net.earthcomputer.clientcommands.command.arguments.PostEffectArgument.*;
import static net.earthcomputer.clientcommands.platform.ClientCommands.*;

public class PostEffectCommand {
    public static void register(CommandDispatcher<CClientCommandSource> dispatcher) {
        dispatcher.register(literal("cposteffect")
            .then(argument("posteffect", postEffect())
                .executes(ctx -> applyPostEffect(ctx.getSource(), getPostEffect(ctx, "posteffect"))))
            .then(literal("reset")
                .executes(ctx -> applyPostEffect(ctx.getSource(), null))));
    }

    private static int applyPostEffect(CClientCommandSource source, @Nullable Identifier postEffect) {
        if (postEffect == null) {
            source.getClient().gameRenderer.clearSpectatedEntityPostEffect();
            source.sendFeedback(Component.translatable("commands.cposteffect.reset.success"));
            return Command.SINGLE_SUCCESS;
        }
        source.getClient().gameRenderer.setSpectatedEntityPostEffect(postEffect);
        source.sendFeedback(Component.translatable("commands.cposteffect.apply.success", postEffect));
        return Command.SINGLE_SUCCESS;
    }
}
