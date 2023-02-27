package net.defade.towerbow.games;

import net.defade.towerbow.players.TPlayer;
import net.defade.towerbow.utils.Conf;
import net.defade.towerbow.utils.Messager;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.inventory.PlayerInventoryItemChangeEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
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
        this.playerInstanceNode = EventNode.type("instance-player", EventFilter.PLAYER, (playerEvent, player) -> gameInstance.getTPlayers().contains((TPlayer) player));
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
            TPlayer player = (TPlayer) event.getPlayer();
            checkFallDamage(event, player);

        });

        playerInstanceNode.addListener(PlayerPreEatEvent.class, event -> {
            if (instance.getGameStatus().isPlaying() && event.getItemStack().material() == Material.GOLDEN_APPLE) {
                if (event.getPlayer().getHealth() >= event.getPlayer().getMaxHealth()) event.setCancelled(true);
            }
        });
        playerInstanceNode.addListener(PlayerEatEvent.class, event -> {
            if (instance.getGameStatus().isPlaying() && event.getItemStack().material() == Material.GOLDEN_APPLE) {
                Player player = event.getPlayer();
                player.heal();

                int amount =  event.getItemStack().amount();
                byte slot = player.getHeldSlot();
                if (amount > 0) player.getInventory().setItemStack(slot, event.getItemStack().withAmount(amount - 1));
                else player.getInventory().setItemStack(slot, event.getItemStack().withMaterial(Material.AIR));

            }
        });

        playerInstanceNode.addListener(InventoryPreClickEvent.class, event -> {
            int slot = event.getSlot();
            if (slot >= 41 && slot<= 44) event.setCancelled(true);
        });
    }

    private void checkFallDamage(PlayerMoveEvent event, TPlayer player) {
        boolean isOnGround = event.isOnGround();
        if (isOnGround && player.wasFalling()) {
            int damage = (int) (player.getFalledHeight() - event.getPlayer().getPosition().y() - 3.0)/3;
            player.stopFalling();
            if (damage > 0) player.fallDamage(damage);
        } else if (!isOnGround && !player.wasFalling()) {
            player.startFalling(event.getPlayer().getPosition().y());
        }
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
