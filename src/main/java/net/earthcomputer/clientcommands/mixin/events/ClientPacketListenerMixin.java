package net.earthcomputer.clientcommands.mixin.events;

import net.earthcomputer.clientcommands.event.MoreClientEntityEvents;
import net.earthcomputer.clientcommands.event.MoreClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Inject(method = "handleAddEntity", at = @At("HEAD"))
    public void onHandleAddEntityPre(ClientboundAddEntityPacket packet, CallbackInfo ci) {
        MoreClientEntityEvents.postAddEntity(packet, true);
    }

    @Inject(method = "handleAddEntity", at = @At("TAIL"))
    public void onHandleAddEntity(ClientboundAddEntityPacket packet, CallbackInfo ci) {
        MoreClientEntityEvents.postAddEntity(packet, false);
    }

    @Inject(method = "handleSetTime", at = @At("HEAD"))
    private void onHandleSetTime(ClientboundSetTimePacket packet, CallbackInfo ci) {
        if (Minecraft.getInstance().isSameThread()) {
            MoreClientEvents.postTimeSync(packet, false);
        } else {
            MoreClientEvents.postTimeSync(packet, true);
        }
    }
}
