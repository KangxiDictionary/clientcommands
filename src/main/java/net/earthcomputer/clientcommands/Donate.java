package net.earthcomputer.clientcommands;

import net.earthcomputer.clientcommands.command.ClientCommandHelper;
import net.earthcomputer.clientcommands.util.CComponentUtil;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.network.chat.Component;

public final class Donate {
    private Donate() {
    }

    public static void registerEvents() {
        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingIn.class, event -> run());
    }

    private static void run() {
        if (Configs.donationMessage) {
            ClientCommandHelper.sendHelp(
                Component.translatable(
                    "commands.client.donate",
                    CComponentUtil.getLinkTextComponent("commands.client.donate.link", "https://ko-fi.com/earthcomputer")
                )
            );

            Configs.donationMessage = false;
            Configs.save();
        }
    }
}
