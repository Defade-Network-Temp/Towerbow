package net.defade.towerbow.games;

import net.defade.towerbow.players.TPlayer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class GameManager {
    private static final int MAX_INSTANCE_PER_SERVER = 15;
    private static long CURRENT_TICK = 0;
    private final Task tickClock;
    private final Set<GameInstance> gameInstances = new HashSet<>();

    public GameManager() {
        MinecraftServer.getConnectionManager().setPlayerProvider(TPlayer::new);
        updateGameInstances();
        MinecraftServer.getGlobalEventHandler().addListener(PlayerLoginEvent.class, playerLoginEvent -> {
            Player player = playerLoginEvent.getPlayer();

            GameInstance gameInstance = getAvailableInstance();

            if (gameInstance != null) {
                playerLoginEvent.setSpawningInstance(gameInstance);
            } else player.kick("Manager can't find a game");
        });

        tickClock = MinecraftServer.getSchedulerManager().submitTask(() -> {
            CURRENT_TICK++;
            return TaskSchedule.tick(1);
        });
    }

    public void updateGameInstances() {
        // Check how many game instances are available to join
        long availableGamesCount = gameInstances.stream().filter(GameInstance::canAcceptPlayers).count();
        // If there is less than two, create a new instance
        if (availableGamesCount < 2 && gameInstances.size() <= MAX_INSTANCE_PER_SERVER) createGameInstance();
    }

    private void createGameInstance() {
        GameInstance gameInstance = new GameInstance(this); // Create a new game
        MinecraftServer.getInstanceManager().registerInstance(gameInstance); // register the instance
        gameInstance.postInit(); // call the post init method to set the instance ready
        gameInstances.add(gameInstance); // add the instance to the list
        updateGameInstances(); // update the instances
    }

    void destroyGameInstance(GameInstance gameInstance) {
        gameInstance.destroy();
        MinecraftServer.getInstanceManager().unregisterInstance(gameInstance); // unregister the instance
        this.gameInstances.remove(gameInstance); // remove the instance from the list
        updateGameInstances(); // update the instances
    }

    @Nullable
    public GameInstance getAvailableInstance() {
        for (GameInstance gameInstance : gameInstances) {
            if (gameInstance.canAcceptPlayers()) return gameInstance;
        }
        return null;
    }

    public void destroy(String reason) {
        tickClock.cancel();
    }

    public static long currentTick() {
        return CURRENT_TICK;
    }
}
