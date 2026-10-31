package net.earthcomputer.clientcommands.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.earthcomputer.clientcommands.render.RenderQueue;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Inject(method = "submitFeatures", at = @At("TAIL"))
    private void clientcommands$submitCustomGeometry(LevelRenderState state, SubmitNodeCollector collector, boolean bl, CallbackInfo ci) {
        RenderQueue.submit(new PoseStack(), collector, state);
    }
}
