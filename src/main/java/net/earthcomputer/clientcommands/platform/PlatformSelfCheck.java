package net.earthcomputer.clientcommands.platform;

import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.neoforge.client.ClientCommandSourceStack;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * One-shot runtime verification of the platform assumptions that cannot be checked at build time.
 * The results show up in the game log right after the first client tick.
 */
public final class PlatformSelfCheck {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean done;

    private PlatformSelfCheck() {
    }

    public static void runOnce() {
        if (done) {
            return;
        }
        done = true;
        checkAccessTransformer();
        checkSourceInterface(ClientCommandSourceStack.class, "command execution source");
        checkSourceInterface(ClientSuggestionProvider.class, "command completion source");
        LOGGER.info("[clientcommands] platform self check finished");
    }

    private static void checkAccessTransformer() {
        try {
            Field field = ItemEntity.class.getDeclaredField("DATA_ITEM");
            boolean isPublic = Modifier.isPublic(field.getModifiers());
            if (isPublic) {
                LOGGER.info("[clientcommands] access transformer: applied (DATA_ITEM is public)");
            } else {
                LOGGER.warn("[clientcommands] access transformer: NOT applied (DATA_ITEM is still private)");
            }
        } catch (Throwable t) {
            LOGGER.warn("[clientcommands] access transformer check failed", t);
        }
    }

    private static void checkSourceInterface(Class<?> target, String role) {
        boolean ok = CClientCommandSource.class.isAssignableFrom(target);
        if (ok) {
            LOGGER.info("[clientcommands] {}: ok ({})", role, target.getSimpleName());
        } else {
            LOGGER.warn("[clientcommands] {}: MISSING CClientCommandSource on {}", role, target.getSimpleName());
        }
    }
}
