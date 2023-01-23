package net.defade.towerbow.utils;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.LeatherArmorMeta;

import java.util.function.Function;

public enum Items {
    BOW((team)-> {
        return ItemStack.builder(Material.BOW)
                .displayName(Component.text(""))
                .build();
    }),
    HELMET((team)-> {
        return ItemStack.builder(Material.LEATHER_HELMET)
                .displayName(Component.text(""))
                .meta(new LeatherArmorMeta.Builder().color(team.getColor()).build())
                .build();
    }),
    CHEST((team)-> {
        return ItemStack.builder(Material.LEATHER_CHESTPLATE)
                .displayName(Component.text(""))
                .meta(new LeatherArmorMeta.Builder().color(team.getColor()).build())
                .build();
    }),
    LEGGINGS((team)-> {
        return ItemStack.builder(Material.LEATHER_LEGGINGS)
                .displayName(Component.text(""))
                .meta(new LeatherArmorMeta.Builder().color(team.getColor()).build())
                .build();
    }),
    BOOTS((team)-> {
        return ItemStack.builder(Material.LEATHER_BOOTS)
                .displayName(Component.text(""))
                .meta(new LeatherArmorMeta.Builder().color(team.getColor()).build())
                .build();
    }),
    STONE((team)-> {
        return ItemStack.builder(Material.COBBLESTONE)
                .displayName(Component.text(""))
                .amount(64)
                .build();
    }),
    PICKAXE((team)-> {
        return ItemStack.builder(Material.GOLDEN_PICKAXE)
                .displayName(Component.text(""))
                .build();
    }),
    ;

    private final Function<Team, ItemStack> itemStackFunction;

    Items(Function<Team, ItemStack> itemStackFunction) {
       this.itemStackFunction = itemStackFunction;
    }

    public ItemStack get(Team team) {
        return itemStackFunction.apply(team);
    }
}
