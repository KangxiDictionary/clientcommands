package net.earthcomputer.clientcommands.platform;

import net.earthcomputer.clientcommands.Configs;
import net.earthcomputer.clientcommands.command.ReplyCommand;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * NeoForge config specification. {@link Configs} keeps the mutable static fields used by the rest of
 * the mod; this class defines the TOML file, its defaults, comments and value ranges.
 */
public final class ConfigSpecs {
    public static final ModConfigSpec SPEC;
    public static final Map<String, ModConfigSpec.ConfigValue<?>> VALUES = new LinkedHashMap<>();

    private ConfigSpecs() {
    }

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("General").push("general");
        put("donationMessage", builder
            .comment("Show the donation reminder on the first join.")
            .define("donationMessage", true));
        put("autoPrefix", builder
            .comment("Text automatically prepended to chat messages you send.")
            .define("autoPrefix", ""));
        put("commandExecutionLimit", builder
            .comment("Maximum number of commands the mod may run per tick.")
            .defineInRange("commandExecutionLimit", 65536, 1, 1_000_000, Integer.class));
        put("overriddenFps", builder
            .comment("Overrides the FPS limit (0 disables the override).")
            .defineInRange("overriddenFps", 0, 0, 1000, Integer.class));
        put("toolBreakWarning", builder
            .comment("Warn in chat before a tool is about to break.")
            .define("toolBreakWarning", false));
        put("infiniteTools", builder
            .comment("Stop tools from losing durability when used by the client.")
            .define("infiniteTools", false));
        put("acceptC2CPackets", builder
            .comment("Accept client to client packets sent by some servers.")
            .define("acceptC2CPackets", false));
        builder.pop();

        builder.comment("Enchantment and RNG manipulation").push("manipulation");
        put("enchantingPrediction", builder
            .comment("Predict enchanting table results. Requires a cracked player seed (/ccrackrng).")
            .define("enchantingPrediction", false));
        put("playerRNGMaintenance", builder
            .comment("Track the player's random number consumption so seed cracking stays accurate.")
            .define("playerRNGMaintenance", true));
        put("maxEnchantItemThrows", builder
            .comment("Maximum number of item throws used when manipulating enchantments.")
            .defineInRange("maxEnchantItemThrows", 16384, 1, 1_000_000, Integer.class));
        put("minEnchantBookshelves", builder
            .comment("Minimum bookshelves used in the enchantment simulation.")
            .defineInRange("minEnchantBookshelves", 0, 0, 32, Integer.class));
        put("maxEnchantBookshelves", builder
            .comment("Maximum bookshelves used in the enchantment simulation.")
            .defineInRange("maxEnchantBookshelves", 15, 0, 32, Integer.class));
        put("minEnchantLevels", builder
            .comment("Minimum enchanting level used in the simulation.")
            .defineInRange("minEnchantLevels", 1, 1, 100, Integer.class));
        put("maxEnchantLevels", builder
            .comment("Maximum number of enchanting levels used in the simulation.")
            .defineInRange("maxEnchantLevels", 30, 1, 100, Integer.class));
        put("maxEnchantSlot", builder
            .comment("Highest enchantment slot to use (1 to 3).")
            .defineInRange("maxEnchantSlot", 3, 1, 3, Integer.class));
        put("itemThrowsPerTick", builder
            .comment("How many items are thrown per tick while manipulating.")
            .defineInRange("itemThrowsPerTick", 1.0f, 0.01f, 20.0f, Float.class));
        put("chorusManipulation", builder
            .comment("Enable chorus fruit teleport manipulation.")
            .define("chorusManipulation", false));
        put("maxChorusItemThrows", builder
            .comment("Maximum number of item throws used when manipulating chorus fruit.")
            .defineInRange("maxChorusItemThrows", 2048, 1, 1_000_000, Integer.class));
        put("fishingManipulation", builder
            .comment("Fishing manipulation mode: OFF, MANUAL or AFK.")
            .defineEnum("fishingManipulation", Configs.FishingManipulation.OFF));
        builder.pop();

        builder.comment("Debugging").push("debug");
        put("packetDumpMethod", builder
            .comment("How packet contents are dumped for debugging.")
            .defineEnum("packetDumpMethod", Configs.PacketDumpMethod.REFLECTION));
        put("maximumPacketFieldDepth", builder
            .comment("Maximum field depth when dumping packets.")
            .defineInRange("maximumPacketFieldDepth", 10, 1, 100, Integer.class));
        put("minimumReplyDelaySeconds", builder
            .comment("Minimum delay before an automatic reply is sent.")
            .defineInRange("minimumReplyDelaySeconds", 0.5f, 0.0f, ReplyCommand.MAXIMUM_REPLY_DELAY_SECONDS, Float.class));
        builder.pop();

        SPEC = builder.build();
    }

    private static void put(String name, ModConfigSpec.ConfigValue<?> value) {
        VALUES.put(name, value);
    }
}
