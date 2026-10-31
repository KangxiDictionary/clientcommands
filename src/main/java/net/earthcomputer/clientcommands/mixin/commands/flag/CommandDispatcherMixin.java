package net.earthcomputer.clientcommands.mixin.commands.flag;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.earthcomputer.clientcommands.command.Flag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Brigadier cannot express "this flag may appear at most once", so the raw input is validated
 * before the command tree runs.
 */
@Mixin(CommandDispatcher.class)
public class CommandDispatcherMixin {
    @Inject(method = "execute(Lcom/mojang/brigadier/StringReader;Ljava/lang/Object;)I", at = @At("HEAD"))
    private void clientcommands$checkRepeatedFlags(StringReader reader, Object source, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        Flag.checkForRepeatedFlags(reader.getString());
    }
}
