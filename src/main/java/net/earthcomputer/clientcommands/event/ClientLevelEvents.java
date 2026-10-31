package net.earthcomputer.clientcommands.event;

import net.earthcomputer.clientcommands.compat.CEvent;
import net.earthcomputer.clientcommands.compat.CEventFactory;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

public final class ClientLevelEvents {
    public static final CEvent<LoadLevel> LOAD_LEVEL = CEventFactory.createArrayBacked(LoadLevel.class, listeners -> level -> {
        for (LoadLevel listener : listeners) {
            listener.onLoadLevel(level);
        }
    });

    public static final CEvent<UnloadLevel> UNLOAD_LEVEL = CEventFactory.createArrayBacked(UnloadLevel.class, listeners -> isDisconnect -> {
        for (UnloadLevel listener : listeners) {
            listener.onUnloadLevel(isDisconnect);
        }
    });

    public static final CEvent<ChunkUpdate> CHUNK_UPDATE = CEventFactory.createArrayBacked(ChunkUpdate.class, listeners -> (level, pos, oldState, newState) -> {
        for (ChunkUpdate listener : listeners) {
            listener.onBlockStateUpdate(level, pos, oldState, newState);
        }
    });

    public static final CEvent<LoadChunk> LOAD_CHUNK = CEventFactory.createArrayBacked(LoadChunk.class, listeners -> (level, pos) -> {
        for (LoadChunk listener : listeners) {
            listener.onLoadChunk(level, pos);
        }
    });

    public static final CEvent<UnloadChunk> UNLOAD_CHUNK = CEventFactory.createArrayBacked(UnloadChunk.class, listeners -> (level, pos) -> {
        for (UnloadChunk listener : listeners) {
            listener.onUnloadChunk(level, pos);
        }
    });

    @FunctionalInterface
    public interface LoadLevel {
        void onLoadLevel(ClientLevel level);
    }

    @FunctionalInterface
    public interface UnloadLevel {
        void onUnloadLevel(boolean isDisconnect);
    }

    @FunctionalInterface
    public interface ChunkUpdate {
        void onBlockStateUpdate(ClientLevel level, BlockPos pos, BlockState oldState, BlockState newState);
    }

    @FunctionalInterface
    public interface LoadChunk {
        void onLoadChunk(ClientLevel level, ChunkPos chunkPos);
    }

    @FunctionalInterface
    public interface UnloadChunk {
        void onUnloadChunk(ClientLevel level, ChunkPos chunkPos);
    }
}
