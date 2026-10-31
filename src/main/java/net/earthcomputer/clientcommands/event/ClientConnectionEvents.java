package net.earthcomputer.clientcommands.event;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.Packet;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;

public final class ClientConnectionEvents {
    private ClientConnectionEvents() {
    }

    @FunctionalInterface
    public interface SendPacketListener {
        void onSendPacket(ClientPacketListener connection, Packet<?> packet);
    }

    public static final class Disconnect extends Event {
    }

    public static final class SendPacket extends Event {
        public final ClientPacketListener connection;
        public final Packet<?> packet;
        /** {@code true} while the packet is still wrapped in a bundle. */
        public final boolean bundled;

        public SendPacket(ClientPacketListener connection, Packet<?> packet, boolean bundled) {
            this.connection = connection;
            this.packet = packet;
            this.bundled = bundled;
        }
    }

    public static void registerDisconnect(Runnable listener) {
        NeoForge.EVENT_BUS.addListener(Disconnect.class, event -> listener.run());
    }

    public static void registerSendPacket(SendPacketListener listener) {
        NeoForge.EVENT_BUS.addListener(SendPacket.class, event -> {
            if (!event.bundled) {
                listener.onSendPacket(event.connection, event.packet);
            }
        });
    }

    public static void registerSendPacketNoUnpack(SendPacketListener listener) {
        NeoForge.EVENT_BUS.addListener(SendPacket.class, event -> {
            if (event.bundled) {
                listener.onSendPacket(event.connection, event.packet);
            }
        });
    }

    public static void postDisconnect() {
        NeoForge.EVENT_BUS.post(new Disconnect());
    }

    public static void postSendPacket(ClientPacketListener connection, Packet<?> packet, boolean bundled) {
        NeoForge.EVENT_BUS.post(new SendPacket(connection, packet, bundled));
    }
}
