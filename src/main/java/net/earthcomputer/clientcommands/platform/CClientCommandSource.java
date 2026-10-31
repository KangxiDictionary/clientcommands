package net.earthcomputer.clientcommands.platform;

import java.util.Objects;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

/**
 * Client-side command source used by every clientcommmands command.
 *
 * <p>NeoForge executes client commands with a {@code ClientCommandSourceStack}; a mixin makes that
 * class implement this interface, so commands can be written against the client accessors without
 * casting. The accessors are default methods on purpose: the only state they need is the running
 * {@link Minecraft} instance.
 */
public interface CClientCommandSource
    extends CommandSource,
        net.earthcomputer.clientcommands.interfaces.IClientSuggestionsProvider,
        net.earthcomputer.clientcommands.interfaces.IClientSuggestionsProvider_Alias {
    default Minecraft getClient() {
        return Minecraft.getInstance();
    }

    default LocalPlayer getClientPlayer() {
        return Objects.requireNonNull(this.getClient().player, "player");
    }

    default ClientLevel getClientLevel() {
        return Objects.requireNonNull(this.getClient().level, "level");
    }

    default Vec3 getPosition() {
        return this.getClientPlayer().position();
    }

    default net.minecraft.world.entity.Entity getEntity() {
        return this.getClientPlayer();
    }

    default Vec3 getSourcePosition() {
        return this.getClientPlayer().position();
    }

    /** x = pitch, y = yaw (same convention as Fabric's client command source). */
    default Vec2 getRotation() {
        LocalPlayer player = this.getClientPlayer();
        return new Vec2(player.getXRot(), player.getYRot());
    }


    default net.minecraft.core.RegistryAccess registryAccess() {
        return this.getClientLevel().registryAccess();
    }

    default net.minecraft.world.flag.FeatureFlagSet enabledFeatures() {
        return this.getClientLevel().enabledFeatures();
    }

    default java.util.Set<net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level>> levels() {
        return java.util.Set.of(this.getClientLevel().dimension());
    }

    default java.util.Collection<String> getOnlinePlayerNames() {
        net.minecraft.client.multiplayer.ClientPacketListener connection = this.getClient().getConnection();
        if (connection == null) {
            return java.util.List.of();
        }
        return connection.getOnlinePlayers().stream()
            .map(player -> player.getProfile().name())
            .toList();
    }

    default java.util.Collection<String> getAllTeams() {
        net.minecraft.world.scores.Scoreboard scoreboard = this.getScoreboard();
        return scoreboard == null ? java.util.List.of() : scoreboard.getTeamNames();
    }

    default net.minecraft.world.scores.Scoreboard getScoreboard() {
        return null;
    }

    default net.minecraft.advancements.AdvancementHolder getAdvancement(net.minecraft.resources.Identifier id) {
        return null;
    }

    default net.minecraft.server.MinecraftServer getServer() {
        return null;
    }

    default java.util.stream.Stream<net.minecraft.resources.Identifier> getAvailableSounds() {
        return net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.stream()
            .map(net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT::getKey);
    }

    default String getName() {
        return this.getClient().getUser().getName();
    }

    default boolean isCreativePlayer() {
        return this.getClientPlayer().isCreative();
    }

    default void sendFeedback(Component message) {
        this.getClientPlayer().sendSystemMessage(message);
    }

    default void sendError(Component message) {
        this.getClientPlayer().sendSystemMessage(message.copy().withStyle(ChatFormatting.RED));
    }

    @Override
    default void sendSystemMessage(Component message) {
        this.sendFeedback(message);
    }

    @Override
    default boolean acceptsSuccess() {
        return true;
    }

    @Override
    default boolean acceptsFailure() {
        return true;
    }

    @Override
    default boolean shouldInformAdmins() {
        return false;
    }
}
