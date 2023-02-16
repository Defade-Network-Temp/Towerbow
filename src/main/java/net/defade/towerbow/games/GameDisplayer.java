package net.defade.towerbow.games;

import net.defade.towerbow.players.TPlayer;
import net.defade.towerbow.utils.Team;
import net.defade.towerbow.utils.Utils;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;

import java.util.List;

public class GameDisplayer {
    private final GameInstance instance;
    private final List<TPlayer> players;
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
        updateActionBar();
    }

    private void updateBossBar() {
        switch (status()) {
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

        for (Player player : players) {
            player.showBossBar(bossBar);
        }
    }

    private void updateActionBar() {
        if (!status().isPlaying() || status() == GameStatus.STARTING) return;
        TextComponent blueHeart = Component.text("");
        TextComponent redHeart = Component.text("");
        for (TPlayer player : players) {
            Team team = player.getGameTeam();
            boolean isDead = player.isDead();
            if (team == Team.BLUE) {
                if (isDead) blueHeart = blueHeart.append(Component.text("✪").color(TextColor.color(75, 75, 75)));
                else blueHeart = blueHeart.append(Component.text("✪").color(TextColor.color(255, 75, 0)));
            } else if (team == Team.RED) {
                if (isDead) redHeart = redHeart.append(Component.text("✪").color(TextColor.color(75, 75, 75)));
                else redHeart = redHeart.append(Component.text("✪").color(TextColor.color(0, 190, 255)));
            }
        }

        for (TPlayer player : players) {
            int kills = player.getKills();
            TextComponent kill;
            if (kills == 1) kill = Component.text(" kill  ");
            else kill = Component.text(" kills  ");
            player.sendActionBar( blueHeart.append(Component.text(" " + kills)).append(kill).append(redHeart));
        }
    }

    public void destroy() {
        clock.cancel();
    }

    private GameStatus status() {
        return instance.getGameStatus();
    }
}
