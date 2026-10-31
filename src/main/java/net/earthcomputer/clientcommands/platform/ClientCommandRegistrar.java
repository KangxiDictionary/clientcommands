package net.earthcomputer.clientcommands.platform;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;

import net.earthcomputer.clientcommands.ClientCommands;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

/**
 * Bridges the mod's command definitions into NeoForge's client command dispatcher.
 *
 * <p>NeoForge already merges its client dispatcher into the connection's command tree (so tab
 * completion and local execution work), therefore the commands only need to be added once at
 * registration time.
 */
public final class ClientCommandRegistrar {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final CommandDispatcher<CClientCommandSource> DISPATCHER = new CommandDispatcher<>();
    private static boolean registered;
    private static boolean mergedLogged;

    private ClientCommandRegistrar() {
    }

    public static CommandDispatcher<CClientCommandSource> getDispatcher() {
        return DISPATCHER;
    }

    public static void register(RegisterClientCommandsEvent event) {
        // A failure here must never take down the vanilla command tree.
        try {
            if (!registered) {
                ClientCommands.registerCommands(DISPATCHER, event.getBuildContext());
                registered = true;
                int roots = DISPATCHER.getRoot().getChildren().size();
                if (roots == 0) {
                    LOGGER.warn("[clientcommands] no root commands were registered");
                } else {
                    LOGGER.info("[clientcommands] registered {} root commands", roots);
                }
            }
            mergeInto(event.getDispatcher());
        } catch (Throwable t) {
            LOGGER.error("[clientcommands] failed to register client commands", t);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void mergeInto(CommandDispatcher<CommandSourceStack> target) {
        int merged = 0;
        for (CommandNode<CClientCommandSource> node : DISPATCHER.getRoot().getChildren()) {
            if (target.getRoot().getChild(node.getName()) == null) {
                target.getRoot().addChild((CommandNode) node);
                merged++;
            }
        }
        if (!mergedLogged && merged > 0) {
            mergedLogged = true;
            LOGGER.info("[clientcommands] merged {} root commands into the client dispatcher", merged);
        }
    }
}
