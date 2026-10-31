package net.earthcomputer.clientcommands.mixin.platform;

import com.mojang.brigadier.suggestion.Suggestion;
import net.earthcomputer.clientcommands.command.Flag;
import net.earthcomputer.clientcommands.interfaces.IClientSuggestionsProvider;
import net.earthcomputer.clientcommands.platform.CClientCommandSource;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Completion is evaluated with {@code ClientSuggestionProvider} as the source, so it has to expose
 * the same client command source interface (and flag state) as the execution source.
 */
@Mixin(ClientSuggestionProvider.class)
public abstract class ClientSuggestionProviderMixin implements CClientCommandSource {
    @Unique
    private final Map<Flag<?>, Object> clientcommands$flags = new HashMap<>();

    @Unique
    private final Set<String> clientcommands$seenAliases = new HashSet<>();

    @SuppressWarnings("unchecked")
    @Override
    public <T> T clientcommands_getFlag(Flag<T> flag) {
        return (T) this.clientcommands$flags.getOrDefault(flag, flag.getDefaultValue());
    }

    @Override
    public <T> IClientSuggestionsProvider clientcommands_withFlag(Flag<T> flag, T value) {
        this.clientcommands$flags.put(flag, value);
        return this;
    }

    @Override
    public @Nullable List<Suggestion> clientcommands_filterSuggestions(List<Suggestion> suggestions) {
        if (this.clientcommands$flags.isEmpty()) {
            return null;
        }
        return suggestions.stream().filter(suggestion -> {
            String text = suggestion.getText();
            return !Flag.isFlag(text) || this.clientcommands$flags.keySet().stream()
                .noneMatch(arg -> !arg.isRepeatable() && (text.equals(arg.getFlag()) || text.equals(arg.getShortFlag())));
        }).toList();
    }

    @Override
    public void clientcommands_addSeenAlias(String alias) {
        this.clientcommands$seenAliases.add(alias);
    }

    @Override
    public void clientcommands_removeSeenAlias(String alias) {
        this.clientcommands$seenAliases.remove(alias);
    }

    @Override
    public boolean clientcommands_isAliasSeen(String alias) {
        return this.clientcommands$seenAliases.contains(alias);
    }
}
