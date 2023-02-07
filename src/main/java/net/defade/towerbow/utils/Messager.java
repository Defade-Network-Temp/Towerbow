package net.defade.towerbow.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.advancements.FrameType;
import net.minestom.server.advancements.notifications.Notification;
import net.minestom.server.advancements.notifications.NotificationCenter;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;

public class Messager {
    private static final Tag<Long> NOTIF_TAG = Tag.Long("last_notif");
    public static void sendWarningFromGame(Player player, TextComponent message) {
        player.sendMessage(
                Component.text("⋗").color(TextColor.color(255, 217, 0))
                        .append(message.color(TextColor.color(255, 217, 0)))
        );
    }
    public static void sendPlayerNotificationImpossible(Player player, TextComponent text) {
        if (canPlayerBeNotif(player)) NotificationCenter.send(new Notification(text.color(TextColor.color(255, 0, 0)), FrameType.GOAL, ItemStack.of(Material.BARRIER)), player);
    }
    public static void sendPlayerNotificationWarning(Player player, TextComponent text) {
        if (canPlayerBeNotif(player)) NotificationCenter.send(new Notification(text.color(TextColor.color(255, 188, 0)), FrameType.GOAL, ItemStack.of(Material.SKELETON_SKULL)), player);
    }

    private static boolean canPlayerBeNotif(Player player) {
        if (!player.hasTag(NOTIF_TAG) || System.currentTimeMillis() - player.getTag(NOTIF_TAG) >= 5000) {
            player.setTag(NOTIF_TAG, System.currentTimeMillis());
            return true;
        } else return false;
    }
}
