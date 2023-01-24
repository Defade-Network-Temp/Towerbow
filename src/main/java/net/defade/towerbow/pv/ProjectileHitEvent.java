package net.defade.towerbow.pv;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import org.jetbrains.annotations.NotNull;

public abstract class ProjectileHitEvent implements EntityInstanceEvent {

    private final TTArrow projectile;

    public ProjectileHitEvent(@NotNull TTArrow projectile) {
        this.projectile = projectile;
    }

    @Override
    public @NotNull TTArrow getEntity() {
        return projectile;
    }

    public static class ProjectileBlockHitEvent extends ProjectileHitEvent {

        public ProjectileBlockHitEvent(@NotNull TTArrow projectile) {
            super(projectile);
        }
    }
    public static class ProjectileEntityHitEvent extends ProjectileHitEvent implements CancellableEvent {

        private final Entity hitEntity;
        private boolean cancelled;

        public ProjectileEntityHitEvent(@NotNull TTArrow projectile, @NotNull Entity hitEntity) {
            super(projectile);
            this.hitEntity = hitEntity;
        }

        public Entity getHitEntity() {
            return hitEntity;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public void setCancelled(boolean cancel) {
            this.cancelled = cancel;
        }
    }
}
