package net.earthcomputer.clientcommands.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.earthcomputer.clientcommands.compat.CClientTickEvents;
import net.earthcomputer.clientcommands.compat.CRenderContext;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.CustomFeatureRenderer;
import net.minecraft.client.renderer.oit.OitPipelineSet;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RenderQueue {
    private static int tickCounter = 0;
    private static final List<AddQueueEntry> addQueue = new ArrayList<>();
    private static final List<RemoveQueueEntry> removeQueue = new ArrayList<>();
    private static final EnumMap<Layer, Map<Object, Shape>> queue = new EnumMap<>(Layer.class);
    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();
    private static boolean submitLogged;

    private static final RenderPipeline LINES_NO_DEPTH_PIPELINE = RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath("clientcommands", "pipeline/lines_no_depth"))
        .withColorTargetState(ColorTargetState.DEFAULT)
        .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, true))
        .build();
    private static final OitPipelineSet LINES_NO_DEPTH_OIT_PIPELINE = OitPipelineSet.builder(
        "clientcommands_no_depth",
        RenderPipeline.builder(RenderPipelines.OIT_LINES_SNIPPET)
    )
        .withoutDepthTest()
        .build();
    public static final RenderType LINES_NO_DEPTH_LAYER = RenderType.create(
        "clientcommands_no_depth",
        RenderSetup.builder(LINES_NO_DEPTH_PIPELINE)
            .setOitPipelines(LINES_NO_DEPTH_OIT_PIPELINE)
            .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
            .createRenderSetup()
    );

    static {
        CClientTickEvents.START_CLIENT_TICK.register(RenderQueue::tick);
    }

    public static void register() {
        // load class
    }

    public static void add(Layer layer, Object key, Shape shape, int life) {
        addQueue.add(new AddQueueEntry(layer, key, shape, life));
    }

    public static void addCuboid(Layer layer, Object key, Vec3 from, Vec3 to, int color, int life) {
        add(layer, key, new Cuboid(from, to, color), life);
    }

    public static void addCuboid(Layer layer, Object key, AABB cuboid, int color, int life) {
        add(layer, key, new Cuboid(cuboid, color), life);
    }

    public static void addLine(Layer layer, Object key, Vec3 from, Vec3 to, int color, int life) {
        add(layer, key, new Line(from, to, color), life);
    }

    public static void remove(Layer layer, Object key) {
        removeQueue.add(new RemoveQueueEntry(layer, key));
    }

    private static void doAdd(AddQueueEntry entry) {
        Map<Object, Shape> shapes = queue.computeIfAbsent(entry.layer(), k -> new LinkedHashMap<>());
        Shape oldShape = shapes.get(entry.key());
        if (oldShape != null) {
            entry.shape().prevPos = oldShape.prevPos;
        } else {
            entry.shape().prevPos = entry.shape().getPos();
        }
        entry.shape().deathTime = tickCounter + entry.life();
        shapes.put(entry.key(), entry.shape());
    }

    private static void tick(Minecraft mc) {
        for (RemoveQueueEntry entry : removeQueue) {
            Map<Object, Shape> shapes = queue.get(entry.layer());
            if (shapes != null) {
                shapes.remove(entry.key());
            }
        }
        removeQueue.clear();

        queue.values().forEach(shapes -> shapes.values().forEach(shape -> shape.prevPos = shape.getPos()));
        tickCounter++;
        for (AddQueueEntry entry : addQueue) {
            doAdd(entry);
        }
        addQueue.clear();
        for (Map<Object, Shape> shapes : queue.values()) {
            Iterator<Shape> itr = shapes.values().iterator();
            while (itr.hasNext()) {
                Shape shape = itr.next();
                if (tickCounter == shape.deathTime) {
                    itr.remove();
                }
                shape.tick();
            }
        }
    }

    /**
     * Called from the LevelRenderer mixin at the end of the feature submission phase,
     * where both the LevelRenderState and the SubmitNodeCollector are available.
     */
    public static void submit(PoseStack poseStack, SubmitNodeCollector collector, LevelRenderState levelState) {
        if (!submitLogged) {
            submitLogged = true;
            int shapes = queue.values().stream().mapToInt(Map::size).sum();
            LOGGER.info("[clientcommands] render submission reached, {} queued shape(s)", shapes);
        }
        Minecraft mc = Minecraft.getInstance();
        CRenderContext context = new CRenderContext(poseStack, mc.gameRenderer.mainCamera(),
            mc.getDeltaTracker(), collector, levelState);

        for (Map.Entry<Layer, Map<Object, Shape>> entry : queue.entrySet()) {
            for (Shape shape : entry.getValue().values()) {
                shape.extract(context).render(context, LINES_NO_DEPTH_LAYER);
            }
        }
    }

    public static void submitCustomGeometry(
        SubmitNodeCollector collector,
        PoseStack poseStack,
        RenderType renderType,
        SubmitNodeCollector.CustomGeometryRenderer renderer
    ) {
        if (renderType == LINES_NO_DEPTH_LAYER && collector.order(0) instanceof SubmitNodeCollection collection) {
            collection.afterTerrain.submit(new CustomFeatureRenderer.Submit(poseStack.last().copy(), renderType, renderer));
        } else {
            collector.submitCustomGeometry(poseStack, renderType, renderer);
        }
    }

    public enum Layer {
        ON_TOP
    }

    private record AddQueueEntry(Layer layer, Object key, Shape shape, int life) {}

    private record RemoveQueueEntry(Layer layer, Object key) {}
}
