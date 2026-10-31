package net.earthcomputer.clientcommands.platform;

import com.mojang.brigadier.suggestion.Suggestion;
import net.earthcomputer.clientcommands.command.Flag;
import net.earthcomputer.clientcommands.interfaces.IClientSuggestionsProvider;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Standalone source for call sites that need one outside of command execution
 * (e.g. evaluating entity selectors while rendering).
 */
public final class SimpleClientCommandSource implements CClientCommandSource {
    private final Map<Flag<?>, Object> flags = new HashMap<>();
    private final Set<String> seenAliases = new HashSet<>();

    @SuppressWarnings("unchecked")
    @Override
    public <T> T clientcommands_getFlag(Flag<T> flag) {
        return (T) this.flags.getOrDefault(flag, flag.getDefaultValue());
    }

    @Override
    public <T> IClientSuggestionsProvider clientcommands_withFlag(Flag<T> flag, T value) {
        this.flags.put(flag, value);
        return this;
    }

    @Override
    public @Nullable List<Suggestion> clientcommands_filterSuggestions(List<Suggestion> suggestions) {
        return null;
    }

    @Override
    public void clientcommands_addSeenAlias(String alias) {
        this.seenAliases.add(alias);
    }

    @Override
    public void clientcommands_removeSeenAlias(String alias) {
        this.seenAliases.remove(alias);
    }

    @Override
    public boolean clientcommands_isAliasSeen(String alias) {
        return this.seenAliases.contains(alias);
    }
}
