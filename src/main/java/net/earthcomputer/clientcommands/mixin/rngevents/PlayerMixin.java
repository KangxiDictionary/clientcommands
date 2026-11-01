package net.earthcomputer.clientcommands.mixin.rngevents;

import com.llamalad7.mixinextras.sugar.Local;
import net.earthcomputer.clientcommands.features.PlayerRandCracker;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Tracks whether a client-side attack will actually damage its target, so weapon durability changes
 * match what the server does.
 *
 * <p>The injection sits before {@code Entity.hurtOrSimulate}, the point where the attack is applied,
 * and captures the damage source (slot 4) and the final damage (slot 11) that vanilla computed. It
 * deliberately avoids {@code @WrapOperation}/{@code @Redirect}: generated wrappers around this call
 * fail verification on 26.3 with "Bad access to protected data in getfield".
 */
@Mixin(Player.class)
public abstract class PlayerMixin {
    @Inject(
        method = "attack",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            ordinal = 0,
            shift = At.Shift.BEFORE
        )
    )
    private void clientcommands$onAttack(Entity target, CallbackInfo ci,
                                         @Local(index = 4) DamageSource source,
                                         @Local(index = 11) float amount) {
        Player self = (Player) (Object) this;
        if (self.level().isClientSide() && self instanceof LocalPlayer player) {
            PlayerRandCracker.onAttack(player, target, source, amount);
        }
    }
}
