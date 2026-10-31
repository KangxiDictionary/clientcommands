package net.earthcomputer.clientcommands.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.earthcomputer.clientcommands.Configs;
import net.earthcomputer.clientcommands.platform.CClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.List;

import static net.earthcomputer.clientcommands.compat.ClientCommands.*;

/**
 * Carpet style settings page: the setting list is printed in chat, the names are clickable to show
 * details and the values are clickable to change them.
 */
public final class ConfigCommand {
    private static final int PAGE_SIZE = 10;

    private ConfigCommand() {
    }

    public static void register(CommandDispatcher<CClientCommandSource> dispatcher) {
        dispatcher.register(literal("cconfig")
            .executes(ctx -> list(ctx.getSource(), 1))
            .then(argument("page", IntegerArgumentType.integer(1))
                .executes(ctx -> list(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "page"))))
            .then(literal("list")
                .executes(ctx -> list(ctx.getSource(), 1))
                .then(argument("page", IntegerArgumentType.integer(1))
                    .executes(ctx -> list(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "page")))))
            .then(literal("get")
                .then(argument("option", StringArgumentType.word())
                    .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(Configs.getOptionNames(), builder))
                    .executes(ctx -> get(ctx.getSource(), StringArgumentType.getString(ctx, "option")))))
            .then(literal("set")
                .then(argument("option", StringArgumentType.word())
                    .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(Configs.getOptionNames(), builder))
                    .then(argument("value", StringArgumentType.greedyString())
                        .suggests(ConfigCommand::suggestValues)
                        .executes(ctx -> set(ctx.getSource(),
                            StringArgumentType.getString(ctx, "option"),
                            StringArgumentType.getString(ctx, "value")))))));
    }

    private static java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestValues(
        CommandContext<CClientCommandSource> ctx,
        com.mojang.brigadier.suggestion.SuggestionsBuilder builder
    ) {
        List<String> choices = Configs.getOptionChoices(StringArgumentType.getString(ctx, "option"));
        return SharedSuggestionProvider.suggest(choices, builder);
    }

    private static int list(CClientCommandSource source, int page) {
        List<String> names = Configs.getOptionNames();
        if (names.isEmpty()) {
            source.sendFeedback(Component.translatable("commands.cconfig.noOptions"));
            return 0;
        }
        int pageCount = (names.size() + PAGE_SIZE - 1) / PAGE_SIZE;
        int current = Math.clamp(page, 1, pageCount);
        int from = (current - 1) * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, names.size());

        source.sendFeedback(Component.translatable("commands.cconfig.title", current, pageCount));
        for (int i = from; i < to; i++) {
            source.sendFeedback(renderEntry(names.get(i)));
        }

        MutableComponent footer = Component.literal("");
        if (current > 1) {
            footer.append(pageButton("commands.cconfig.page.prev", current - 1));
        }
        if (current < pageCount) {
            if (current > 1) {
                footer.append(" ");
            }
            footer.append(pageButton("commands.cconfig.page.next", current + 1));
        }
        if (!footer.getString().isEmpty()) {
            source.sendFeedback(footer);
        }
        return to - from;
    }

    private static Component pageButton(String key, int page) {
        return Component.translatable(key).withStyle(style -> style
            .withColor(ChatFormatting.BLUE)
            .withClickEvent(new ClickEvent.RunCommand("/cconfig list " + page))
            .withHoverEvent(new HoverEvent.ShowText(Component.translatable("commands.cconfig.hover.page", page))));
    }

    private static Component renderEntry(String name) {
        String value = Configs.getOptionValue(name);
        MutableComponent namePart = Component.literal(name).withStyle(style -> style
            .withColor(ChatFormatting.AQUA)
            .withClickEvent(new ClickEvent.RunCommand("/cconfig get " + name))
            .withHoverEvent(new HoverEvent.ShowText(Component.translatable("commands.cconfig.desc." + name))));
        return Component.literal("- ").append(namePart).append(" = ").append(renderValue(name, value));
    }

    private static Component renderValue(String name, String value) {
        // Every value behaves the same way: clicking prefills the command instead of silently
        // applying something, so the player always sees what is about to be executed.
        String suggestion = nextChoice(Configs.getOptionChoices(name), value);
        String command = suggestion == null ? "/cconfig set " + name + " " : "/cconfig set " + name + " " + suggestion;
        Style style = Style.EMPTY.withColor(ChatFormatting.WHITE)
            .withClickEvent(new ClickEvent.SuggestCommand(command))
            .withHoverEvent(new HoverEvent.ShowText(suggestion == null
                ? Component.translatable("commands.cconfig.hover.suggest", name)
                : Component.translatable("commands.cconfig.hover.prefill", name, suggestion)));
        return Component.literal(value).withStyle(style);
    }

    private static String nextChoice(List<String> choices, String current) {
        for (String choice : choices) {
            if (choice.equalsIgnoreCase(current)) {
                int index = choices.indexOf(choice);
                return choices.get((index + 1) % choices.size());
            }
        }
        return choices.isEmpty() ? null : choices.getFirst();
    }

    private static int get(CClientCommandSource source, String option) {
        String value = Configs.getOptionValue(option);
        if (value == null) {
            source.sendError(Component.translatable("commands.cconfig.unknown", option));
            return 0;
        }
        String type = Configs.getOptionTypeName(option);
        source.sendFeedback(Component.translatable("commands.cconfig.desc." + option));
        source.sendFeedback(Component.translatable("commands.cconfig.get", option, value, type == null ? "?" : type));
        List<String> choices = Configs.getOptionChoices(option);
        if (!choices.isEmpty()) {
            MutableComponent choicesLine = Component.translatable("commands.cconfig.choices").append(" ");
            for (int i = 0; i < choices.size(); i++) {
                if (i > 0) {
                    choicesLine.append(", ");
                }
                String choice = choices.get(i);
                choicesLine.append(Component.literal(choice).withStyle(style -> style
                    .withColor(ChatFormatting.GREEN)
                    .withClickEvent(new ClickEvent.RunCommand("/cconfig set " + option + " " + choice))));
            }
            source.sendFeedback(choicesLine);
        }
        return 1;
    }

    private static int set(CClientCommandSource source, String option, String value) {
        String error = Configs.setOptionValue(option, value);
        if (Configs.ERROR_UNKNOWN_OPTION.equals(error)) {
            source.sendError(Component.translatable("commands.cconfig.unknown", option));
            return 0;
        }
        if (error != null) {
            source.sendError(Component.translatable("commands.cconfig.invalid", option, value));
            return 0;
        }
        source.sendFeedback(Component.translatable("commands.cconfig.set", option, Configs.getOptionValue(option)));
        return 1;
    }
}
