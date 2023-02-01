package net.defade.towerbow.games;

import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerDeathEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.block.Block;

public class GameEvents {
    private final GameInstance instance;
    private final EventNode<Event> parentNode;
    private final EventNode<Event> generalEventNode;
    private final EventNode<InstanceEvent> instanceEventEventNode;
    private final EventNode<PlayerEvent> playerInstanceNode;

    public GameEvents(GameInstance gameInstance, EventNode<Event> parentNode) {
        this.instance = gameInstance;
        this.parentNode = parentNode;
        this.generalEventNode = EventNode.all("game");
        this.instanceEventEventNode = EventNode.type("instance-event", EventFilter.INSTANCE, (instanceEvent, instance1) -> instanceEvent.getInstance().equals(instance));
        this.playerInstanceNode = EventNode.type("instance-player", EventFilter.PLAYER, (playerEvent, player) -> gameInstance.containsTP(player));
        parentNode.addChild(generalEventNode);
        generalEventNode.addChild(playerInstanceNode);
        generalEventNode.addChild(instanceEventEventNode);
        addMainEvents();
    }

    private void addMainEvents() {
        playerInstanceNode.addListener(PlayerBlockBreakEvent.class, event -> {
            if (event.getBlock() == Block.GRAY_STAINED_GLASS) {
                event.setCancelled(true);
            } else {
                instance.removeBlock(event.getBlockPosition());
            }
        });

        playerInstanceNode.addListener(PlayerBlockPlaceEvent.class, event -> {
            if (instance.getGameStatus().isPlaying()) {
                instance.addBlock(event.getBlockPosition());
                event.consumeBlock(false);
            } else {
                event.setCancelled(true);
            }
        });

        playerInstanceNode.addListener(ItemDropEvent.class, event -> {
            event.setCancelled(true);
        });
    }

    public EventNode<Event> getGeneralEventNode() {
        return generalEventNode;
    }

    public EventNode<InstanceEvent> getInstanceEventEventNode() {
        return instanceEventEventNode;
    }

    public EventNode<PlayerEvent> getPlayerInstanceNode() {
        return playerInstanceNode;
    }

    public void unregister() {
        parentNode.removeChild(generalEventNode);
        parentNode.removeChild(playerInstanceNode);
    }

}
