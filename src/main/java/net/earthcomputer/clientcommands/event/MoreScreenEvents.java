package net.earthcomputer.clientcommands.event;

import net.earthcomputer.clientcommands.compat.CEvent;
import net.earthcomputer.clientcommands.compat.CEventFactory;
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.Nullable;

public final class MoreScreenEvents {
    public static final CEvent<BeforeAdd> BEFORE_ADD = CEventFactory.createArrayBacked(BeforeAdd.class, listeners -> screen -> {
        boolean accept = true;
        for (BeforeAdd listener : listeners) {
            accept &= listener.beforeScreenAdd(screen);
        }
        return accept;
    });

    @FunctionalInterface
    public interface BeforeAdd {
        boolean beforeScreenAdd(@Nullable Screen screen);
    }
}
