package net.defade.towerbow;

import net.defade.bismuth.core.servers.GameType;
import net.minestom.server.extensions.Extension;
import org.jetbrains.annotations.Nullable;

public class Main extends Extension {

    @Override
    public void initialize() {

    }

    @Override
    public void terminate() {

    }

    @Override
    public @Nullable GameType serverGameType() {
        return new GameType("towerbow", "classic");
    }
}
