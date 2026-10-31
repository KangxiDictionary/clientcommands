package net.earthcomputer.clientcommands.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.earthcomputer.clientcommands.Configs;
import net.earthcomputer.clientcommands.features.ServerBrandManager;
import net.earthcomputer.clientcommands.features.CCrackRng;
import net.earthcomputer.clientcommands.features.PlayerRandCracker;
import net.earthcomputer.clientcommands.platform.CClientCommandSource;
import net.minecraft.network.chat.Component;

import static net.earthcomputer.clientcommands.platform.ClientCommands.*;

public class CrackRNGCommand {

    public static void register(CommandDispatcher<CClientCommandSource> dispatcher) {
        dispatcher.register(literal("ccrackrng")
            .executes(ctx -> crackPlayerRNG(ctx.getSource())));
    }

    private static int crackPlayerRNG(CClientCommandSource source) throws CommandSyntaxException {
        ServerBrandManager.rngWarning();
        CCrackRng.crack(seed -> {
            source.sendFeedback(Component.translatable("commands.ccrackrng.success", Long.toHexString(seed)));
            PlayerRandCracker.setSeed(seed);
            Configs.playerCrackState = PlayerRandCracker.CrackState.CRACKED;
        });
        return Command.SINGLE_SUCCESS;
    }

}
