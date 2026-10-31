package net.earthcomputer.clientcommands.mixin.compat;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.clientcommands.compat.CClientCommandSource;
import net.earthcomputer.clientcommands.compat.ClientCommandManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Unique
    private CClientCommandSource clientcommands$source;

    /**
     * Vanilla parses completion input with {@code getSuggestionsProvider()}. Handing out our own
     * source type keeps the client command casts working while the client command nodes are merged
     * into the vanilla dispatcher.
     */
    @ModifyReturnValue(method = "getSuggestionsProvider", at = @At("RETURN"))
    private ClientSuggestionProvider clientcommands$useClientSource(ClientSuggestionProvider original) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return original;
        }
        if (clientcommands$source == null) {
            clientcommands$source = new CClientCommandSource((ClientPacketListener) (Object) this, minecraft);
        }
        return clientcommands$source;
    }

    @Inject(method = "sendCommand", at = @At("HEAD"), cancellable = true)
    private void clientcommands$interceptClientCommand(String command, CallbackInfo ci) {
        if (ClientCommandManager.handleCommand(command)) {
            ci.cancel();
        }
    }

    @Inject(method = "getCommands", at = @At("RETURN"))
    private void clientcommands$addClientCommands(CallbackInfoReturnable<CommandDispatcher<ClientSuggestionProvider>> cir) {
        CommandDispatcher<ClientSuggestionProvider> dispatcher = cir.getReturnValue();
        if (dispatcher != null) {
            ClientCommandManager.registerInto(dispatcher);
        }
    }
}
