package net.defade.towerbow.games;

import net.defade.towerbow.players.TPlayer;
import net.defade.towerbow.utils.Utils;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.world.DimensionType;

import java.util.*;

public class GameInstance extends InstanceContainer {
    private final GameManager gameManager;
    private final GameTimeline timeline;
    private final GameDisplayer displayer;

    private final GameEvents events;
    private final GamePvpHandler pvpHandler;
    private GameStatus gameStatus = GameStatus.CREATING;
    private final List<TPlayer> players = new ArrayList<>();
    private final Map<Point, Long> blocks = new HashMap<>();
    private final GameClocks clocks;

    public GameInstance(GameManager gameManager) {
        super(UUID.randomUUID(), DimensionType.OVERWORLD);
        this.gameManager = gameManager;
        events = new GameEvents(this, MinecraftServer.getGlobalEventHandler());
        timeline = new GameTimeline(this);
        this.displayer = new GameDisplayer(this);
        pvpHandler = new GamePvpHandler(this);
        clocks = new GameClocks(this);
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


    public List<TPlayer> getTPlayers() {
        return players;
    }

    public void addTPlayer(Player player) {
        players.add((TPlayer) player);
    }

    public void destroy() {
        events.unregister();
        displayer.destroy();
        clocks.destroy();
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

    public Map<Point, Long> getBlocks() {
        return blocks;
    }
}
