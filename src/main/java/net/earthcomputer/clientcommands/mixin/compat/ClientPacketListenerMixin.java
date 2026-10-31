package net.earthcomputer.clientcommands.mixin.compat;

import net.earthcomputer.clientcommands.compat.ClientCommandManager;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Inject(method = "sendCommand", at = @At("HEAD"), cancellable = true)
    private void clientcommands$interceptClientCommand(String command, CallbackInfo ci) {
        if (ClientCommandManager.handleCommand(command)) {
            ci.cancel();
        }
    }
}
