package net.defade.towerbow.utils;

import net.defade.towerbow.map.TowerbowMapGenerator;

public class Conf {
    public static int MIN_Y = TowerbowMapGenerator.Y + 10;
    public static int TICK_SAFE_DZ = 5*20; // 5 seconds in ticks
    public static int TICKS_FOR_BLOCK_AIR = 3 * 60 * 20;
    public static int TICKS_FOR_BLOCK_MOSSY = /*(2 * 60 + 55) * 1000*/ 30*20;

}
