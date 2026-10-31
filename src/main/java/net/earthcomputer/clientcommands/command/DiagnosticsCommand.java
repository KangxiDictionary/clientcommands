package net.earthcomputer.clientcommands.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.clientcommands.Configs;
import net.earthcomputer.clientcommands.platform.CPlatform;
import net.earthcomputer.clientcommands.platform.CClientCommandSource;
import net.earthcomputer.clientcommands.platform.ClientCommandRegistrar;
import net.earthcomputer.clientcommands.platform.PlatformSelfCheck;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.ClientCommandSourceStack;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import static net.earthcomputer.clientcommands.platform.ClientCommands.literal;

/**
 * Prints the platform state so a bug report can be diagnosed without reading the log.
 */
public final class DiagnosticsCommand {
    private DiagnosticsCommand() {
    }

    public static void register(CommandDispatcher<CClientCommandSource> dispatcher) {
        dispatcher.register(literal("cdiag").executes(ctx -> run(ctx.getSource())));
    }

    private static int run(CClientCommandSource source) {
        source.sendFeedback(Component.translatable("commands.cdiag.header").withStyle(ChatFormatting.AQUA));

        String modVersion = ModList.get().getModContainerById("clientcommands")
            .map(container -> container.getModInfo().getVersion().toString()).orElse("?");
        String neoVersion = ModList.get().getModContainerById("neoforge")
            .map(container -> container.getModInfo().getVersion().toString()).orElse("?");
        line(source, "commands.cdiag.versions", modVersion, SharedConstants.getCurrentVersion().name(), neoVersion);

        line(source, "commands.cdiag.platform",
            ClientCommandRegistrar.getDispatcher().getRoot().getChildren().size(),
            status(CClientCommandSource.class.isAssignableFrom(ClientCommandSourceStack.class)),
            status(CClientCommandSource.class.isAssignableFrom(ClientSuggestionProvider.class)));

        line(source, "commands.cdiag.accessTransformer", status(PlatformSelfCheck.isAccessTransformerApplied()));

        boolean meleeHook = mixinEnabled("rngevents.PlayerMixin");
        lineStyle(source, "commands.cdiag.meleeHook", meleeHook ? ChatFormatting.GRAY : ChatFormatting.RED,
            meleeHook ? "enabled" : "disabled");

        line(source, "commands.cdiag.config", Configs.getOptionNames().size(),
            CPlatform.INSTANCE.getConfigDir().resolve("clientcommands").resolve("config.json").toString());

        line(source, "commands.cdiag.rng",
            Configs.playerCrackState.name().toLowerCase(Locale.ROOT),
            Configs.enchCrackState.name().toLowerCase(Locale.ROOT),
            status(Configs.playerRNGMaintenance));

        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level != null && minecraft.player != null) {
            line(source, "commands.cdiag.world", level.dimension().identifier(),
                (int) minecraft.player.getX(), (int) minecraft.player.getY(), (int) minecraft.player.getZ(),
                level.getEntityCount());
        } else {
            source.sendFeedback(Component.translatable("commands.cdiag.noWorld").withStyle(ChatFormatting.GRAY));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static String status(boolean value) {
        return value ? "OK" : "MISSING";
    }

    private static void line(CClientCommandSource source, String key, Object... args) {
        lineStyle(source, key, ChatFormatting.GRAY, args);
    }

    private static void lineStyle(CClientCommandSource source, String key, ChatFormatting color, Object... args) {
        source.sendFeedback(Component.translatable(key, args).withStyle(color));
    }

    private static boolean mixinEnabled(String entry) {
        try (InputStream in = DiagnosticsCommand.class.getResourceAsStream("/mixins.clientcommands.json")) {
            if (in == null) {
                return false;
            }
            String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return json.contains(entry);
        } catch (Throwable t) {
            return false;
        }
    }
}
