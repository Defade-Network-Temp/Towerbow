package net.defade.towerbow.games;

import net.defade.towerbow.utils.Team;
import net.defade.towerbow.utils.Utils;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.network.packet.server.play.SoundEffectPacket;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import net.minestom.server.world.DimensionType;
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

    public GameInstance(GameManager gameManager) {
        super(UUID.randomUUID(), DimensionType.OVERWORLD);
        this.gameManager = gameManager;
        events = new GameEvents(this, MinecraftServer.getGlobalEventHandler());
        timeline = new GameTimeline(this);
        this.displayer = new GameDisplayer(this);
        pvpHandler = new GamePvpHandler(this);
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
        player.setTag(Tag.UUID("uuid"), player.getUuid());
        players.put(player, team);
    }

    public void destroy() {
        events.unregister();
        displayer.destroy();
    }

    public void addBlock(Point point) {
        blocks.put(point, System.currentTimeMillis());
    }

    public void removeBlock(Point point) {
        blocks.remove(point);
    }
}
