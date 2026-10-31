package net.earthcomputer.clientcommands.mixin.rngevents;

import net.earthcomputer.clientcommands.features.PlayerRandCracker;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({MushroomCow.class, Sheep.class, SnowGolem.class})
public class MushroomCowSheepAndSnowGolemMixin {
    @Inject(method = "mobInteract", at = @At("HEAD"))
    public void onInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> ci) {
        if (player.level() instanceof ServerLevel) {
            PlayerRandCracker.onItemDamage(1, player, player.getItemInHand(hand));
        }
    }
}
