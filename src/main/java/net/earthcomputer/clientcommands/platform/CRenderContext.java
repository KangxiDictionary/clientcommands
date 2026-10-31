package net.earthcomputer.clientcommands.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;

/**
 * Minimal stand-in for Fabric's LevelRenderContext, built from the vanilla
 * render pipeline at the point where the submit collector is available.
 */
public record CRenderContext(PoseStack poseStack, Camera camera, DeltaTracker deltaTracker,
                             SubmitNodeCollector submitNodeCollector, LevelRenderState levelState) {
}
