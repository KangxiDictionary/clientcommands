package net.earthcomputer.clientcommands.mixin.compat;

import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.clientcommands.compat.CClientCommandSource;
import net.earthcomputer.clientcommands.compat.ClientCommandManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
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
