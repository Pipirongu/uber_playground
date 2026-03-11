package com.plv.uberplayground.actors;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;

public class ProjectilePool {
    private static final int POOL_SIZE = 20;
    
    private Array<Projectile> pool;
    private World world;
    private Stage gameStage;
    
    public ProjectilePool(World world, Stage gameStage) {
        this.world = world;
        this.gameStage = gameStage;
        this.pool = new Array<Projectile>(POOL_SIZE);
        
        // Pre-create pool of Projectile instances with updated speed
        for (int i = 0; i < POOL_SIZE; i++) {
            Projectile projectile = new Projectile("spawn_unit", world, 1, 1);
            pool.add(projectile);
        }
    }
    
    public Projectile obtain(Vector2 position, Vector2 direction, Vector2 shipVelocity) {
        // Find an inactive projectile in the pool
        for (Projectile projectile : pool) {
            if (!projectile.isActive()) {
                // Activate and configure the projectile
                projectile.fire(position, direction, shipVelocity);

                // Add to stage if not already added
                if (projectile.getStage() == null) {
                    gameStage.addActor(projectile);
                }

                return projectile;
            }
        }

        // Pool exhausted - reuse the oldest active projectile
        Projectile oldestProjectile = null;
        float maxTimeAlive = -1.0f;

        for (Projectile projectile : pool) {
            if (projectile.isActive() && projectile.getTimeAlive() > maxTimeAlive) {
                maxTimeAlive = projectile.getTimeAlive();
                oldestProjectile = projectile;
            }
        }

        if (oldestProjectile != null) {
            // Reuse the oldest projectile
            oldestProjectile.fire(position, direction, shipVelocity);
            return oldestProjectile;
        }

        // Fallback: return null if somehow no projectiles are available
        return null;
    }
    
    public void free(Projectile projectile) {
        if (projectile != null) {
            // Deactivate the projectile
            projectile.deactivate();
            
            // Remove from stage
            projectile.remove();
        }
    }
    
    public void updateAll(float delta) {
        // Call act() on all active projectiles
        for (Projectile projectile : pool) {
            if (projectile.isActive()) {
                projectile.act(delta);
                
                // If projectile became inactive during act(), remove from stage
                if (!projectile.isActive() && projectile.getStage() != null) {
                    projectile.remove();
                }
            }
        }
    }
    
    public void disposeAll() {
        // Dispose all projectiles in the pool
        for (Projectile projectile : pool) {
            if (projectile.getStage() != null) {
                projectile.remove();
            }
            // Note: Individual projectiles don't need explicit disposal
            // as they share the same World which will be disposed elsewhere
        }
        pool.clear();
    }

    /**
     * Clears all active projectiles from the game without disposing the pool.
     * This deactivates all active projectiles and removes them from the stage.
     * Used during game over state to clear all projectiles from the game.
     */
    public void clearAllActive() {
        for (Projectile projectile : pool) {
            if (projectile.isActive()) {
                // Deactivate the projectile
                projectile.deactivate();

                // Remove from stage if present
                if (projectile.getStage() != null) {
                    projectile.remove();
                }
            }
        }
    }
    
    /**
     * Recreate the projectile pool with fresh instances to pick up any speed changes
     */
    public void recreatePool() {
        // Clear existing pool
        disposeAll();
        
        // Recreate pool with fresh projectile instances
        for (int i = 0; i < POOL_SIZE; i++) {
            Projectile projectile = new Projectile("spawn_unit", world, 1, 1);
            pool.add(projectile);
        }
    }
}