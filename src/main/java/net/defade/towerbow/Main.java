package net.defade.towerbow;

import net.defade.bismuth.core.servers.GameType;
import net.defade.towerbow.games.GameManager;
import net.minestom.server.MinecraftServer;
import net.minestom.server.extensions.Extension;
import org.jetbrains.annotations.Nullable;

public class Main extends Extension {

    GameManager gameManager;

    @Override
    public void initialize() {
        gameManager = new GameManager();
    }

    @Override
    public void terminate() {
        gameManager.destroy("terminate()");
    }

    @Override
    public @Nullable GameType serverGameType() {
        return new GameType("towerbow", "classic");
    }
}
