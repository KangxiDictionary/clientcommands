package dev.xpple.clientarguments.arguments;

import net.earthcomputer.clientcommands.compat.CClientCommandSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public interface CCoordinates {
	Vec3 getPosition(CClientCommandSource source);

	Vec2 getRotation(CClientCommandSource source);

	default BlockPos getBlockPos(CClientCommandSource source) {
		return BlockPos.containing(this.getPosition(source));
	}

	boolean isXRelative();

	boolean isYRelative();

	boolean isZRelative();
}
