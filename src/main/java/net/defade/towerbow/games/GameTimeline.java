package net.defade.towerbow.games;

import net.defade.towerbow.utils.Items;
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
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class GameTimeline {
    private static final double GRAVITY_ACC = 0.08;
    private static final double GRAVITY_DRA = 0.02;
    private final GameInstance instance;
    private final GameEvents events;

    public GameTimeline(GameInstance instance) {
        this.instance = instance;
        events = instance.getEvents();
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
                instance.addTPlayer(player, Team.NOT);
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
                instance.addTPlayer(event.getPlayer(), Team.NOT);
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
        players().forEach((player, team) -> {
            //TODO player.getAttribute(Attribute.MOVEMENT_SPEED).addModifier(modifier);
        });
        // Create teams
        List<Player> shuffled = new ArrayList<>(players().keySet().stream().toList());
        Collections.shuffle(shuffled);
        boolean b = true;
        for (Player player : shuffled) {
            if (b) players().replace(player, Team.BLUE);
            else players().replace(player, Team.RED);
            b = !b;
        }
        // Give stuff
        players().forEach((player, team) -> {
            player.getInventory().setItemStack(0, Items.BOW.get(team));
            player.getInventory().setItemStack(1, Items.PICKAXE.get(team));
            player.getInventory().setHelmet(Items.HELMET.get(team));
            player.getInventory().setChestplate(Items.CHEST.get(team));
            player.getInventory().setLeggings(Items.LEGGINGS.get(team));
            player.getInventory().setBoots(Items.BOOTS.get(team));
            player.getInventory().setItemInOffHand(Items.STONE.get(team));

        });

        // Teleport teams
        players().forEach((player, team) -> {
            int x = team.getI() * 40 + ThreadLocalRandom.current().nextInt(-2, 2);
            int y = TowerbowMapGenerator.Y;
            int z = team.getI() * 40 + ThreadLocalRandom.current().nextInt(-2, 2);
            player.teleport(new Pos(x, y + 2, z));
            player.facePosition(Player.FacePoint.EYE, new Pos(0, y, 0));
        });
        // Give darkness for 5 seconds
        players().forEach((player, team) -> {
            player.addEffect(new Potion(PotionEffect.DARKNESS, (byte) 1, 5 * 20));
            player.addEffect(new Potion(PotionEffect.JUMP_BOOST, (byte)2, 30 * 20));
        });
        // Remove player freeze in 5 seconds
        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            players().forEach((player, team) -> {
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

    private Map<Player, Team> players() {
        return instance.getTPlayers();
    }
}
