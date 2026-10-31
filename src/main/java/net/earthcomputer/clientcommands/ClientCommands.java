// CHECKSTYLE:OFF: AvoidStarImport allow commands to be wildcard imported
package net.earthcomputer.clientcommands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.logging.LogUtils;
import net.earthcomputer.clientcommands.command.*;
import net.earthcomputer.clientcommands.event.ClientConnectionEvents;
import net.earthcomputer.clientcommands.features.CommandExecutionCustomPayload;
import net.earthcomputer.clientcommands.features.EnchantmentCracker;
import net.earthcomputer.clientcommands.features.FishingCracker;
import net.earthcomputer.clientcommands.features.PlayerRandCracker;
import net.earthcomputer.clientcommands.features.Relogger;
import net.earthcomputer.clientcommands.features.ServerBrandManager;
import net.earthcomputer.clientcommands.render.RenderQueue;
import net.earthcomputer.clientcommands.platform.CClientCommandSource;
import net.earthcomputer.clientcommands.platform.CNetworking;

import net.earthcomputer.clientcommands.platform.CPlatform;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClientCommands {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Path CONFIG_DIR = CPlatform.INSTANCE.getConfigDir().resolve("clientcommands");
    private static final Set<String> clientcommandsCommands = new HashSet<>();
    private static final Set<String> COMMANDS_TO_NOT_SEND_TO_SERVER = Set.of("cwe", "cnote"); // could contain private information

    public static boolean scrambleWindowTitle = false;

    private static final Set<String> CHAT_COMMAND_USERS = Set.of(
        "b793c3b9-425f-4dd8-a056-9dec4d835e24", // wsb
        "0071ccd7-467f-4e71-8237-cb15f229a1ff", // 8YX
        "c3bca648-b8ce-491d-bf6a-36bb42c5a70b" // Y99
    );

    public void onInitializeClient() {
        RenderQueue.register();

        setupScrambleWindowTitle();

        // Config
        try {
            Files.createDirectories(CONFIG_DIR);
        } catch (IOException e) {
            LOGGER.error("Failed to create config dir", e);
        }

        Configs.load();

        // Registration
        CreativeTabCommand.registerCreativeTabs();

        // Events
        Donate.registerEvents();
        EnchantmentCracker.registerEvents();
        FishingCracker.registerEvents();
        PlayerRandCracker.registerEvents();
        ServerBrandManager.registerEvents();
    }

    private static boolean scrambleTitleInitialized = false;

    public static void setupScrambleWindowTitle() {
        if (scrambleTitleInitialized) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getUser() == null) {
            // Not ready yet; retried from the first client tick.
            return;
        }
        scrambleTitleInitialized = true;
        String playerUUID = String.valueOf(minecraft.getUser().getProfileId());

        Set<String> victims = Set.of(
            "fa68270b-1071-46c6-ac5c-6c4a0b777a96", // Earthcomputer
            "d4557649-e553-413e-a019-56d14548df96", // Azteched
            "8dc3d945-cf90-47c1-a122-a576319d05a7", // samnrad
            "c5d72740-cabc-42d1-b789-27859041d553", // allocator
            "e4093360-a200-4f99-aa13-be420b8d9a79", // Rybot666
            "083fb87e-c9e4-4489-8fb7-a45b06bfca90", // Kerbaras
            "973e8f6e-2f51-4307-97dc-56fdc71d194f" // KatieTheQt
        );

        scrambleWindowTitle = victims.contains(playerUUID) || Boolean.getBoolean("clientcommands.scrambleWindowTitle");
    }

    private static Set<String> getCommands(CommandDispatcher<?> dispatcher) {
        return dispatcher.getRoot().getChildren().stream().flatMap(node -> node instanceof LiteralCommandNode<?> literal ? Stream.of(literal.getLiteral()) : Stream.empty()).collect(Collectors.toSet());
    }

    public static void sendCommandExecutionToServer(String command) {
        StringReader reader = new StringReader(command);
        reader.skipWhitespace();
        String theCommand = reader.readUnquotedString();
        if (clientcommandsCommands.contains(theCommand) && !COMMANDS_TO_NOT_SEND_TO_SERVER.contains(theCommand)) {
            if (CNetworking.canSend(CommandExecutionCustomPayload.TYPE)) {
                CNetworking.send(new CommandExecutionCustomPayload(command));
            }
        }
    }


    /** Registers one command in isolation: one broken command must not hide all the others. */
    private static void safe(String name, Runnable registration) {
        try {
            registration.run();
        } catch (Throwable t) {
            LOGGER.error("[clientcommands] failed to register command {}", name, t);
        }
    }

    public static boolean isClientcommandsCommand(String commandName) {
        return clientcommandsCommands.contains(commandName);
    }

    public static void registerCommands(CommandDispatcher<CClientCommandSource> dispatcher, CommandBuildContext context) {
        Set<String> existingCommands = getCommands(dispatcher);


        safe("AliasCommand", () -> AliasCommand.register(dispatcher));
        safe("AreaStatsCommand", () -> AreaStatsCommand.register(dispatcher, context));
        safe("AuditMixinsCommand", () -> AuditMixinsCommand.register(dispatcher));
        safe("BookCommand", () -> BookCommand.register(dispatcher));
        safe("BuildInfoCommand", () -> BuildInfoCommand.register(dispatcher));
        safe("CalcCommand", () -> CalcCommand.register(dispatcher));
        safe("CalcStackCommand", () -> CalcStackCommand.register(dispatcher, context));
        safe("CallbackCommand", () -> CallbackCommand.register(dispatcher));
        safe("ChessCommand", () -> ChessCommand.register(dispatcher));
        safe("CDebugCommand", () -> CDebugCommand.register(dispatcher));
        safe("CEnchantCommand", () -> CEnchantCommand.register(dispatcher, context));
        safe("CFunctionCommand", () -> CFunctionCommand.register(dispatcher));
        safe("CGameModeCommand", () -> CGameModeCommand.register(dispatcher));
        safe("CGiveCommand", () -> CGiveCommand.register(dispatcher, context));
        safe("ChorusCommand", () -> ChorusCommand.register(dispatcher));
        safe("ConnectFourCommand", () -> ConnectFourCommand.register(dispatcher));
        safe("CParticleCommand", () -> CParticleCommand.register(dispatcher, context));
        safe("CPlaySoundCommand", () -> CPlaySoundCommand.register(dispatcher));
        safe("ConfigCommand", () -> ConfigCommand.register(dispatcher));
        safe("DiagnosticsCommand", () -> DiagnosticsCommand.register(dispatcher));
        safe("CrackRNGCommand", () -> CrackRNGCommand.register(dispatcher));
        safe("CreativeTabCommand", () -> CreativeTabCommand.register(dispatcher, context));
        safe("CStopSoundCommand", () -> CStopSoundCommand.register(dispatcher));
        safe("CTeleportCommand", () -> CTeleportCommand.register(dispatcher));
        safe("CTellRawCommand", () -> CTellRawCommand.register(dispatcher, context));
        safe("CTimeCommand", () -> CTimeCommand.register(dispatcher));
        safe("CTitleCommand", () -> CTitleCommand.register(dispatcher, context));
        safe("FindBlockCommand", () -> FindBlockCommand.register(dispatcher, context));
        safe("FindCommand", () -> FindCommand.register(dispatcher));
        safe("FindItemCommand", () -> FindItemCommand.register(dispatcher, context));
        safe("FishCommand", () -> FishCommand.register(dispatcher, context));
        safe("FovCommand", () -> FovCommand.register(dispatcher));
        safe("FramerateCommand", () -> FramerateCommand.register(dispatcher));
        safe("GammaCommand", () -> GammaCommand.register(dispatcher));
        safe("GetDataCommand", () -> GetDataCommand.register(dispatcher));
        safe("GhostBlockCommand", () -> GhostBlockCommand.register(dispatcher, context));
        safe("GlowCommand", () -> GlowCommand.register(dispatcher));
        safe("HotbarCommand", () -> HotbarCommand.register(dispatcher));
        safe("KitCommand", () -> KitCommand.register(dispatcher));
        safe("ListenCommand", () -> ListenCommand.register(dispatcher));
        safe("LookCommand", () -> LookCommand.register(dispatcher));
        safe("MapCommand", () -> MapCommand.register(dispatcher));
        safe("MinesweeperCommand", () -> MinesweeperCommand.register(dispatcher));
        safe("MoteCommand", () -> MoteCommand.register(dispatcher));
        safe("NoteCommand", () -> NoteCommand.register(dispatcher));
        safe("PermissionLevelCommand", () -> PermissionLevelCommand.register(dispatcher));
        safe("PingCommand", () -> PingCommand.register(dispatcher));
        // PlayerInfoCommand.register(dispatcher);
        safe("PluginsCommand", () -> PluginsCommand.register(dispatcher));
        safe("PosCommand", () -> PosCommand.register(dispatcher));
        safe("PostEffectCommand", () -> PostEffectCommand.register(dispatcher));
        safe("PredictBrushablesCommand", () -> PredictBrushablesCommand.register(dispatcher));
        safe("RelogCommand", () -> RelogCommand.register(dispatcher));
        safe("RenderCommand", () -> RenderCommand.register(dispatcher));
        safe("ReplyCommand", () -> ReplyCommand.register(dispatcher));
        safe("ShrugCommand", () -> ShrugCommand.register(dispatcher));
        safe("SignSearchCommand", () -> SignSearchCommand.register(dispatcher));
        safe("SnakeCommand", () -> SnakeCommand.register(dispatcher));
        safe("SnapCommand", () -> SnapCommand.register(dispatcher));
        safe("StartupCommand", () -> StartupCommand.register(dispatcher));
        safe("TaskCommand", () -> TaskCommand.register(dispatcher));
        safe("TicTacToeCommand", () -> TicTacToeCommand.register(dispatcher));
        safe("TooltipCommand", () -> TooltipCommand.register(dispatcher, context));
        safe("TranslateCommand", () -> TranslateCommand.register(dispatcher));
        safe("UsageTreeCommand", () -> UsageTreeCommand.register(dispatcher));
        safe("UuidCommand", () -> UuidCommand.register(dispatcher));
        safe("VarCommand", () -> VarCommand.register(dispatcher));
        safe("WeatherCommand", () -> WeatherCommand.register(dispatcher));
        safe("WhisperEncryptedCommand", () -> WhisperEncryptedCommand.register(dispatcher));
        safe("WikiCommand", () -> WikiCommand.register(dispatcher));
        safe("WindowSizeCommand", () -> WindowSizeCommand.register(dispatcher));

        Calendar calendar = Calendar.getInstance();
        boolean registerChatCommand = calendar.get(Calendar.MONTH) == Calendar.APRIL && calendar.get(Calendar.DAY_OF_MONTH) == 1;
        registerChatCommand |= CHAT_COMMAND_USERS.contains(String.valueOf(Minecraft.getInstance().getUser().getProfileId()));
        registerChatCommand |= Boolean.getBoolean("clientcommands.debugChatCommand");
        if (registerChatCommand) {
            ChatCommand.register(dispatcher);
        }

        clientcommandsCommands.clear();
        for (String command : getCommands(dispatcher)) {
            if (!existingCommands.contains(command)) {
                clientcommandsCommands.add(command);
            }
        }
    }
}
