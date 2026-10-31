package net.earthcomputer.clientcommands.event;

import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.ExperienceOrb;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;

public final class MoreClientEntityEvents {
    private MoreClientEntityEvents() {
    }

    /** Posted twice, first on the network thread and then on the main thread. */
    public static final class AddEntity extends Event {
        public final ClientboundAddEntityPacket packet;
        public final boolean networkThread;

        public AddEntity(ClientboundAddEntityPacket packet, boolean networkThread) {
            this.packet = packet;
            this.networkThread = networkThread;
        }
    }

    public static final class SetInitialXpOrbValue extends Event {
        public final ExperienceOrb orb;

        public SetInitialXpOrbValue(ExperienceOrb orb) {
            this.orb = orb;
        }
    }

    @FunctionalInterface
    public interface AddEntityListener {
        void onAddEntity(ClientboundAddEntityPacket packet);
    }

    @FunctionalInterface
    public interface SetInitialXpOrbValueListener {
        void onSetInitialXpOrbValue(ExperienceOrb orb);
    }

    public static void registerPostAdd(AddEntityListener listener) {
        NeoForge.EVENT_BUS.addListener(AddEntity.class, event -> {
            if (!event.networkThread) {
                listener.onAddEntity(event.packet);
            }
        });
    }

    public static void registerPreAddMaybeOnNetworkThread(AddEntityListener listener) {
        NeoForge.EVENT_BUS.addListener(AddEntity.class, event -> {
            if (event.networkThread) {
                listener.onAddEntity(event.packet);
            }
        });
    }

    public static void registerPostSetInitialXpOrbValue(SetInitialXpOrbValueListener listener) {
        NeoForge.EVENT_BUS.addListener(SetInitialXpOrbValue.class, event -> listener.onSetInitialXpOrbValue(event.orb));
    }

    public static void postAddEntity(ClientboundAddEntityPacket packet, boolean networkThread) {
        NeoForge.EVENT_BUS.post(new AddEntity(packet, networkThread));
    }

    public static void postSetInitialXpOrbValue(ExperienceOrb orb) {
        NeoForge.EVENT_BUS.post(new SetInitialXpOrbValue(orb));
    }
}
