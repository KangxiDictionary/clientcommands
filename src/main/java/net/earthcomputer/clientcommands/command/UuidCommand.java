package net.earthcomputer.clientcommands.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.clientcommands.platform.CClientCommandSource;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

import java.util.UUID;

import static net.earthcomputer.clientcommands.command.arguments.EntityUUIDArgument.*;
import static net.earthcomputer.clientcommands.compat.ClientCommands.*;

public class UuidCommand {

    public static void register(CommandDispatcher<CClientCommandSource> dispatcher) {
        dispatcher.register(literal("cuuid")
            .then(argument("entity", entityUuid())
                .executes(ctx -> getUuid(ctx.getSource(), getEntityUuid(ctx, "entity")))));
    }

    private static int getUuid(CClientCommandSource source, UUID entity) {
        String uuid = entity.toString();
        Component uuidComponent = Component.literal(uuid).withStyle(style -> style
            .withUnderlined(true)
            .withHoverEvent(new HoverEvent.ShowText(Component.translatable("chat.copy.click")))
            .withClickEvent(new ClickEvent.CopyToClipboard(uuid))
        );

        ClientPacketListener packetListener = source.getClient().getConnection();
        assert packetListener != null;

        PlayerInfo player = packetListener.getPlayerInfo(entity);
        if (player == null) {
            source.sendFeedback(Component.translatable("commands.cuuid.success.nameless", uuidComponent));
            return Command.SINGLE_SUCCESS;
        }
        String name = player.getProfile().name();
        source.sendFeedback(Component.translatable("commands.cuuid.success", name, uuidComponent));
        return Command.SINGLE_SUCCESS;
    }
}
