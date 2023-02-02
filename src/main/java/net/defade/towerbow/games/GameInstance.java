package net.defade.towerbow.games;

import net.defade.towerbow.utils.Team;
import net.defade.towerbow.utils.Utils;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.world.DimensionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameInstance extends InstanceContainer {
    private final  GameManager gameManager;
    private final GameTimeline timeline;
    private final GameDisplayer displayer;

    private final GameEvents events;
    private final GamePvpHandler pvpHandler;
    private GameStatus gameStatus = GameStatus.CREATING;
    private final Map<Player, Team> players = new HashMap<>();
    private final Map<Point, Long> blocks = new HashMap<>();
    private  Task clock;

    public GameInstance(GameManager gameManager) {
        super(UUID.randomUUID(), DimensionType.OVERWORLD);
        this.gameManager = gameManager;
        events = new GameEvents(this, MinecraftServer.getGlobalEventHandler());
        timeline = new GameTimeline(this);
        this.displayer = new GameDisplayer(this);
        pvpHandler = new GamePvpHandler(this);
        launchClock();
    }

    public boolean containsTP(Player player) {
        return players.containsKey(player);
    }

    public boolean canAcceptPlayers() {
        return gameStatus.canAcceptPlayers() && players.size() < Utils.MAX_PLAYER;
    }

    public void postInit() {
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }
    void setGameStatus(GameStatus gameStatus) {
        if (!gameStatus.equals(this.gameStatus)) {
            this.gameStatus = gameStatus;
        }
    }

    public GameEvents getEvents() {
        return events;
    }


    public Map<Player, Team> getTPlayers() {
        return players;
    }

    public void addTPlayer(Player player, Team team) {
        players.put(player, team);
    }

    public void destroy() {
        events.unregister();
        displayer.destroy();
        clock.cancel();
    }

    public void bowDamage(Player victim, Player shooter, int damage) {
        if (victim.getUuid().equals(shooter.getUuid())) {
            damagePlayer(shooter, DamageType.fromPlayer(shooter), 0);
            shooter.playSound(Sound.sound(SoundEvent.ENTITY_ARROW_HIT, Sound.Source.NEUTRAL, 1.0f, 1.0f));
        } else if (sameTeamDiffPlayer(shooter, victim)) {
            damagePlayer(shooter, DamageType.fromPlayer(shooter), damage);
            shooter.playSound(Sound.sound(SoundEvent.BLOCK_NOTE_BLOCK_BASEDRUM, Sound.Source.NEUTRAL, 1.0f, 1.0f));
        } else {
            damagePlayer(victim, DamageType.fromPlayer(shooter), damage);
            shooter.playSound(Sound.sound(SoundEvent.ENTITY_ARROW_HIT_PLAYER, Sound.Source.NEUTRAL, 1.0f, 1.0f));
        }
    }

    private void damagePlayer(Player player,DamageType type, int damage) {
        if (player.getHealth() - damage > 0) {
            player.damage(type, damage);
        } else {
            //TODO implement death
            player.setHealth(player.getMaxHealth());
        }
    }

    private boolean sameTeamDiffPlayer(Player p1, Player p2) {
        return players.get(p2) == players.get(p1) && !p2.getUuid().equals(p1.getUuid());
    }

    public void addBlock(Point point) {
        blocks.put(point, System.currentTimeMillis());
    }

    public void removeBlock(Point point) {
        blocks.remove(point);
    }

    public boolean isPvpOn() {
        return gameStatus.isPlaying() && gameStatus != GameStatus.STARTING;
    }

    private void launchClock() {
        clock = MinecraftServer.getSchedulerManager().submitTask(()-> {
            long now = System.currentTimeMillis();
            for (Point point : new ArrayList<>(blocks.keySet())) {
                if (now - blocks.get(point) > 3*60*1000) {
                    super.setBlock(point, Block.AIR);
                    blocks.remove(point);
                    //TODO Do a block destroy animation
                } else if (now - blocks.get(point) > (2*60+55)*1000) {
                    super.setBlock(point, Block.MOSSY_COBBLESTONE);
                    //TODO Do a sound when the block transforms : .playSound(Sound.sound(SoundEvent.BLOCK_MOSS_PLACE, Sound.Source.NEUTRAL, 1.0f, 1.0f));
                }
            }
           return TaskSchedule.tick(2);
        });
    }
}
