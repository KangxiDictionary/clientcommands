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
    private static boolean registrationFailed;
    private static boolean loggedMerge;
    private static int mergeAttempts;
    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();

    private ClientCommandManager() {
    }

    public static CommandDispatcher<CClientCommandSource> getDispatcher() {
        return DISPATCHER;
    }

    public static void ensureRegistered(Minecraft client) {
        if (registered || registrationFailed) {
            return;
        }
        ClientLevel level = client.level;
        if (level == null) {
            return;
        }
        try {
            ClientCommands.registerCommands(DISPATCHER, CommandBuildContext.simple(level.registryAccess(), level.enabledFeatures()));
            registered = true;
            LOGGER.info("[clientcommands] registered {} root commands", DISPATCHER.getRoot().getChildren().size());
        } catch (Throwable t) {
            registrationFailed = true;
            LOGGER.error("Failed to register client commands", t);
        }
    }

    /**
     * Merges the client command nodes into the vanilla suggestion dispatcher so that
     * they show up in tab completion.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void registerInto(CommandDispatcher<?> target) {
        // Never let our merging break the vanilla command tree / tab completion.
        try {
            Minecraft client = Minecraft.getInstance();
            if (client == null) {
                return;
            }
            ensureRegistered(client);
            if (!registered) {
                if (mergeAttempts++ < 3) {
                    LOGGER.warn("[clientcommands] cannot merge, registered=false failed={} level={}",
                        registrationFailed, client.level != null);
                }
                return;
            }
            CommandDispatcher rawTarget = target;
            int added = 0;
            for (Object node : DISPATCHER.getRoot().getChildren()) {
                com.mojang.brigadier.tree.CommandNode rawNode = (com.mojang.brigadier.tree.CommandNode) node;
                if (rawTarget.getRoot().getChild(rawNode.getName()) == null) {
                    rawTarget.getRoot().addChild(rawNode);
                    added++;
                }
            }
            if (added > 0 && !loggedMerge) {
                loggedMerge = true;
                LOGGER.info("[clientcommands] merged {} command nodes into the suggestion dispatcher", added);
            }
        } catch (Throwable t) {
            LOGGER.error("Failed to merge client command suggestions", t);
        }
    }

    /**
     * Keeps the vanilla dispatcher in sync while in game, independent of the injection
     * ordering of {@code ClientPacketListener#getCommands()}.
     */
    public static void mergeIntoConnection(Minecraft client) {
        if (client.getConnection() == null) {
            return;
        }
        ensureRegistered(client);
        if (!registered) {
            return;
        }
        registerInto(client.getConnection().getCommands());
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
