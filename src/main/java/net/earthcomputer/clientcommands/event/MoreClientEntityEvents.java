package net.earthcomputer.clientcommands.event;

import net.earthcomputer.clientcommands.compat.CEvent;
import net.earthcomputer.clientcommands.compat.CEventFactory;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.ExperienceOrb;

public final class MoreClientEntityEvents {
    /**
     * Called twice, first on the network thread and then on the main thread
     */
    public static final CEvent<AddEntity> PRE_ADD_MAYBE_ON_NETWORK_THREAD = CEventFactory.createArrayBacked(AddEntity.class, listeners -> packet -> {
        for (AddEntity listener : listeners) {
            listener.onAddEntity(packet);
        }
    });

    public static final CEvent<AddEntity> POST_ADD = CEventFactory.createArrayBacked(AddEntity.class, listeners -> packet -> {
        for (AddEntity listener : listeners) {
            listener.onAddEntity(packet);
        }
    });

    public static final CEvent<SetInitialXpOrbValue> POST_SET_INITIAL_XP_ORB_VALUE = CEventFactory.createArrayBacked(SetInitialXpOrbValue.class, listeners -> orb -> {
        for (SetInitialXpOrbValue listener : listeners) {
            listener.onSetInitialXpOrbValue(orb);
        }
    });

    @FunctionalInterface
    public interface AddEntity {
        void onAddEntity(ClientboundAddEntityPacket packet);
    }

    @FunctionalInterface
    public interface SetInitialXpOrbValue {
        void onSetInitialXpOrbValue(ExperienceOrb orb);
    }
}
