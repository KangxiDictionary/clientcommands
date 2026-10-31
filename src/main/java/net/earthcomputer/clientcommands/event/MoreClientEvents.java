package net.earthcomputer.clientcommands.event;

import net.earthcomputer.clientcommands.compat.CEvent;
import net.earthcomputer.clientcommands.compat.CEventFactory;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;

public final class MoreClientEvents {
    public static final CEvent<TimeSync> TIME_SYNC_ON_NETWORK_THREAD = CEventFactory.createArrayBacked(TimeSync.class, listeners -> packet -> {
        for (TimeSync listener : listeners) {
            listener.onTimeSync(packet);
        }
    });
    public static final CEvent<TimeSync> TIME_SYNC = CEventFactory.createArrayBacked(TimeSync.class, listeners -> packet -> {
        for (TimeSync listener : listeners) {
            listener.onTimeSync(packet);
        }
    });

    @FunctionalInterface
    public interface TimeSync {
        void onTimeSync(ClientboundSetTimePacket packet);
    }
}
