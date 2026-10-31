package net.earthcomputer.clientcommands.event;

import net.minecraft.client.gui.screens.Screen;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jspecify.annotations.Nullable;

public final class MoreScreenEvents {
    private MoreScreenEvents() {
    }

    /** Cancel to prevent the screen from being shown. */
    public static final class BeforeScreenAdd extends Event implements ICancellableEvent {
        public final @Nullable Screen screen;

        public BeforeScreenAdd(@Nullable Screen screen) {
            this.screen = screen;
        }
    }

    @FunctionalInterface
    public interface ScreenListener {
        /** @return {@code false} to block the screen */
        boolean beforeScreenAdd(@Nullable Screen screen);
    }

    public static void register(ScreenListener listener) {
        NeoForge.EVENT_BUS.addListener(BeforeScreenAdd.class, event -> {
            if (!listener.beforeScreenAdd(event.screen)) {
                event.setCanceled(true);
            }
        });
    }

    /** @return {@code false} if a listener blocked the screen */
    public static boolean post(@Nullable Screen screen) {
        BeforeScreenAdd event = new BeforeScreenAdd(screen);
        NeoForge.EVENT_BUS.post(event);
        return !event.isCanceled();
    }
}
