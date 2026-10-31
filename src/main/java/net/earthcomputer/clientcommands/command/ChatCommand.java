package net.earthcomputer.clientcommands.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.clientcommands.platform.CClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;

import static net.earthcomputer.clientcommands.command.ClientCommandHelper.*;
import static net.earthcomputer.clientcommands.compat.ClientCommands.*;

public class ChatCommand {
    public static void register(CommandDispatcher<CClientCommandSource> dispatcher) {
        dispatcher.register(literal("chat").executes(ctx -> execute()));
    }

    private static int execute() {
        Minecraft.getInstance().schedule(() -> {
            Minecraft.getInstance().gui.openChatScreen(ChatComponent.ChatMethod.MESSAGE);
            sendFeedback("commands.chat.success");
        });
        return Command.SINGLE_SUCCESS;
    }
}
