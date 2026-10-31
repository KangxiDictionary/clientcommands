package net.earthcomputer.clientcommands.render;

import net.earthcomputer.clientcommands.platform.CRenderContext;
import net.earthcomputer.clientcommands.platform.CRenderContext;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.phys.Vec3;

public abstract class Shape {
    int deathTime;
    protected Vec3 prevPos = Vec3.ZERO;

    public void tick() {
    }

    public abstract RenderState extract(CRenderContext context);

    public abstract Vec3 getPos();

    public interface RenderState {
        void render(CRenderContext context, RenderType renderType);
    }
}
