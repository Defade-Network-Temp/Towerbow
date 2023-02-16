package net.defade.towerbow.games;

import net.defade.towerbow.players.TPlayer;
import net.defade.towerbow.utils.Team;
import net.defade.towerbow.utils.Utils;
import net.defade.towerbow.map.TowerbowMapGenerator;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GameTimeline {
    private static final double GRAVITY_ACC = 0.08;
    private static final double GRAVITY_DRA = 0.02;
    private final GameInstance instance;
    private final GameEvents events;
    private final List<TPlayer> players;

    public GameTimeline(GameInstance instance) {
        this.instance = instance;
        events = instance.getEvents();
        players = instance.getTPlayers();
        setup();
    }

    private void setup() {
        TowerbowMapGenerator.generate(instance, 50);
        waitingPlayersForDemo();
    }

    private void waitingPlayersForDemo() {
        setStatus(GameStatus.WAITING_PLAYERS_FOR_DEMO);

        events.getGeneralEventNode().addListener(EventListener.builder(PlayerSpawnEvent.class).expireWhen(playerSpawnEvent -> {
            if (playerSpawnEvent.getSpawnInstance().equals(instance)) {
                Player player = playerSpawnEvent.getPlayer();
                instance.addTPlayer(player);
                player.teleport(new Pos(0, TowerbowMapGenerator.Y + 2, 0));
                if (instance.getTPlayers().size() >= Utils.MIN_PLAYER) waitingPlayersOpt();
            }
            return getStatus() != GameStatus.WAITING_PLAYERS_FOR_DEMO;
        }).build());
    }

    private void waitingPlayersOpt() {
        setStatus(GameStatus.WAITING_PLAYER_OPTIONAL);

        Utils.count = 10;
        Utils.currentMillis = System.currentTimeMillis();

        Task task = MinecraftServer.getSchedulerManager().scheduleTask(this::starting, TaskSchedule.seconds(Utils.count), TaskSchedule.stop());

        events.getGeneralEventNode().addListener(EventListener.builder(PlayerSpawnEvent.class).expireWhen(event -> {
            if (event.getSpawnInstance().equals(instance)) {
                instance.addTPlayer(event.getPlayer());
                event.getPlayer().teleport(new Pos(0, TowerbowMapGenerator.Y + 2, 0));
                if (instance.getTPlayers().size() == Utils.MAX_PLAYER) {
                    task.cancel();
                    starting();
                }
            }
            return getStatus() != GameStatus.WAITING_PLAYER_OPTIONAL;
        }).build());
    }

    private void starting() {
        setStatus(GameStatus.STARTING);
        //TODO AttributeModifier modifier = new AttributeModifier("speed", 0, AttributeOperation.MULTIPLY_BASE);

        //Block players
        players.forEach((tPlayer) -> {
            //TODO player.getAttribute(Attribute.MOVEMENT_SPEED).addModifier(modifier);
        });
        // Create teams
        List<TPlayer> shuffled = new ArrayList<>(players);
        Collections.shuffle(shuffled);
        boolean b = true;
        for (TPlayer player : shuffled) {
            if (b) player.setGameTeam(Team.BLUE);
            else player.setGameTeam(Team.RED);
            b = !b;
        }
        // Give stuff
        for (TPlayer player : players) {
            player.setEquipment();
        }

        // Teleport teams
        players.forEach((player) -> {
            Team team = player.getGameTeam();
            int x = team.getI() * 40 + ThreadLocalRandom.current().nextInt(-2, 2);
            int y = TowerbowMapGenerator.Y;
            int z = team.getI() * 40 + ThreadLocalRandom.current().nextInt(-2, 2);
            player.teleport(new Pos(x, y + 2, z));
            player.facePosition(Player.FacePoint.EYE, new Pos(0, y, 0));
        });
        // Give darkness for 5 seconds
        players.forEach((player) -> {
            player.addEffect(new Potion(PotionEffect.DARKNESS, (byte) 1, 5 * 20));
            player.addEffect(new Potion(PotionEffect.JUMP_BOOST, (byte)2, 30 * 20));
        });
        // Remove player freeze in 5 seconds
        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            players.forEach((player) -> {
                //TODO player.getAttribute(Attribute.MOVEMENT_SPEED).removeModifier(modifier);
            });
        }, TaskSchedule.seconds(5), TaskSchedule.stop());
        // Switch to phase 1 in 30 sec
        MinecraftServer.getSchedulerManager().scheduleTask(this::phaseOne, TaskSchedule.seconds(30), TaskSchedule.stop());
    }

    private void phaseOne() {
        setStatus(GameStatus.PHASE_1);


    }

    private void setStatus(GameStatus gameStatus) {
        instance.setGameStatus(gameStatus);
    }

    private GameStatus getStatus() {
        return instance.getGameStatus();
    }
}
