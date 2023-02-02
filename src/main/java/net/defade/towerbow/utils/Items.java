package net.defade.towerbow.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.item.*;
import net.minestom.server.item.metadata.LeatherArmorMeta;

import javax.naming.Name;
import java.util.function.Function;

public enum Items {
    BOW((team)-> {
        return ItemStack.builder(Material.BOW)
                .displayName(Component.text("Bow").color(TextColor.color(NamedTextColor.GOLD)).decoration(TextDecoration.ITALIC, false))
                .meta(builder -> builder.enchantment(Enchantment.PUNCH,  (short) 1)
                        .hideFlag(ItemHideFlag.HIDE_ENCHANTS, ItemHideFlag.HIDE_ATTRIBUTES))
                .build();
    }),
    HELMET((team)-> {
       Material material;
        if(team == Team.RED) material = Material.RED_STAINED_GLASS;
        else if (team == Team.BLUE) material = Material.BLUE_STAINED_GLASS;
        else material = Material.DRAGON_HEAD; // This shouldn't happen
        return ItemStack.builder(material)
                .displayName(Component.text("Space Helmet").color(TextColor.color(NamedTextColor.GOLD)).decoration(TextDecoration.ITALIC, false))
                .build();
    }),
    CHEST((team)-> {
        return ItemStack.builder(Material.LEATHER_CHESTPLATE)
                .meta(new LeatherArmorMeta.Builder().color(team.getColor())
                        .hideFlag(ItemHideFlag.HIDE_ATTRIBUTES, ItemHideFlag.HIDE_DYE, ItemHideFlag.HIDE_ENCHANTS)
                        .build())
                .displayName(Component.text("Chestplate").color(TextColor.color(NamedTextColor.GOLD)).decoration(TextDecoration.ITALIC, false))
                .build();
    }),
    LEGGINGS((team)-> {
        return ItemStack.builder(Material.IRON_LEGGINGS)
                .displayName(Component.text("Leggings").color(TextColor.color(NamedTextColor.GOLD)).decoration(TextDecoration.ITALIC, false))
                .meta(builder -> builder.enchantment(Enchantment.PROTECTION, (short) 1)
                        .hideFlag(ItemHideFlag.HIDE_ENCHANTS, ItemHideFlag.HIDE_ATTRIBUTES))
                .build();
    }),
    BOOTS((team)-> {
        return ItemStack.builder(Material.IRON_BOOTS)
                .displayName(Component.text("Boots").color(TextColor.color(NamedTextColor.GOLD)).decoration(TextDecoration.ITALIC, false))
                .meta(builder -> builder.enchantment(Enchantment.PROTECTION, (short) 1)
                        .hideFlag(ItemHideFlag.HIDE_ENCHANTS, ItemHideFlag.HIDE_ATTRIBUTES))
                .build();
    }),
    STONE((team)-> {
        return ItemStack.builder(Material.COBBLESTONE)
                .displayName(Component.text("Blocks").color(TextColor.color(NamedTextColor.GOLD)).decoration(TextDecoration.ITALIC, false))
                .amount(64)
                .build();
    }),
    PICKAXE((team)-> {
        return ItemStack.builder(Material.GOLDEN_PICKAXE)
                .displayName(Component.text("Pickaxe").color(TextColor.color(NamedTextColor.GOLD)).decoration(TextDecoration.ITALIC, false))
                .meta(builder -> builder.enchantment(Enchantment.SHARPNESS,  (short) 1)
                        .hideFlag(ItemHideFlag.HIDE_ENCHANTS, ItemHideFlag.HIDE_ATTRIBUTES))
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
