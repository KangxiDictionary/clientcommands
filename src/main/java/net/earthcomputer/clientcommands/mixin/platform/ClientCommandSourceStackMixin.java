package net.earthcomputer.clientcommands.mixin.platform;

import net.earthcomputer.clientcommands.platform.CClientCommandSource;
import net.neoforged.neoforge.client.ClientCommandSourceStack;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Makes NeoForge's client command source usable as the mod's command source type.
 */
@Mixin(ClientCommandSourceStack.class)
public abstract class ClientCommandSourceStackMixin implements CClientCommandSource {
}
