package net.earthcomputer.clientcommands.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import dev.xpple.clientarguments.arguments.CCoordinates;
import net.earthcomputer.clientcommands.platform.CClientCommandSource;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec2;

import static dev.xpple.clientarguments.arguments.CBlockPosArgument.*;
import static dev.xpple.clientarguments.arguments.CRotationArgument.*;
import static net.earthcomputer.clientcommands.platform.ClientCommands.*;

public class LookCommand {

    public static void register(CommandDispatcher<CClientCommandSource> dispatcher) {
        dispatcher.register(literal("clook")
            .then(literal("block")
                .then(argument("pos", blockPos())
                    .executes(ctx -> lookBlock(ctx.getSource(), getBlockPos(ctx, "pos")))))
            .then(literal("angles")
                .then(argument("rotation", rotation())
                    .executes(ctx -> lookAngles(ctx.getSource(), getRotation(ctx, "rotation")))))
            .then(literal("cardinal")
                .then(literal("down")
                    .executes(ctx -> lookCardinal(ctx.getSource(), 90)))
                .then(literal("up")
                    .executes(ctx -> lookCardinal(ctx.getSource(), -90)))
                .then(literal("west")
                    .executes(ctx -> lookCardinal(ctx.getSource(), 90, 0)))
                .then(literal("east")
                    .executes(ctx -> lookCardinal(ctx.getSource(), -90, 0)))
                .then(literal("north")
                    .executes(ctx -> lookCardinal(ctx.getSource(), -180, 0)))
                .then(literal("south")
                    .executes(ctx -> lookCardinal(ctx.getSource(), 0, 0)))));
    }

    private static int lookBlock(CClientCommandSource source, BlockPos pos) {
        LocalPlayer player = source.getClientPlayer();
        double dx = (pos.getX() + 0.5) - player.getX();
        double dy = (pos.getY() + 0.5) - (player.getY() + player.getEyeHeight());
        double dz = (pos.getZ() + 0.5) - player.getZ();
        double dh = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, dh));
        return doLook(player, yaw, pitch);
    }

    private static int lookAngles(CClientCommandSource source, CCoordinates rotation) {
        Vec2 rot = rotation.getRotation(source);
        return doLook(source.getClientPlayer(), rot.y, rot.x);
    }

    private static int lookCardinal(CClientCommandSource source, float yaw, float pitch) {
        return doLook(source.getClientPlayer(), yaw, pitch);
    }

    private static int lookCardinal(CClientCommandSource source, float pitch) {
        return lookCardinal(source, source.getRotation().y, pitch);
    }

    private static int doLook(LocalPlayer player, float yaw, float pitch) {
        player.moveOrInterpolateTo(player.position(), yaw, pitch);
        return Command.SINGLE_SUCCESS;
    }

}
