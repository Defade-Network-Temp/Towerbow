package net.defade.towerbow.games;

import net.defade.towerbow.utils.Messager;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;

public class GameEvents {
    private final GameInstance instance;
    private final EventNode<Event> parentNode;
    private final EventNode<Event> generalEventNode;
    private final EventNode<InstanceEvent> instanceEventEventNode;
    private final EventNode<PlayerEvent> playerInstanceNode;
    private final Tag<Double> fallingTag = Tag.Double("playerBlockFalling");

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

        playerInstanceNode.addListener(ItemDropEvent.class, event -> event.setCancelled(true));

        playerInstanceNode.addListener(PlayerMoveEvent.class, event -> {
           boolean isOnGround = event.isOnGround();
           if (isOnGround && event.getPlayer().hasTag(fallingTag)) {
               int damage = (int) ((event.getPlayer().getTag(fallingTag)) - event.getPlayer().getPosition().y() - 3.0)/3;
               event.getPlayer().removeTag(fallingTag);
               if (damage > 0) instance.damagePlayer(event.getPlayer(),null,DamageType.GRAVITY, damage);
           } else if (!isOnGround && !event.getPlayer().hasTag(fallingTag)) {
               event.getPlayer().setTag(fallingTag, event.getPlayer().getPosition().y());
           }
        });

        playerInstanceNode.addListener(PlayerPreEatEvent.class, event -> {
            if (instance.getGameStatus().isPlaying() && event.getItemStack().material() == Material.GOLDEN_APPLE) {
                Player player = event.getPlayer();
                float health = player.getHealth();
                float maxHealth = player.getMaxHealth();
                if (health == maxHealth) {
                    event.setCancelled(true);
                    Messager.sendPlayerNotificationWarning(player, Component.text("Health is full!"));
                }
            }
        });
        playerInstanceNode.addListener(PlayerEatEvent.class, event -> {
            if (instance.getGameStatus().isPlaying() && event.getItemStack().material() == Material.GOLDEN_APPLE) {
                Player player = event.getPlayer();
                float health = player.getHealth();
                float maxHealth = player.getMaxHealth();
                if (health < maxHealth) {
                    player.setHealth(maxHealth);
                    event.getItemStack().consume(1);
                }
            }
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
