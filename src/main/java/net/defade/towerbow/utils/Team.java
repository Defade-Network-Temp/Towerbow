package net.defade.towerbow.utils;

import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.color.Color;

public enum Team {
    NOT(NamedTextColor.WHITE, new Color(0, 0, 0), 0),
    BLUE(NamedTextColor.BLUE, new Color(0, 0, 100), 1),
    RED(NamedTextColor.RED, new Color(100, 0, 0), -1);

    private final NamedTextColor namedTextColor;
    private final Color color;
    private final int i;

    Team(NamedTextColor namedTextColor, Color color, int i) {
        this.namedTextColor = namedTextColor;
        this.color = color;
        this.i = i;
    }

    public NamedTextColor getNamedTextColor() {
        return namedTextColor;
    }

    public Color getColor() {
        return color;
    }

    public int getI() {
        return i;
    }
}
