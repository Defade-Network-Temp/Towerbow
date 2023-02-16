package net.defade.towerbow.pvp;

import net.defade.towerbow.games.GameInstance;
import net.defade.towerbow.players.TPlayer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.ProjectileMeta;
import net.minestom.server.entity.metadata.arrow.AbstractArrowMeta;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityShootEvent;
import net.minestom.server.event.entity.projectile.ProjectileUncollideEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;
//Most of the code from this class is inspired by MinestomPVP.
//        MinestomPVP repo : https://github.com/TogAr2/MinestomPvP

public class TowerbowArrow extends Entity {
    private static final double ARROW_BASE_DAMAGE = 2.0;

    private final Set<Integer> piercingIgnore = new HashSet<>();
    protected int pickupDelay;
    protected int stuckTime;
    protected int ticks;
    private final int knockback = 3;
    private final GameInstance gameInstance;

    private final Entity shooter;
    private final boolean hitAnticipation;
    protected boolean noClip;

    public TowerbowArrow(GameInstance gameInstance, Entity shooter) {
        super(EntityType.ARROW);
        this.gameInstance = gameInstance;
        this.shooter = shooter;
        this.hitAnticipation = false;
        setup();
    }

    private void setup() {
        super.hasPhysics = false;
        if (getEntityMeta() instanceof ProjectileMeta) {
            ((ProjectileMeta) getEntityMeta()).setShooter(shooter);
        }
    }

    public @Nullable Entity getShooter() {
        return shooter;
    }

    @Override
    public void update(long time) {
        if (onGround) {
            stuckTime++;
        } else {
            stuckTime = 0;
        }

        if (pickupDelay > 0) {
            pickupDelay--;
        }

        //TODO water (also for other projectiles?)

        tickRemoval();
    }

    protected void tickRemoval() {
        ticks++;
        if (ticks >= 1200) {
            remove();
        }
    }

    // Called when the arrow is stuck in a block, in this case the arrow is deleted after a minute
    public void onStuck() {
        this.setVelocity(Vec.ZERO);
        if (stuckTime > 20*60) remove();
    }

    public void setCritical(boolean critical) {
        ((AbstractArrowMeta) getEntityMeta()).setCritical(critical);
    }

    public void setPiercingLevel(byte piercingLevel) {
        ((AbstractArrowMeta) getEntityMeta()).setPiercingLevel(piercingLevel);
    }


    public void onUnstuck() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        setVelocity(getPosition().direction().mul(
                random.nextFloat() * 0.2,
                random.nextFloat() * 0.2,
                random.nextFloat() * 0.2
        ).mul(MinecraftServer.TICK_PER_SECOND));
        ticks = 0;
    }

    public void onHit(Entity entity) {
        if (entity instanceof Player && shooter instanceof Player) {
            TPlayer victim = (TPlayer) entity;
            TPlayer badGuy = (TPlayer) shooter;
            if (piercingIgnore.contains(entity.getEntityId())) return;

            ThreadLocalRandom random = ThreadLocalRandom.current();
            double movementSpeed = getVelocity().length() / MinecraftServer.TICK_PER_SECOND;
            int damage = (int) Math.ceil(MathUtils.clamp(
                    movementSpeed * ARROW_BASE_DAMAGE, 0.0, 2.147483647E9D));

            if (getPiercingLevel() > 0) {
                if (piercingIgnore.size() >= getPiercingLevel() + 1) {
                    remove();
                    return;
                }

                piercingIgnore.add(entity.getEntityId());
            }

            if (isCritical()) {
                int randomDamage = random.nextInt(damage / 2 + 2);
                damage = (int) Math.min(randomDamage + damage, 2147483647L);
            }
            if (gameInstance.isPvpOn()) {
                victim.bowDamage( badGuy, damage);

                if (knockback > 0) {
                    Vec knockbackVec = getVelocity()
                            .mul(1, 0, 1)
                            .normalize().mul(knockback * 0.6);
                    knockbackVec = knockbackVec.add(0, 0.1, 0)
                            .mul(MinecraftServer.TICK_PER_SECOND / 2.0);


                    if (knockbackVec.lengthSquared() > 0) {
                        Vec newVel = victim.getVelocity().add(knockbackVec);
                        victim.setVelocity(newVel);
                    }

                }
            }
            remove();
        }
    }

    public boolean isCritical() {
        return ((AbstractArrowMeta) getEntityMeta()).isCritical();
    }

    public byte getPiercingLevel() {
        return ((AbstractArrowMeta) getEntityMeta()).getPiercingLevel();
    }

    public void shoot(Point to, double power, double spread) {
        EntityShootEvent shootEvent = new EntityShootEvent(this.shooter, this, to, power, spread);
        EventDispatcher.call(shootEvent);
        if (shootEvent.isCancelled()) {
            remove();
            return;
        }
        final var from = this.shooter.getPosition().add(0D, this.shooter.getEyeHeight(), 0D);
        shoot(from, to, shootEvent.getPower(), shootEvent.getSpread());
    }

    private void shoot(@NotNull Point from, @NotNull Point to, double power, double spread) {
        double dx = to.x() - from.x();
        double dy = to.y() - from.y();
        double dz = to.z() - from.z();
        double xzLength = Math.sqrt(dx * dx + dz * dz);
        dy += xzLength * 0.20000000298023224D;

        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        dx /= length;
        dy /= length;
        dz /= length;
        Random random = ThreadLocalRandom.current();
        spread *= 0.007499999832361937D;
        dx += random.nextGaussian() * spread;
        dy += random.nextGaussian() * spread;
        dz += random.nextGaussian() * spread;

        final double mul = 20 * power;
        this.velocity = new Vec(dx * mul, dy * mul, dz * mul);
        setView(
                (float) Math.toDegrees(Math.atan2(dx, dz)),
                (float) Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)))
        );
    }

    @Override
    public void tick(long time) {
        if (hitAnticipation && getAliveTicks() == 0) {
            final TowerbowArrow.State state = guessNextState(getPosition());
            handleState(state);
            if (state != State.Flying) return;
        }

        final Pos posBefore = getPosition();
        super.tick(time);
        final Pos posNow = getPosition();
        final TowerbowArrow.State state = hitAnticipation ? guessNextState(posNow) : getState(posBefore, posNow, true);
        handleState(state);
    }

    protected void handleState(State state) {
        if (state == State.Flying) {
            if (!noClip && hasVelocity()) {
                Vec direction = getVelocity().normalize();
                double dx = direction.x();
                double dy = direction.y();
                double dz = direction.z();
                setView(
                        (float) Math.toDegrees(Math.atan2(dx, dz)),
                        (float) Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)))
                );
            }

            if (!super.onGround) {
                return;
            }
            super.onGround = false;
            setNoGravity(false);
            EventDispatcher.call(new ProjectileUncollideEvent(this));
            onUnstuck();
        } else if (state == State.StuckInBlock) {
            if (super.onGround) {
                return;
            }
            super.onGround = true;
            this.velocity = Vec.ZERO;
            sendPacketToViewersAndSelf(getVelocityPacket());
            setNoGravity(true);
            onStuck();
        } else {
            onHit(((State.HitEntity) state).entity);
        }
    }

    protected State guessNextState(Pos posNow) {
        return getState(posNow, posNow.add(getVelocity().mul(0.06)), false);
    }

    /**
     * Checks whether a projectile is stuck in block / hit an entity.
     *
     * @param pos    position right before current tick.
     * @param posNow position after current tick.
     * @return current state of the projectile.
     */
    @SuppressWarnings("ConstantConditions")
    private State getState(Pos pos, Pos posNow, boolean shouldTeleport) {
        if (noClip) return State.Flying;

        if (pos.samePoint(posNow)) {
            if (instance.getBlock(posNow).isSolid()) {
                return State.StuckInBlock;
            } else {
                return State.Flying;
            }
        }

        Instance instance = getInstance();
        Chunk chunk = null;
        Collection<Entity> entities = null;

        /*
          What we're about to do is to discretely jump from the previous position to the new one.
          For each point we will be checking blocks and entities we're in.
         */
        double part = .25D; // half of the bounding box
        final var dir = posNow.sub(pos).asVec();
        int parts = (int) Math.ceil(dir.length() / part);
        final var direction = dir.normalize().mul(part).asPosition();
        for (int i = 0; i < parts; ++i) {
            // If we're at last part, we can't just add another direction-vector, because we can exceed end point.
            if (i == parts - 1) {
                pos = posNow;
            } else {
                pos = pos.add(direction);
            }
            if (!instance.isChunkLoaded(pos)) {
                remove();
                return State.Flying;
            }
            Point blockPos = new Pos(pos.blockX(), pos.blockY(), pos.blockZ());
            if (instance.getBlock(pos).registry().collisionShape().intersectBox(pos.sub(blockPos), getBoundingBox())) {
                if (shouldTeleport) teleport(pos);
                return State.StuckInBlock;
            }


            Chunk currentChunk = instance.getChunkAt(pos);
            if (currentChunk != chunk) {
                chunk = currentChunk;
                entities = instance.getChunkEntities(chunk)
                        .stream()
                        .filter(LivingEntity.class::isInstance)
                        .collect(Collectors.toSet());
            }

            final Pos finalPos = pos;
            Stream<Entity> victims = entities.stream().filter(entity -> getBoundingBox().intersectEntity(finalPos, entity));

            /*
              We won't check collisions with self for first ticks of projectile's life, because it spawns in the
              shooter and will immediately be triggered by him.
             */
            if (getAliveTicks() < 6) {
                victims = victims.filter(entity -> entity != getShooter());
            }
            Optional<Entity> victim = victims.findAny();
            if (victim.isPresent()) {
                return new State.HitEntity(victim.get());
            }
        }
        return State.Flying;
    }

    protected interface State {
        State Flying = new State() {
        };
        State StuckInBlock = new State() {
        };

        record HitEntity(Entity entity) implements State {}
    }
}
