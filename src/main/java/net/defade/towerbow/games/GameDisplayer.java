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
    private static final TextComponent TEAM_LOGO = Component.text("❤");
    private static final TextComponent ENEMY_LOGO = Component.text("❤");

    private final GameInstance instance;
    private final List<TPlayer> players;
    private final BossBar bossBar;
    private final Task clock;

    private int _blueAlive = 0, _blueDead = 0, _redAlive = 0, _redDead = 0;

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
                bossBar.name(Component.text("Starting in " + (Utils.count - (System.currentTimeMillis() - Utils.currentMillis) / 1000)).color(NamedTextColor.YELLOW));
                bossBar.overlay(BossBar.Overlay.PROGRESS);
                bossBar.color(BossBar.Color.PINK);
                float progress = ((float) (System.currentTimeMillis() - Utils.currentMillis) / 1000) / Utils.count;
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
        if (status().isPlaying() && status() != GameStatus.STARTING) {
            // super boilerplate code but working
            int blueAlive = 0;
            int blueDead = 0;
            int redAlive = 0;
            int redDead = 0;
            for (TPlayer player : players) {
                boolean isDead = player.isDead();
                Team team = player.getGameTeam();
                if (team.isBlue() && !isDead) ++blueAlive;
                else if (team.isBlue() && isDead) ++blueDead;
                else if (team.isRed() && !isDead) ++redAlive;
                else if (team.isRed() && isDead) ++redDead;
            }
            if (blueAlive != _blueAlive || blueDead != _blueDead || redAlive != _redAlive || redDead != _redDead) {
                _blueAlive = blueAlive;
                _blueDead = blueDead;
                _redAlive = redAlive;
                _redDead = redDead;

                //send the correct numbers to each player
                for (TPlayer player : players) {
                    if (player.getGameTeam().isBlue()) updateActionBar(player, blueAlive, blueDead, redAlive, redDead);
                    else if (player.getGameTeam().isRed())
                        updateActionBar(player, redAlive, redDead, blueAlive, blueDead);
                }
            }
        } else if (status() == GameStatus.STARTING) {
            players.forEach(player -> player.setActionMessage(Component.text("↑ Get up ↑")));
        }


        players.forEach(TPlayer::sendActionMessage);
    }

    public void destroy() {
        clock.cancel();
    }

    private GameStatus status() {
        return instance.getGameStatus();
    }

    //macros
    public void updateActionBar(TPlayer player, int teamAlive, int teamDead, int enemyAlive, int enemyDead) {
        Team team = player.getGameTeam();
        TextComponent component = Component.text("");

        String kills;
        if (player.getKills() == 1) kills = " kill";
        else kills = " kills";

        for (int i = 0; i < teamAlive; ++i) component = component.append(TEAM_LOGO.color(team.getTextColor()));
        for (int i = 0; i < teamDead; ++i) component = component.append(TEAM_LOGO.color(TextColor.color(75, 75, 75)));
        component = component.append(Component.text("  " + player.getKills() + kills + " "));
        for (int i = 0; i < enemyAlive; ++i)
            component = component.append(ENEMY_LOGO.color(team.getOpposite().getTextColor()));
        for (int i = 0; i < enemyDead; ++i) component = component.append(ENEMY_LOGO.color(TextColor.color(75, 75, 75)));

        player.setActionMessage(component);
    }
}
