package net.defade.towerbow.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.entity.Player;

public class Messager {
    public static void sendWarningFromGame(Player player, TextComponent message) {
        player.sendMessage(
                Component.text("⋗").color(TextColor.color(255, 217, 0))
                        .append(message.color(TextColor.color(255, 217, 0)))
        );
    }
}
