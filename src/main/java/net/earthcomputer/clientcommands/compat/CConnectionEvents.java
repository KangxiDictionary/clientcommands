package net.earthcomputer.clientcommands.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

public final class CConnectionEvents {
    public interface Join {
        void onJoin(ClientPacketListener handler, Object sender, Minecraft client);
    }

    public interface Disconnect {
        void onDisconnect(ClientPacketListener handler, Minecraft client);
    }

    public static final CEvent<Join> JOIN = CEventFactory.createArrayBacked(Join.class,
        listeners -> (handler, sender, client) -> {
            for (Join listener : listeners) {
                listener.onJoin(handler, sender, client);
            }
        });

    public static final CEvent<Disconnect> DISCONNECT = CEventFactory.createArrayBacked(Disconnect.class,
        listeners -> (handler, client) -> {
            for (Disconnect listener : listeners) {
                listener.onDisconnect(handler, client);
            }
        });

    private CConnectionEvents() {
    }
}
