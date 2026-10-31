package net.earthcomputer.clientcommands.compat;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

/**
 * Drop-in replacement for Fabric's client-side Brigadier helpers.
 */
public final class ClientCommands {
    private ClientCommands() {
    }

    public static LiteralArgumentBuilder<CClientCommandSource> literal(String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    public static <T> RequiredArgumentBuilder<CClientCommandSource, T> argument(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    public static CClientCommandSource getSource(CommandContext<CClientCommandSource> context) {
        return context.getSource();
    }

    public static CommandDispatcher<CClientCommandSource> getActiveDispatcher() {
        return ClientCommandManager.getDispatcher();
    }
}
