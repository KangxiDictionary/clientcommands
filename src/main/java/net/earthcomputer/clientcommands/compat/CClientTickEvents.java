package net.earthcomputer.clientcommands.compat;

import net.minecraft.client.Minecraft;

/**
 * Fabric-style client tick events, fired from the NeoForge event bus.
 */
public final class CClientTickEvents {
    public interface StartTick {
        void onStartTick(Minecraft client);
    }

    public interface EndTick {
        void onEndTick(Minecraft client);
    }

    public static final CEvent<StartTick> START_CLIENT_TICK = CEventFactory.createArrayBacked(StartTick.class,
        listeners -> client -> {
            for (StartTick listener : listeners) {
                listener.onStartTick(client);
            }
        });

    public static final CEvent<EndTick> END_CLIENT_TICK = CEventFactory.createArrayBacked(EndTick.class,
        listeners -> client -> {
            for (EndTick listener : listeners) {
                listener.onEndTick(client);
            }
        });

    private CClientTickEvents() {
    }
}
