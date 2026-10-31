package net.earthcomputer.clientcommands.event;

import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;

public final class MoreClientEvents {
    private MoreClientEvents() {
    }

    public static final class TimeSync extends Event {
        public final ClientboundSetTimePacket packet;
        public final boolean networkThread;

        public TimeSync(ClientboundSetTimePacket packet, boolean networkThread) {
            this.packet = packet;
            this.networkThread = networkThread;
        }
    }

    @FunctionalInterface
    public interface TimeSyncListener {
        void onTimeSync(ClientboundSetTimePacket packet);
    }

    public static void registerTimeSync(TimeSyncListener listener) {
        NeoForge.EVENT_BUS.addListener(TimeSync.class, event -> {
            if (!event.networkThread) {
                listener.onTimeSync(event.packet);
            }
        });
    }

    public static void registerTimeSyncOnNetworkThread(TimeSyncListener listener) {
        NeoForge.EVENT_BUS.addListener(TimeSync.class, event -> {
            if (event.networkThread) {
                listener.onTimeSync(event.packet);
            }
        });
    }

    public static void postTimeSync(ClientboundSetTimePacket packet, boolean networkThread) {
        NeoForge.EVENT_BUS.post(new TimeSync(packet, networkThread));
    }
}
