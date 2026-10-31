package net.earthcomputer.clientcommands.compat;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.earthcomputer.clientcommands.ClientCommands;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;

/**
 * Client-side equivalent of Fabric's client command manager: owns the dispatcher,
 * registers the commands lazily (a CommandBuildContext needs a level) and executes
 * matching commands instead of sending them to the server.
 */
public final class ClientCommandManager {
    private static final CommandDispatcher<CClientCommandSource> DISPATCHER = new CommandDispatcher<>();
    private static boolean registered;

    private ClientCommandManager() {
    }

    public static CommandDispatcher<CClientCommandSource> getDispatcher() {
        return DISPATCHER;
    }

    public static void ensureRegistered(Minecraft client) {
        if (registered) {
            return;
        }
        ClientLevel level = client.level;
        if (level == null) {
            return;
        }
        ClientCommands.registerCommands(DISPATCHER, CommandBuildContext.simple(level.registryAccess(), level.enabledFeatures()));
        registered = true;
    }

    /**
     * Merges the client command nodes into the vanilla suggestion dispatcher so that
     * they show up in tab completion.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void registerInto(CommandDispatcher<?> target) {
        Minecraft client = Minecraft.getInstance();
        if (client == null) {
            return;
        }
        ensureRegistered(client);
        CommandDispatcher rawTarget = target;
        for (Object node : DISPATCHER.getRoot().getChildren()) {
            com.mojang.brigadier.tree.CommandNode rawNode = (com.mojang.brigadier.tree.CommandNode) node;
            if (rawTarget.getRoot().getChild(rawNode.getName()) == null) {
                rawTarget.getRoot().addChild(rawNode);
            }
        }
    }

    /**
     * @return true if the command was a client command and has been handled
     */
    public static boolean handleCommand(String command) {
        Minecraft client = Minecraft.getInstance();
        if (client.getConnection() == null) {
            return false;
        }
        String commandName = command.split(" ")[0];
        if (DISPATCHER.getRoot().getChild(commandName) == null) {
            ensureRegistered(client);
            if (DISPATCHER.getRoot().getChild(commandName) == null) {
                return false;
            }
        }
        CClientCommandSource source = new CClientCommandSource(client.getConnection(), client);
        try {
            DISPATCHER.execute(command, source);
        } catch (CommandSyntaxException e) {
            source.sendError(Component.literal(e.getMessage()));
        }
        return true;
    }
}
