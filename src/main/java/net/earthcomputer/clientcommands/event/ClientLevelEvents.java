package net.earthcomputer.clientcommands.event;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;

import java.util.function.Consumer;

/**
 * Client level lifecycle events. They are ordinary NeoForge events posted on
 * {@link NeoForge#EVENT_BUS}; the {@code register*} helpers only adapt the bus to a typed listener so
 * call sites do not have to unwrap the event object.
 */
public final class ClientLevelEvents {
    private ClientLevelEvents() {
    }

    public static final class LoadLevel extends Event {
        public final ClientLevel level;

        public LoadLevel(ClientLevel level) {
            this.level = level;
        }
    }

    public static final class UnloadLevel extends Event {
        public final boolean isDisconnect;

        public UnloadLevel(boolean isDisconnect) {
            this.isDisconnect = isDisconnect;
        }
    }

    public static final class ChunkUpdate extends Event {
        public final ClientLevel level;
        public final BlockPos pos;
        public final BlockState oldState;
        public final BlockState newState;

        public ChunkUpdate(ClientLevel level, BlockPos pos, BlockState oldState, BlockState newState) {
            this.level = level;
            this.pos = pos;
            this.oldState = oldState;
            this.newState = newState;
        }
    }

    public static final class LoadChunk extends Event {
        public final ClientLevel level;
        public final ChunkPos chunkPos;

        public LoadChunk(ClientLevel level, ChunkPos chunkPos) {
            this.level = level;
            this.chunkPos = chunkPos;
        }
    }

    public static final class UnloadChunk extends Event {
        public final ClientLevel level;
        public final ChunkPos chunkPos;

        public UnloadChunk(ClientLevel level, ChunkPos chunkPos) {
            this.level = level;
            this.chunkPos = chunkPos;
        }
    }

    @FunctionalInterface
    public interface ChunkUpdateListener {
        void onBlockStateUpdate(ClientLevel level, BlockPos pos, BlockState oldState, BlockState newState);
    }

    @FunctionalInterface
    public interface ChunkListener {
        void onChunk(ClientLevel level, ChunkPos chunkPos);
    }

    public static void registerLoadLevel(Consumer<ClientLevel> listener) {
        NeoForge.EVENT_BUS.addListener(LoadLevel.class, event -> listener.accept(event.level));
    }

    public static void registerUnloadLevel(Consumer<Boolean> listener) {
        NeoForge.EVENT_BUS.addListener(UnloadLevel.class, event -> listener.accept(event.isDisconnect));
    }

    public static void registerChunkUpdate(ChunkUpdateListener listener) {
        NeoForge.EVENT_BUS.addListener(ChunkUpdate.class,
            event -> listener.onBlockStateUpdate(event.level, event.pos, event.oldState, event.newState));
    }

    public static void registerLoadChunk(ChunkListener listener) {
        NeoForge.EVENT_BUS.addListener(LoadChunk.class, event -> listener.onChunk(event.level, event.chunkPos));
    }

    public static void registerUnloadChunk(ChunkListener listener) {
        NeoForge.EVENT_BUS.addListener(UnloadChunk.class, event -> listener.onChunk(event.level, event.chunkPos));
    }

    public static void postLoadLevel(ClientLevel level) {
        NeoForge.EVENT_BUS.post(new LoadLevel(level));
    }

    public static void postUnloadLevel(boolean isDisconnect) {
        NeoForge.EVENT_BUS.post(new UnloadLevel(isDisconnect));
    }

    public static void postChunkUpdate(ClientLevel level, BlockPos pos, BlockState oldState, BlockState newState) {
        NeoForge.EVENT_BUS.post(new ChunkUpdate(level, pos, oldState, newState));
    }

    public static void postLoadChunk(ClientLevel level, ChunkPos chunkPos) {
        NeoForge.EVENT_BUS.post(new LoadChunk(level, chunkPos));
    }

    public static void postUnloadChunk(ClientLevel level, ChunkPos chunkPos) {
        NeoForge.EVENT_BUS.post(new UnloadChunk(level, chunkPos));
    }
}
