package net.defade.towerbow.utils;

import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.color.Color;

public enum Team {
    NOT(NamedTextColor.WHITE, new Color(0, 0, 0)),
    BLUE(NamedTextColor.BLUE, new Color(0, 0, 100)),
    RED(NamedTextColor.RED, new Color(100, 0, 0));

    private final NamedTextColor namedTextColor;
    private final Color color;

    Team(NamedTextColor namedTextColor, Color color) {
        this.namedTextColor = namedTextColor;
        this.color = color;
    }

    public NamedTextColor getNamedTextColor() {
        return namedTextColor;
    }

    public Color getColor() {
        return color;
    }
}
