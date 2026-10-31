package dev.xpple.clientarguments.arguments;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.earthcomputer.clientcommands.compat.CClientCommandSource;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;

public class CSuggestionProviders {
    public static final SuggestionProvider<CClientCommandSource> AVAILABLE_SOUNDS = (context, builder) -> SharedSuggestionProvider.suggestResource(context.getSource().getAvailableSounds(), builder);
    public static final SuggestionProvider<CClientCommandSource> SUMMONABLE_ENTITIES = (context, builder) -> SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.stream().filter(EntityType::canSummon), builder, EntityType::getKey, EntityType::getDescription);
}
