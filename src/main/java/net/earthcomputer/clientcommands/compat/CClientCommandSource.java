package net.earthcomputer.clientcommands.compat;

import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSource;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

/**
 * NeoForge replacement for {@code CClientCommandSource}. Extends the vanilla
 * client suggestion provider so that Brigadier completion keeps working.
 */
public class CClientCommandSource extends ClientSuggestionProvider implements CommandSource {
    private final Minecraft client;

    public CClientCommandSource(ClientPacketListener connection, Minecraft client) {
        super(connection, client, PermissionSet.NO_PERMISSIONS);
        this.client = client;
    }

    public Minecraft getClient() {
        return this.client;
    }

    public LocalPlayer getPlayer() {
        return Objects.requireNonNull(this.client.player, "player");
    }

    public ClientLevel getLevel() {
        return Objects.requireNonNull(this.client.level, "level");
    }

    public Entity getEntity() {
        return this.getPlayer();
    }

    public Vec3 getPosition() {
        return this.getPlayer().position();
    }

    public Vec3 getSourcePosition() {
        return this.getPosition();
    }

    /** x = pitch, y = yaw (same convention as Fabric's client command source). */
    public Vec2 getRotation() {
        LocalPlayer player = this.getPlayer();
        return new Vec2(player.getXRot(), player.getYRot());
    }

    public String getName() {
        return this.client.getUser().getName();
    }

    public boolean isCreativePlayer() {
        return this.getPlayer().isCreative();
    }

    public void sendFeedback(Component message) {
        this.getPlayer().sendSystemMessage(message);
    }

    public void sendError(Component message) {
        this.getPlayer().sendSystemMessage(message.copy().withStyle(ChatFormatting.RED));
    }

    @Override
    public void sendSystemMessage(Component message) {
        this.sendFeedback(message);
    }

    @Override
    public boolean acceptsSuccess() {
        return true;
    }

    @Override
    public boolean acceptsFailure() {
        return true;
    }

    @Override
    public boolean shouldInformAdmins() {
        return false;
    }
}
