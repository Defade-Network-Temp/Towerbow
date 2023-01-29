package net.defade.towerbow.map;

import net.defade.towerbow.games.GameInstance;
import net.minestom.server.instance.block.Block;

public class TowerbowMapGenerator  {
    public static final int Y = 2;
    public static void generate(GameInstance gameInstance, int sizeOfHalfASide) {

        for (int x = -sizeOfHalfASide;x < sizeOfHalfASide; ++x) {
            for (int z = -sizeOfHalfASide;z < sizeOfHalfASide; ++z) {
                gameInstance.setBlock(x, Y, z, Block.GRAY_STAINED_GLASS);
            }
        }

        gameInstance.getWorldBorder().setDiameter(sizeOfHalfASide*2);
    }
}
