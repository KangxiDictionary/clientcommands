package net.earthcomputer.clientcommands.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.xpple.clientarguments.arguments.CSuggestionProviders;
import net.earthcomputer.clientcommands.platform.CClientCommandSource;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

import static com.mojang.brigadier.arguments.FloatArgumentType.*;
import static dev.xpple.clientarguments.arguments.CIdentifierArgument.*;
import static dev.xpple.clientarguments.arguments.CVec3Argument.*;
import static net.earthcomputer.clientcommands.platform.ClientCommands.*;

public class CPlaySoundCommand {

    public static void register(CommandDispatcher<CClientCommandSource> dispatcher) {
        var builder = argument("sound", id())
            .suggests(CSuggestionProviders.AVAILABLE_SOUNDS);

        for (SoundSource source : SoundSource.values()) {
            builder.then(buildArguments(source));
        }

        dispatcher.register(literal("cplaysound").then(builder));
    }

    private static LiteralArgumentBuilder<CClientCommandSource> buildArguments(SoundSource source) {
        return literal(source.getName())
            .executes(ctx -> playSound(ctx.getSource(), getId(ctx, "sound"), source, ctx.getSource().getClientPlayer().position(), 1, 1))
            .then(argument("pos", vec3())
                .executes(ctx -> playSound(ctx.getSource(), getId(ctx, "sound"), source, getVec3(ctx, "pos"), 1, 1))
                .then(argument("volume", floatArg(0))
                    .executes(ctx -> playSound(ctx.getSource(), getId(ctx, "sound"), source, getVec3(ctx, "pos"), getFloat(ctx, "volume"), 1))
                    .then(argument("pitch", floatArg(0, 2))
                        .executes(ctx -> playSound(ctx.getSource(), getId(ctx, "sound"), source, getVec3(ctx, "pos"), getFloat(ctx, "volume"), getFloat(ctx, "pitch"))))));
    }

    private static int playSound(CClientCommandSource source, Identifier sound, SoundSource soundSource, Vec3 pos, float volume, float pitch) {
        SoundInstance soundInstance = new SimpleSoundInstance(sound, soundSource, volume, pitch, RandomSource.create(), false, 0, SoundInstance.Attenuation.LINEAR, pos.x(), pos.y(), pos.z(), false);
        source.getClient().getSoundManager().play(soundInstance);

        source.sendFeedback(Component.translatable("commands.cplaysound.success", sound));

        return Command.SINGLE_SUCCESS;
    }

}
