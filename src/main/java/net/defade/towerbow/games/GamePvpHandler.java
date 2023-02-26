package net.defade.towerbow.games;

import net.defade.towerbow.pvp.TowerbowArrow;
import net.defade.towerbow.utils.Messager;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.item.ItemUpdateStateEvent;
import net.minestom.server.event.player.PlayerItemAnimationEvent;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;

import java.util.Objects;
/*
Most of the code from this class is inspired by MinestomPVP.
MinestomPVP repo : https://github.com/TogAr2/MinestomPvP
Inspired from : https://github.com/TogAr2/MinestomPvP/blob/b5bff43012f0a826aaf25a89dcfde2b0aaac09a4/src/main/java/io/github/bloepiloepi/pvp/projectile/ProjectileListener.java
*/

public class GamePvpHandler {
    private static final Tag<Long> BOW_USE_START_TIME = Tag.Long("ItemUseStartTime");

    private final GameInstance instance;
    private final GameEvents events;

    public GamePvpHandler(GameInstance instance) {
        this.instance = instance;
        this.events = instance.getEvents();
        addTrackerListener();
        addBowListener();
    }

    private void addTrackerListener() {
        events.getPlayerInstanceNode().addListener(PlayerItemAnimationEvent.class, event -> {
            if (event.getItemAnimationType() == PlayerItemAnimationEvent.ItemAnimationType.BOW) {
                event.getPlayer().setTag(BOW_USE_START_TIME, System.currentTimeMillis());
            }
        });
    }

    private void addBowListener() {
        events.getPlayerInstanceNode().addListener(EventListener.builder(ItemUpdateStateEvent.class).handler(event -> {
            Player player = event.getPlayer();
            if (!instance.getGameStatus().isPlaying()) return;
            if (instance.getGameStatus() == GameStatus.STARTING) {
                Messager.sendWarningFromGame(player, Component.text("PVP off for the moment"));
                return;
            }

            long useDuration = System.currentTimeMillis() - player.getTag(BOW_USE_START_TIME);
            double power = getBowPower(useDuration);
            if (power < 0.1) return;
            TowerbowArrow arrow = new TowerbowArrow(instance, player);

            Pos position = player.getPosition().add(0D, player.getEyeHeight(), 0D);
            arrow.setInstance(Objects.requireNonNull(player.getInstance()),
                    position.sub(0, 0.1D, 0));

            Vec direction = position.direction();
            position = position.add(direction).sub(0, 0.2, 0);

            arrow.shoot(position, power * 3, 0.0);

            player.playSound(Sound.sound(SoundEvent.ENTITY_ARROW_SHOOT, Sound.Source.PLAYER, 1.0f, 1.0f));

            Vec playerVel = player.getVelocity();
            arrow.setVelocity(arrow.getVelocity().add(playerVel.x(),
                    player.isOnGround() ? 0.0D : playerVel.y(), playerVel.z()));

        }).filter(event -> event.getItemStack().material() == Material.BOW).build());
    }



    public double getBowPower(long useDurationMillis) {
        double seconds = useDurationMillis / 1000.0;
        double power = (seconds * seconds + seconds * 2.0) / 3.0;
        if (power > 1) {
            power = 1;
        }

        return power;
    }


}
