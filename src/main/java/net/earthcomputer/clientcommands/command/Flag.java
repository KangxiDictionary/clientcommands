package net.earthcomputer.clientcommands.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.earthcomputer.clientcommands.command.arguments.ExcludingArgument;
import net.earthcomputer.clientcommands.mixin.commands.flag.ArgumentCommandNodeAccessor;
import net.earthcomputer.clientcommands.platform.CClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;

import static net.earthcomputer.clientcommands.platform.ClientCommands.*;

public final class Flag<T> {
    private final Class<T> type;
    private final String name;
    @Nullable
    private final Character shortName;
    private final T defaultValue;
    private final boolean repeatable;

    private Flag(Class<T> type, String name, @Nullable Character shortName, T defaultValue, boolean repeatable) {
        this.type = type;
        this.name = name;
        this.shortName = shortName;
        this.defaultValue = defaultValue;
        this.repeatable = repeatable;
        ALL_FLAGS.add(this);
    }

    public static <T> Flag.Builder<T> of(Class<T> type, String name) {
        return new Builder<>(type, name);
    }

    public static Flag.Builder<Boolean> ofFlag(String name) {
        return new Builder<>(Boolean.class, name).withDefaultValue(false);
    }

    public String getName() {
        return name;
    }

    @Nullable
    public String getShortFlag() {
        return shortName == null ? null : "-" + shortName;
    }

    public String getFlag() {
        return "--" + name;
    }

    public static boolean isFlag(String argument) {
        return (argument.startsWith("-") && argument.length() == 2) || argument.startsWith("--");
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public boolean isRepeatable() {
        return repeatable;
    }

    private void addExclusion(LiteralCommandNode<CClientCommandSource> baseNode) {
        Collection<CommandNode<CClientCommandSource>> children = baseNode.getChildren();
        for (CommandNode<CClientCommandSource> childDowncasted : children) {
            if (childDowncasted instanceof ArgumentCommandNodeAccessor<?> child) {
                ArgumentType<?> argument = child.getArgumentType();
                if (argument instanceof ExcludingArgument<?, ?> excluding) {
                    excluding.exclusions.add(ExcludingArgument.Exclusion.forFlag(this));
                } else {
                    ArgumentType<?> excluding = ExcludingArgument.excluding(argument, ExcludingArgument.Exclusion.forFlag(this));
                    child.setArgumentType(excluding);
                }
            }
        }
    }

    private void addLiteralFlagToCommand(String literal, CommandDispatcher<CClientCommandSource> dispatcher, LiteralCommandNode<CClientCommandSource> baseNode, Function<CommandContext<CClientCommandSource>, T> function) {
        dispatcher.register(baseNode.createBuilder()
                .then(literal(literal)
                        .redirect(baseNode, ctx -> ClientCommandHelper.withFlag(ctx.getSource(), this, function.apply(ctx)))
                        .executes(baseNode.getCommand())));
    }

    private void addArgumentFlagToCommand(String literal, ArgumentType<T> argument, CommandDispatcher<CClientCommandSource> dispatcher, LiteralCommandNode<CClientCommandSource> baseNode) {
        dispatcher.register(baseNode.createBuilder()
                .then(literal(literal)
                        .then(argument(this.name, argument)
                                .redirect(baseNode, ctx -> ClientCommandHelper.withFlag(ctx.getSource(), this, ctx.getArgument(this.name, this.type)))
                                .executes(baseNode.getCommand()))));
    }

    public void addToCommand(CommandDispatcher<CClientCommandSource> dispatcher, LiteralCommandNode<CClientCommandSource> baseNode, Function<CommandContext<CClientCommandSource>, T> function) {
        addExclusion(baseNode);

        addLiteralFlagToCommand(getFlag(), dispatcher, baseNode, function);
        if (shortName != null) {
            addLiteralFlagToCommand(getShortFlag(), dispatcher, baseNode, function);
        }
    }

    public void addToCommandWithArg(CommandDispatcher<CClientCommandSource> dispatcher, LiteralCommandNode<CClientCommandSource> baseNode, ArgumentType<T> argument) {
        addExclusion(baseNode);

        addArgumentFlagToCommand(getFlag(), argument, dispatcher, baseNode);
        if (shortName != null) {
            addArgumentFlagToCommand(getShortFlag(), argument, dispatcher, baseNode);
        }
    }

    public static final com.mojang.brigadier.exceptions.DynamicCommandExceptionType REPEATED_FLAG_EXCEPTION =
        new com.mojang.brigadier.exceptions.DynamicCommandExceptionType(
            flag -> Component.translatable("commands.client.flagRepeated", flag));

    /**
     * Rejects a non-repeatable flag that appears more than once in the input. Brigadier cannot
     * express "at most once", so this is enforced when the command reads the flag.
     */
    private static final java.util.List<Flag<?>> ALL_FLAGS = new java.util.ArrayList<>();

    static {
        // no-op: flags register themselves in the constructor below
    }

    /**
     * Rejects any non-repeatable flag that occurs more than once in the raw command input.
     * Called once per command execution.
     */
    /**
     * Removes suggestions for non-repeatable flags that already appear in the command line, so
     * completion never offers something the dispatcher would reject.
     */
    public static java.util.List<com.mojang.brigadier.suggestion.Suggestion> filterSuggestions(
        String input, java.util.List<com.mojang.brigadier.suggestion.Suggestion> suggestions
    ) {
        java.util.Set<String> used = new java.util.HashSet<>();
        for (Flag<?> flag : ALL_FLAGS) {
            if (flag.repeatable) {
                continue;
            }
            int count = countFlagOccurrences(input, "--" + flag.name);
            if (flag.shortName != null) {
                count += countFlagOccurrences(input, "-" + flag.shortName);
            }
            if (count > 0) {
                used.add("--" + flag.name);
                if (flag.shortName != null) {
                    used.add("-" + flag.shortName);
                }
            }
        }
        if (used.isEmpty()) {
            return suggestions;
        }
        return suggestions.stream().filter(suggestion -> !used.contains(suggestion.getText())).toList();
    }

    public static void checkForRepeatedFlags(String input) throws CommandSyntaxException {
        for (Flag<?> flag : ALL_FLAGS) {
            int count = countFlagOccurrences(input, "--" + flag.name);
            if (flag.shortName != null) {
                count += countFlagOccurrences(input, "-" + flag.shortName);
            }
            if (count > 1 && !flag.repeatable) {
                throw REPEATED_FLAG_EXCEPTION.create("--" + flag.name);
            }
        }
    }

    public void checkNotRepeated(CommandContext<?> ctx) throws CommandSyntaxException {
        if (this.repeatable) {
            return;
        }
        String input = ctx.getInput();
        int count = countFlagOccurrences(input, "--" + this.name);
        if (this.shortName != null) {
            count += countFlagOccurrences(input, "-" + this.shortName);
        }
        if (count > 1) {
            throw REPEATED_FLAG_EXCEPTION.create("--" + this.name);
        }
    }

    private static int countFlagOccurrences(String input, String token) {
        java.util.regex.Matcher matcher = Pattern.compile("(?<![\\w-])" + Pattern.quote(token) + "(?![\\w-])").matcher(input);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    public static <S> S getActualSource(ParseResults<S> parse) {
        return getActualSource(parse.getContext().build(parse.getReader().getString()));
    }

    @SuppressWarnings("unchecked")
    public static <S> S getActualSource(CommandContext<S> ctx) {
        S source = ctx.getSource();
        do {
            if (ctx.hasNodes()) {
                RedirectModifier<S> redirectModifier = ctx.getRedirectModifier();
                if (redirectModifier != null) {
                    try {
                        Collection<S> newSources = redirectModifier.apply(ctx.copyFor(source));
                        if (newSources.size() != 1) {
                            return source;
                        }
                        source = newSources.iterator().next();
                    } catch (CommandSyntaxException e) {
                        return source;
                    }
                }
            }
            ctx = ctx.getChild();
        } while (ctx != null);

        return source;
    }

    public static final class Builder<T> {
        private static final Pattern NAME_PATTERN = Pattern.compile("[a-z0-9][a-z0-9-]*");

        private final Class<T> type;
        private final String name;
        @Nullable
        private Character shortName;
        private boolean hasSetDefaultValue = false;
        private T defaultValue;
        private boolean repeatable;

        private Builder(Class<T> type, String name) {
            if (!NAME_PATTERN.matcher(name).matches()) {
                throw new IllegalArgumentException("Flag name does not match the pattern " + NAME_PATTERN.pattern());
            }

            this.type = type;
            this.name = name;
            this.shortName = name.charAt(0);
        }

        public Builder<T> withNoShortName() {
            this.shortName = null;
            return this;
        }

        public Builder<T> withShortName(char shortName) {
            this.shortName = shortName;
            return this;
        }

        public Builder<T> withDefaultValue(T defaultValue) {
            this.hasSetDefaultValue = true;
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder<T> repeatable() {
            this.repeatable = true;
            return this;
        }

        public Flag<T> build() {
            if (!hasSetDefaultValue) {
                throw new IllegalStateException("Default value for flag not set");
            }
            return new Flag<>(type, name, shortName, defaultValue, repeatable);
        }
    }
}
