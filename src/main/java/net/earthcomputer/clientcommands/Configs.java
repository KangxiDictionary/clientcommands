package net.earthcomputer.clientcommands;

import net.earthcomputer.clientcommands.platform.ConfigSpecs;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.earthcomputer.clientcommands.command.ReplyCommand;
import net.earthcomputer.clientcommands.features.ChorusManipulation;
import net.earthcomputer.clientcommands.features.EnchantmentCracker;
import net.earthcomputer.clientcommands.features.FishingCracker;
import net.earthcomputer.clientcommands.features.PlayerRandCracker;
import net.earthcomputer.clientcommands.features.ServerBrandManager;
import net.earthcomputer.clientcommands.util.MultiVersionCompat;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class Configs {
    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();
    private static final Set<String> NOT_PERSISTED = Set.of("calcAnswer", "enchCrackState", "playerCrackState");

    // ------------------------------------------------------------------
    // Option access used by /cconfig
    // ------------------------------------------------------------------


    public static final String ERROR_UNKNOWN_OPTION = "unknown_option";
    public static final String ERROR_INVALID_VALUE = "invalid_value";

    public static @org.jspecify.annotations.Nullable String getOptionTypeName(String name) {
        Field field = findOption(name);
        if (field == null) {
            return null;
        }
        Class<?> type = field.getType();
        if (type == boolean.class) {
            return "boolean";
        } else if (type == int.class || type == long.class) {
            return "integer";
        } else if (type == float.class || type == double.class) {
            return "number";
        } else if (type == String.class) {
            return "string";
        } else if (type.isEnum()) {
            return "enum";
        }
        return type.getSimpleName();
    }

    public static java.util.List<String> getOptionChoices(String name) {
        Field field = findOption(name);
        if (field == null) {
            return java.util.List.of();
        }
        Class<?> type = field.getType();
        if (type == boolean.class) {
            return java.util.List.of("true", "false");
        }
        if (type.isEnum()) {
            java.util.List<String> choices = new java.util.ArrayList<>();
            for (Object constant : type.getEnumConstants()) {
                choices.add(((Enum<?>) constant).name().toLowerCase(Locale.ROOT));
            }
            return choices;
        }
        return java.util.List.of();
    }

    public static java.util.List<String> getOptionNames() {
        java.util.List<String> names = new java.util.ArrayList<>();
        for (Field field : Configs.class.getDeclaredFields()) {
            if (!isOption(field)) {
                continue;
            }
            names.add(field.getName());
        }
        names.sort(String::compareTo);
        return names;
    }

    private static boolean isOption(Field field) {
        int mods = field.getModifiers();
        return Modifier.isStatic(mods) && !Modifier.isTransient(mods) && !Modifier.isFinal(mods)
            && !field.isSynthetic() && !NOT_PERSISTED.contains(field.getName())
            && isSupportedType(field.getType());
    }

    private static boolean isSupportedType(Class<?> type) {
        return type == boolean.class || type == int.class || type == long.class
            || type == float.class || type == double.class || type == String.class
            || type.isEnum();
    }

    private static Field findOption(String name) {
        try {
            Field field = Configs.class.getDeclaredField(name);
            return isOption(field) ? field : null;
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    public static String getOptionValue(String name) {
        Field field = findOption(name);
        if (field == null) {
            return null;
        }
        try {
            return String.valueOf(field.get(null));
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    /**
     * Sets an option, honouring an existing setter and the {@code onChangeX} hook when present.
     *
     * @return {@code null} on success, otherwise a human readable error
     */
    public static String setOptionValue(String name, String rawValue) {
        Field field = findOption(name);
        if (field == null) {
            return ERROR_UNKNOWN_OPTION;
        }
        Object newValue;
        try {
            newValue = parseValue(field.getType(), rawValue);
        } catch (RuntimeException e) {
            return ERROR_INVALID_VALUE;
        }
        try {
            Object oldValue = field.get(null);
            java.lang.reflect.Method setter = findMethod("set" + capitalize(name), field.getType());
            if (setter != null) {
                setter.invoke(null, newValue);
            } else {
                field.set(null, newValue);
            }
            java.lang.reflect.Method onChange = findMethod("onChange" + capitalize(name), field.getType(), field.getType());
            if (onChange != null) {
                onChange.invoke(null, oldValue, newValue);
            }
            save();
            return null;
        } catch (ReflectiveOperationException | RuntimeException e) {
            LOGGER.warn("Failed to set option {}", name, e);
            return ERROR_INVALID_VALUE;
        }
    }

    private static String capitalize(String name) {
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private static java.lang.reflect.Method findMethod(String name, Class<?>... parameterTypes) {
        try {
            java.lang.reflect.Method method = Configs.class.getDeclaredMethod(name, parameterTypes);
            return Modifier.isStatic(method.getModifiers()) ? method : null;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static Object parseValue(Class<?> type, String raw) {
        if (type == boolean.class || type == Boolean.class) {
            return switch (raw.toLowerCase(Locale.ROOT)) {
                case "true", "1", "yes", "on" -> true;
                case "false", "0", "no", "off" -> false;
                default -> throw new IllegalArgumentException("not a boolean: " + raw);
            };
        } else if (type == int.class || type == Integer.class) {
            return Integer.parseInt(raw);
        } else if (type == float.class || type == Float.class) {
            return Float.parseFloat(raw);
        } else if (type == double.class || type == Double.class) {
            return Double.parseDouble(raw);
        } else if (type == long.class || type == Long.class) {
            return Long.parseLong(raw);
        } else if (type == String.class) {
            return raw;
        } else if (type.isEnum()) {
            @SuppressWarnings({"unchecked", "rawtypes"})
            Object value = Enum.valueOf((Class<? extends Enum>) type, raw.toUpperCase(Locale.ROOT));
            return value;
        }
        throw new IllegalArgumentException("unsupported type " + type);
    }

    /** Writes the current values into the NeoForge config and saves it. */
    public static void save() {
        if (!ConfigSpecs.SPEC.isLoaded()) {
            return;
        }
        for (Map.Entry<String, ModConfigSpec.ConfigValue<?>> entry : ConfigSpecs.VALUES.entrySet()) {
            Field field = findOption(entry.getKey());
            if (field == null) {
                continue;
            }
            try {
                @SuppressWarnings("unchecked")
                ModConfigSpec.ConfigValue<Object> value = (ModConfigSpec.ConfigValue<Object>) entry.getValue();
                value.set(field.get(null));
            } catch (IllegalAccessException e) {
                // ignore
            }
        }
        ConfigSpecs.SPEC.save();
    }

    /** Applies the values loaded from the NeoForge config to the runtime fields. */
    public static void load() {
        if (!ConfigSpecs.SPEC.isLoaded()) {
            return;
        }
        for (Map.Entry<String, ModConfigSpec.ConfigValue<?>> entry : ConfigSpecs.VALUES.entrySet()) {
            Field field = findOption(entry.getKey());
            if (field == null) {
                continue;
            }
            try {
                field.set(null, entry.getValue().get());
            } catch (IllegalAccessException e) {
                // ignore
            }
        }
    }

    public static boolean donationMessage = true;
    public static double calcAnswer = 0;
    public static EnchantmentCracker.CrackState enchCrackState = EnchantmentCracker.CrackState.UNCRACKED;
    public static PlayerRandCracker.CrackState playerCrackState = PlayerRandCracker.CrackState.UNCRACKED;
    public static boolean enchantingPrediction = false;
    private static void onChangeEnchantingPrediction(boolean oldEnchantingPrediction, boolean enchantingPrediction) {
        if (enchantingPrediction) {
            ServerBrandManager.rngWarning();
        } else {
            EnchantmentCracker.resetCracker();
        }
    }

    public enum FishingManipulation implements StringRepresentable {
        OFF,
        MANUAL,
        AFK;

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }

        public boolean isEnabled() {
            return this != OFF;
        }
    }
    public static FishingManipulation fishingManipulation = FishingManipulation.OFF;
    private static void onChangeFishingManipulation(FishingManipulation oldFishingManipulation, FishingManipulation fishingManipulation) {
        if (fishingManipulation.isEnabled()) {
            ServerBrandManager.rngWarning();
        } else {
            FishingCracker.reset();
        }
    }
    public static boolean playerRNGMaintenance = true;
    public static boolean toolBreakWarning = false;
    private static int maxEnchantItemThrows = 64 * 256;
    public static int getMaxEnchantItemThrows() {
        return maxEnchantItemThrows;
    }
    public static void setMaxEnchantItemThrows(int maxEnchantItemThrows) {
        Configs.maxEnchantItemThrows = Mth.clamp(maxEnchantItemThrows, 0, 1000000);
    }
    private static int minEnchantBookshelves = 0;
    public static int getMinEnchantBookshelves() {
        return minEnchantBookshelves;
    }
    public static void setMinEnchantBookshelves(int minEnchantBookshelves) {
        Configs.minEnchantBookshelves = Mth.clamp(minEnchantBookshelves, 0, 15);
        Configs.maxEnchantBookshelves = Math.max(Configs.maxEnchantBookshelves, Configs.minEnchantBookshelves);
    }
    private static int maxEnchantBookshelves = 15;
    public static int getMaxEnchantBookshelves() {
        return maxEnchantBookshelves;
    }
    public static void setMaxEnchantBookshelves(int maxEnchantBookshelves) {
        Configs.maxEnchantBookshelves = Mth.clamp(maxEnchantBookshelves, 0, 15);
        Configs.minEnchantBookshelves = Math.min(Configs.minEnchantBookshelves, Configs.maxEnchantBookshelves);
    }
    private static int minEnchantLevels = 1;
    public static int getMinEnchantLevels() {
        return minEnchantLevels;
    }
    public static void setMinEnchantLevels(int minEnchantLevels) {
        Configs.minEnchantLevels = Mth.clamp(minEnchantLevels, 1, 30);
        Configs.maxEnchantLevels = Math.max(Configs.maxEnchantLevels, Configs.minEnchantLevels);
    }
    private static int maxEnchantSlot = 3;

    public static int getMaxEnchantSlot() {
        return maxEnchantSlot;
    }

    public static void setMaxEnchantSlot(int maxEnchantSlot) {
        Configs.maxEnchantSlot = Mth.clamp(maxEnchantSlot, 1, 3);
    }
    private static int maxEnchantLevels = 30;
    public static int getMaxEnchantLevels() {
        return maxEnchantLevels;
    }
    public static void setMaxEnchantLevels(int maxEnchantLevels) {
        Configs.maxEnchantLevels = Mth.clamp(maxEnchantLevels, 1, 30);
        Configs.minEnchantLevels = Math.min(Configs.minEnchantLevels, Configs.maxEnchantLevels);
    }
    public static boolean chorusManipulation = false;
    public static void onChangeChorusManipulation(boolean oldChorusManipulation, boolean chorusManipulation) {
        if (chorusManipulation) {
            ServerBrandManager.rngWarning();
            ChorusManipulation.onChorusManipEnabled();
        }
    }
    private static int maxChorusItemThrows = 64 * 32;
    public static int getMaxChorusItemThrows() {
        return maxChorusItemThrows;
    }
    public static void setMaxChorusItemThrows(int maxChorusItemThrows) {
        Configs.maxChorusItemThrows = Mth.clamp(maxChorusItemThrows, 0, 1000000);
    }
    public static String autoPrefix = "";
    public static boolean infiniteTools = false;
    public static int commandExecutionLimit = 65536;
    public static boolean acceptC2CPackets = false;
    public static float itemThrowsPerTick = 1;

    public static boolean conditionLessThan1_20() {
        return MultiVersionCompat.INSTANCE.getProtocolVersion() < MultiVersionCompat.V1_20;
    }

    public static boolean conditionLessThan1_21() {
        return MultiVersionCompat.INSTANCE.getProtocolVersion() < MultiVersionCompat.V1_21;
    }
    public static PacketDumpMethod packetDumpMethod = PacketDumpMethod.REFLECTION;

    public enum PacketDumpMethod {
        REFLECTION,
        BYTE_BUF,
    }
    public static int maximumPacketFieldDepth = 10;
    public static float minimumReplyDelaySeconds = 0.5f;
    public static void setMinimumReplyDelaySeconds(float minimumReplyDelaySeconds) {
        Configs.minimumReplyDelaySeconds = Math.clamp(minimumReplyDelaySeconds, 0.0f, ReplyCommand.MAXIMUM_REPLY_DELAY_SECONDS);
    }
    public static int overriddenFps = 0;
}
