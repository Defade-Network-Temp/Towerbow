package net.defade.towerbow.players;

import net.defade.towerbow.utils.Items;
import net.defade.towerbow.utils.Messager;
import net.defade.towerbow.utils.Team;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class TPlayer extends Player {
    public static final Tag<Component> ACTION_BAR_TAG = Tag.Component("actionBarTag");
    private static final Tag<Boolean> IS_DEAD_TAG = Tag.Boolean("isDead");
    private static final Tag<Integer> TEAM_TAG = Tag.Integer("team");
    private static final Tag<Integer> KILLS_TAG = Tag.Integer("kills");
    private static final Tag<Double> FALLING_TAG = Tag.Double("playerBlockFalling");
    private static final Tag<Long> DANGER_TIME_TAG = Tag.Long("low_time_tag");


    public TPlayer(@NotNull UUID uuid, @NotNull String username, @NotNull PlayerConnection playerConnection) {
        super(uuid, username, playerConnection);
    }

    //public methods
    public void bowDamage(TPlayer shooter, int damage) {
        if (getUuid().equals(shooter.getUuid())) {
            damage(null, DamageType.fromPlayer(shooter), 0);
            shooter.playSound(Sound.sound(SoundEvent.ENTITY_ARROW_HIT, Sound.Source.NEUTRAL, 1.0f, 1.0f));
        } else if (sameTeamDiffPlayer(this, shooter)) {
            Messager.sendWarningFromGame(shooter, Component.text("Don't shoot at your teammate !"));
            shooter.playSound(Sound.sound(SoundEvent.BLOCK_NOTE_BLOCK_BASEDRUM, Sound.Source.NEUTRAL, 1.0f, 1.0f));
        } else {
            damage(shooter, DamageType.fromPlayer(shooter), damage);
            shooter.playSound(Sound.sound(SoundEvent.ENTITY_ARROW_HIT_PLAYER, Sound.Source.NEUTRAL, 1.0f, 1.0f));
        }
    }

    public void fallDamage(int damage) {
        damage(null, DamageType.GRAVITY, damage);
    }

    public void setEquipment() {
        Team team = getGameTeam();
        getInventory().setItemStack(0, Items.BOW.get(team));
        getInventory().setItemStack(1, Items.PICKAXE.get(team));
        getInventory().setHelmet(Items.HELMET.get(team));
        getInventory().setChestplate(Items.CHEST.get(team));
        getInventory().setLeggings(Items.LEGGINGS.get(team));
        getInventory().setBoots(Items.BOOTS.get(team));
        getInventory().setItemInOffHand(Items.STONE.get(team));
    }

    // private methods
    public void damage(@Nullable TPlayer theBadGuy, DamageType type, int damage) {
        if (getHealth() - damage > 0) {
            damage(type, damage);
        } else {
            setDead();
            if (theBadGuy != null) {
                theBadGuy.addKill(true);
            }
        }
    }

    // Getters / Setters
    public boolean isDead() {
        if (!hasTag(IS_DEAD_TAG)) setTag(IS_DEAD_TAG, false);
        return getTag(IS_DEAD_TAG);
    }

    public void setDead() {
        setTag(IS_DEAD_TAG, true);
        setInvisible(true);
        setGameMode(GameMode.SPECTATOR);
        setHealth(getMaxHealth());
    }

    public Team getGameTeam() {
        if (!hasTag(TEAM_TAG)) setTag(TEAM_TAG, Team.NOT.ordinal());
        return Team.values()[getTag(TEAM_TAG)];
    }

    public void setGameTeam(Team team) {
        setTag(TEAM_TAG, team.ordinal());
    }

    public int getKills() {
        if (!hasTag(KILLS_TAG)) setTag(KILLS_TAG, 0);
        return getTag(KILLS_TAG);
    }

    public void addKill(boolean award) {
        int killsBefore = getTag(KILLS_TAG);
        setTag(KILLS_TAG, killsBefore + 1);
        if (award) awardForKill();
    }

    public boolean wasFalling() {
        return hasTag(FALLING_TAG);
    }

    public void startFalling(double y) {
        setTag(FALLING_TAG, y);
    }

    public void stopFalling() {
        removeTag(FALLING_TAG);
    }

    public double getFalledHeight() {
        return getTag(FALLING_TAG);
    }

    public Component getActionMessage() {
        return getTag(ACTION_BAR_TAG);
    }

    public void setActionMessage(TextComponent message) {
        setTag(ACTION_BAR_TAG, message);
    }

    public void sendActionMessage() {
        if (hasActionMessage()) sendActionBar(getActionMessage());
    }

    public boolean hasActionMessage() {
        return hasTag(ACTION_BAR_TAG);
    }

    public long getTicksInDangerZone() {
        return getTag(DANGER_TIME_TAG);
    }

    public void startInDangerZone(long ticks) {
        setTag(DANGER_TIME_TAG, ticks);
    }

    public boolean wasInDangerZone() {
        return hasTag(DANGER_TIME_TAG);
    }

    public void setNotInDanger() {
        removeTag(DANGER_TIME_TAG);
    }

    // macros

    private void awardForKill() {
        getInventory().addItemStack(Items.GAPPLE.get());
    }

    private boolean sameTeamDiffPlayer(TPlayer p1, TPlayer p2) {
        return p1.getGameTeam() == p2.getGameTeam() && !p2.getUuid().equals(p1.getUuid());
    }
}
