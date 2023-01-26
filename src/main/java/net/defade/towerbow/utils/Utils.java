package net.defade.towerbow.utils;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.sound.SoundEvent;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Utils {
    public static final int MIN_PLAYER = 2;
    public static final int MAX_PLAYER = 4;

    public static int count = 0;
    public static long currentMillis = 0;

    public static void sendSoundAround(Instance instance, Point position, SoundEvent sound, Sound.Source source, float volume, float pitch, Predicate<Player> predicate) {
        double distance = volume > 1.0F ? (double) (16.0F * volume) : 16.0D;

        Predicate<Player> positionPredicate = (player) -> player.getPosition().distance(position) < distance;
        if (predicate != null) {
            predicate = predicate.and(positionPredicate);
        } else {
            predicate = positionPredicate;
        }

        Audience audience = Audience.audience(instance
                .getPlayers().stream().filter(predicate)
                .collect(Collectors.toList()));

        audience.playSound(Sound.sound(sound.key(), source, volume, pitch), position.x(), position.y(), position.z());
    }

}
