package net.earthcomputer.clientcommands.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.clientcommands.platform.CClientCommandSource;
import net.earthcomputer.clientcommands.platform.CPlatform;
import org.spongepowered.asm.mixin.MixinEnvironment;

import static net.earthcomputer.clientcommands.platform.ClientCommands.*;

public class AuditMixinsCommand {
    public static void register(CommandDispatcher<CClientCommandSource> dispatcher) {
        boolean enableAuditMixins = CPlatform.INSTANCE.isDevelopmentEnvironment() || Boolean.getBoolean("clientcommands.enableAuditMixins");
        if (!enableAuditMixins) {
            return;
        }
        dispatcher.register(literal("cauditmixins").executes(ctx -> auditMixins()));
    }

    private static int auditMixins() {
        MixinEnvironment.getCurrentEnvironment().audit();
        return Command.SINGLE_SUCCESS;
    }
}
