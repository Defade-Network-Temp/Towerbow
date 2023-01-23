package net.defade.towerbow.games;

import net.defade.towerbow.utils.Items;
import net.defade.towerbow.utils.Team;
import net.defade.towerbow.utils.Utils;
import net.defade.towerbow.map.TowerbowMapGenerator;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;

import java.util.*;

public class GameTimeline {
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
                if (instance.getTPlayers().size() >= Utils.MIN_PLAYER) waitingPlayersOpt();
            }
            return getStatus() != GameStatus.WAITING_PLAYERS_FOR_DEMO;
        }).build());
    }

    private void waitingPlayersOpt() {
        setStatus(GameStatus.WAITING_PLAYER_OPTIONAL);

        Utils.count = 30;
        Utils.currentMillis = System.currentTimeMillis();

        Task task = MinecraftServer.getSchedulerManager().scheduleTask(this::starting, TaskSchedule.seconds(30), TaskSchedule.stop());

        events.getGeneralEventNode().addListener(EventListener.builder(PlayerSpawnEvent.class).expireWhen(event -> {
            if (event.getSpawnInstance().equals(instance)) {
                instance.addTPlayer(event.getPlayer(), Team.NOT);
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
        //DEBUG
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
