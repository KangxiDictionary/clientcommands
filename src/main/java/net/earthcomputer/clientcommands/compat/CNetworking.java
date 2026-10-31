package net.earthcomputer.clientcommands.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public final class CNetworking {
    private CNetworking() {
    }

    /**
     * Whether the current connection can carry custom payloads. NeoForge does not expose
     * Fabric's per-channel check, so this only verifies that we are connected; the payload
     * is simply dropped by NeoForge if the server does not advertise the channel.
     */
    public static boolean canSend(CustomPacketPayload.Type<?> type) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        return connection != null && connection.getConnection() != null && connection.getConnection().isConnected();
    }

    public static void send(CustomPacketPayload payload) {
        ClientPacketDistributor.sendToServer(payload);
    }
}
