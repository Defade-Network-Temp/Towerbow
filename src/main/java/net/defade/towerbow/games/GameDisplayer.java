package net.defade.towerbow.games;

import net.defade.towerbow.utils.Team;
import net.defade.towerbow.utils.Utils;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;

import java.util.Map;

public class GameDisplayer {
    private final GameInstance instance;
    private final Map<Player, Team> players;
    private final BossBar bossBar;
    private final Task clock;

    public GameDisplayer(GameInstance instance) {
        this.instance = instance;
        players = instance.getTPlayers();
        bossBar = BossBar.bossBar(Component.text("Loading... ").color(NamedTextColor.YELLOW), 0f, BossBar.Color.PURPLE, BossBar.Overlay.PROGRESS);
        clock = MinecraftServer.getSchedulerManager().submitTask(() -> {
            display();
            return TaskSchedule.seconds(1);
        });

    }

    private void display() {
        updateBossBar();
        for (Player player : players.keySet()) {
            player.showBossBar(bossBar);
        }
    }

    private void updateBossBar() {
        switch (instance.getGameStatus()) {
            case WAITING_PLAYERS_FOR_DEMO -> {
                bossBar.name(Component.text("Waiting players...").color(NamedTextColor.YELLOW));
                bossBar.overlay(BossBar.Overlay.PROGRESS);
            }
            case WAITING_PLAYER_OPTIONAL -> {
                bossBar.name(Component.text("Starting in " + (Utils.count - (System.currentTimeMillis() - Utils.currentMillis)/1000)).color(NamedTextColor.YELLOW));
                bossBar.overlay(BossBar.Overlay.PROGRESS);
                bossBar.color(BossBar.Color.PINK);
                float progress = ((float) (System.currentTimeMillis()-Utils.currentMillis)/1000) / Utils.count;
                if (progress >= 0.0 && progress <= 1.0) {
                    bossBar.progress(progress);
                }
            }
            default -> bossBar.name(Component.text("Loading... ").color(NamedTextColor.YELLOW));
        }
    }

    public void destroy() {
        clock.cancel();
    }
}
