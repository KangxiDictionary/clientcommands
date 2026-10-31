package net.earthcomputer.clientcommands.event;

import net.earthcomputer.clientcommands.compat.CEvent;
import net.earthcomputer.clientcommands.compat.CEventFactory;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.Packet;

public final class ClientConnectionEvents {
    public static final CEvent<Disconnect> DISCONNECT = CEventFactory.createArrayBacked(Disconnect.class, listeners -> () -> {
        for (Disconnect listener : listeners) {
            listener.onDisconnect();
        }
    });

    public static final CEvent<SendPacket> SEND_PACKET = CEventFactory.createArrayBacked(SendPacket.class, listeners -> (connection, packet) -> {
        for (SendPacket listener : listeners) {
            listener.onSendPacket(connection, packet);
        }
    });

    public static final CEvent<SendPacket> SEND_PACKET_NO_UNPACK_BUNDLES = CEventFactory.createArrayBacked(SendPacket.class, listeners -> (connection, packet) -> {
        for (SendPacket listener : listeners) {
            listener.onSendPacket(connection, packet);
        }
    });

    @FunctionalInterface
    public interface Disconnect {
        void onDisconnect();
    }

    @FunctionalInterface
    public interface SendPacket {
        void onSendPacket(ClientPacketListener connection, Packet<?> packet);
    }
}
