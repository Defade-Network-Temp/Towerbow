package net.defade.towerbow.games;

import net.defade.towerbow.players.TPlayer;
import net.defade.towerbow.utils.Conf;
import net.defade.towerbow.utils.Utils;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.WorldBorderWarningDelayPacket;
import net.minestom.server.network.packet.server.play.WorldBorderWarningReachPacket;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;

import java.util.ArrayList;
import java.util.Map;

public class GameClocks {
    private final GameInstance instance;
    private final Task blockClock;
    private final Task dzPlayerClock;
    private final Task xpClock;

    public GameClocks(GameInstance instance) {
        this.instance = instance;

        blockClock = MinecraftServer.getSchedulerManager().submitTask(() -> {
            long now = GameManager.currentTick();
            Map<Point, Long> blocks = instance.getBlocks();
            for (Point point : new ArrayList<>(blocks.keySet())) {
                if (now - blocks.get(point) > Conf.TICKS_FOR_BLOCK_AIR) {
                    instance.setBlock(point, Block.AIR);
                    blocks.remove(point);
                    //TODO Do a block destroy animation
                } else if (!instance.getBlock(point).compare(Block.MOSSY_COBBLESTONE) && now - blocks.get(point) > Conf.TICKS_FOR_BLOCK_MOSSY) {
                    instance.setBlock(point, Block.MOSSY_COBBLESTONE);
                    Utils.sendSoundAround(instance, point, SoundEvent.BLOCK_MOSS_PLACE, Sound.Source.BLOCK, 1.0F, 0.0F, null);
                }
            }
            return TaskSchedule.tick(11);
        });

        dzPlayerClock = MinecraftServer.getSchedulerManager().submitTask(() -> {
            if (instance.getGameStatus().isPlaying() && instance.getGameStatus() != GameStatus.STARTING) {
                for (TPlayer player : instance.getTPlayers()) {
                    if (player.getPosition().y() <= Conf.DAMAGE_MIN_Y) {
                        if (player.wasInDangerZone()) {
                            if (GameManager.currentTick() - player.getTicksInDangerZone() > Conf.TICK_SAFE_DZ) {
                                player.damage(null, DamageType.VOID, 1);
                            }
                            player.sendPacket(new WorldBorderWarningDelayPacket(500));
                            player.sendPacket(new WorldBorderWarningReachPacket(1));
                        } else {
                            player.startInDangerZone(GameManager.currentTick());
                        }
                    } else {
                        if (player.wasInDangerZone()) {
                            player.setNotInDanger();
                        }
                    }
                }
            }
            return TaskSchedule.tick(20);
        });

        xpClock = MinecraftServer.getSchedulerManager().submitTask(() -> {
            if (instance.getGameStatus().isPlaying() && instance.getGameStatus() != GameStatus.STARTING) {
                for (TPlayer player : instance.getTPlayers()) {
                    if (player.getPosition().y() <= Conf.DAMAGE_MIN_Y) {
                        if (player.wasInDangerZone()) {
                            long delay = GameManager.currentTick() - player.getTicksInDangerZone();
                            float ratio = (float) (1.0 - (double) delay / (double) Conf.TICK_SAFE_DZ);
                            if (ratio < 0.0) ratio = 0.0F;
                            if (ratio > 1.0) ratio = 1.0F;
                            player.setExp(ratio);
                        } else player.setExp(1.0f);
                    } else player.setExp(1.0f);
                }
            }
            return TaskSchedule.tick(3);
        });
    }

    public void destroy() {
        blockClock.cancel();
        dzPlayerClock.cancel();
        xpClock.cancel();
    }
}
