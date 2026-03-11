package com.plv.uberplayground.enemies;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.plv.uberplayground.actors.PlayerActor;

/**
 * Enhanced enemy object pool for efficient enemy management.
 * Supports up to 50 concurrent enemies with optimized object reuse
 * to minimize garbage collection and improve performance.
 * 
 * Requirements: 4.2 - Enemy pooling to minimize garbage collection
 */
public class EnemyPool {
    private static final int POOL_SIZE = 50;
    
    private final Array<PlayerActor> pool;
    private final Array<PlayerActor> activeEnemies;
    private final World world;
    private final Stage gameStage;
    
    // Pool monitoring metrics
    private int totalObtained = 0;
    private int totalReused = 0;
    private int peakActiveCount = 0;
    
    public EnemyPool(World world, Stage gameStage) {
        this.world = world;
        this.gameStage = gameStage;
        this.pool = new Array<PlayerActor>(POOL_SIZE);
        this.activeEnemies = new Array<PlayerActor>(POOL_SIZE);
        
        // Pre-create pool of enemy instances
        initializePool();
    }
    
    /**
     * Initializes the enemy pool with pre-created PlayerActor instances.
     * All enemies are created in an inactive state ready for reuse.
     */
    private void initializePool() {
        for (int i = 0; i < POOL_SIZE; i++) {
            // Create enemy at off-screen position to avoid debug renderer artifacts at (0,0)
            PlayerActor enemy = new PlayerActor("agent", world, -1000, -1000, 5, 2, 50.0f);
            enemy.setIdleAnimFrameDuration(0.05f);
            enemy.setIsEnemy(true);
            
            // Mark as inactive initially
            enemy.setVisible(false);
            
            // Disable collision detection for pooled enemies to prevent immediate damage
            if (enemy.getBody() != null && enemy.getBody().getFixtureList().size > 0) {
                com.badlogic.gdx.physics.box2d.Filter filter = new com.badlogic.gdx.physics.box2d.Filter();
                filter.categoryBits = 0; // No collision category - won't collide with anything
                filter.maskBits = 0; // No collision mask - won't collide with anything
                enemy.getBody().getFixtureList().get(0).setFilterData(filter);
            }
            
            pool.add(enemy);
        }
    }
    
    /**
     * Obtains an enemy from the pool and configures it for spawning.
     * Reuses inactive enemies when available, minimizing object creation.
     * 
     * @param position The spawn position for the enemy
     * @return Configured enemy ready for use, or null if pool is exhausted
     */
    public PlayerActor obtain(Vector2 position) {
        PlayerActor enemy = null;
        
        // First, try to find an inactive enemy in the pool
        for (PlayerActor pooledEnemy : pool) {
            if (!isEnemyActive(pooledEnemy)) {
                enemy = pooledEnemy;
                totalReused++;
                break;
            }
        }
        
        // If no inactive enemy found, check if we can reuse an active one
        if (enemy == null && activeEnemies.size >= POOL_SIZE) {
            // Pool exhausted - reuse the oldest active enemy
            enemy = findOldestActiveEnemy();
            if (enemy != null) {
                // Remove from current position and reset
                resetEnemy(enemy);
                totalReused++;
            }
        }
        
        if (enemy != null) {
            // Configure the enemy for spawning
            configureEnemyForSpawn(enemy, position);
            
            // Track as active
            if (!activeEnemies.contains(enemy, true)) {
                activeEnemies.add(enemy);
            }
            
            // Update metrics
            totalObtained++;
            peakActiveCount = Math.max(peakActiveCount, activeEnemies.size);
            
            return enemy;
        }
        
        return null; // Pool exhausted and no reusable enemies
    }
    
    /**
     * Returns an enemy to the pool for reuse.
     * Properly cleans up the enemy state and removes from active tracking.
     * 
     * @param enemy The enemy to return to the pool
     */
    public void free(PlayerActor enemy) {
        if (enemy == null) {
            return;
        }
        
        // Remove from active tracking
        activeEnemies.removeValue(enemy, true);
        
        // Reset enemy state for reuse
        resetEnemy(enemy);
    }
    
    /**
     * Configures an enemy for spawning at the specified position.
     * Sets up all necessary properties for active gameplay.
     */
    private void configureEnemyForSpawn(PlayerActor enemy, Vector2 position) {
        // CRITICAL FIX: Always recreate physics body since we destroy it during pooling
        // This ensures no physics bodies exist at (0,0) for the debug renderer to show
        if (enemy.getBody() == null) {
            com.badlogic.gdx.Gdx.app.log("EnemyPool", "Recreating physics body for pooled enemy");
            enemy.recreatePhysicsBody(world, position.x, position.y);
        } else {
            // This shouldn't happen with the new approach, but handle it just in case
            com.badlogic.gdx.Gdx.app.log("EnemyPool", "Warning: Enemy still has physics body when configuring for spawn");
            
            // Set position
            enemy.setPosition(position.x, position.y);
            
            // Reset physics body position
            enemy.getBody().setTransform(position.x, position.y, 0);
            enemy.getBody().setLinearVelocity(0, 0);
            enemy.getBody().setAngularVelocity(0);
        }
        
        // Re-enable collision detection for active enemies (this is handled in recreatePhysicsBody for enemies)
        if (enemy.getBody() != null && enemy.getBody().getFixtureList().size > 0) {
            // Set user data for collision detection
            enemy.getBody().setUserData(enemy);
            
            // CRITICAL FIX: Restore proper collision categories for active enemies
            // This was missing and causing collision detection to fail
            if (enemy.isEnemy()) {
                com.badlogic.gdx.physics.box2d.Filter filter = new com.badlogic.gdx.physics.box2d.Filter();
                filter.categoryBits = com.plv.uberplayground.config.Configuration.EntityCategory.ENEMY_SHIP.getValue();
                filter.maskBits = (short) (com.plv.uberplayground.config.Configuration.EntityCategory.BOUNDARY.getValue() | 
                                           com.plv.uberplayground.config.Configuration.EntityCategory.PLAYER_SHIP.getValue() | 
                                           com.plv.uberplayground.config.Configuration.EntityCategory.PROJECTILE.getValue());
                enemy.getBody().getFixtureList().get(0).setFilterData(filter);
            }
        }
        
        // Make visible and active
        enemy.setVisible(true);
        
        // Add to stage if not already added
        if (enemy.getStage() == null) {
            gameStage.addActor(enemy);
        }
        
        // Reset behavior state if needed
        if (enemy.getBehaviorState() != null) {
            // Force a new random behavior to reset state
            enemy.getBehaviorState().forceBehaviorSwitch(SteeringBehaviorType.getRandomBehavior());
        }
    }
    
    /**
     * Resets an enemy to inactive state for pool reuse.
     * Cleans up all active state while preserving the object for reuse.
     */
    private void resetEnemy(PlayerActor enemy) {
        // Make invisible
        enemy.setVisible(false);
        
        // CRITICAL FIX: Completely destroy physics body to prevent debug renderer artifacts
        // The Box2D debug renderer shows ALL bodies in the world, even inactive ones
        // The only way to prevent rendering is to remove the body entirely
        if (enemy.getBody() != null) {
            // Clear velocities before destroying
            enemy.getBody().setLinearVelocity(0, 0);
            enemy.getBody().setAngularVelocity(0);
            
            // Destroy the physics body completely
            world.destroyBody(enemy.getBody());
            enemy.clearPhysicsBodyReference(); // Set to null so recreatePhysicsBody will work
        }
        
        // Clear behavior state
        if (enemy.getBehaviorState() != null) {
            // Force a new random behavior to reset state
            enemy.getBehaviorState().forceBehaviorSwitch(SteeringBehaviorType.getRandomBehavior());
        }
        
        // Clear player target
        enemy.setPlayerTarget(null);
        
        // Remove from stage but don't dispose
        if (enemy.getStage() != null) {
            enemy.remove();
        }
    }
    
    /**
     * Checks if an enemy is currently active in gameplay.
     */
    private boolean isEnemyActive(PlayerActor enemy) {
        return enemy.isVisible() && enemy.getStage() != null && enemy.getBody() != null;
    }
    
    /**
     * Finds the oldest active enemy for reuse when pool is exhausted.
     * Uses a simple heuristic based on position in the active array.
     */
    private PlayerActor findOldestActiveEnemy() {
        if (activeEnemies.size > 0) {
            // Return the first enemy in the active list (oldest)
            return activeEnemies.first();
        }
        return null;
    }
    
    /**
     * Updates all active enemies in the pool.
     * Should be called each frame to maintain enemy lifecycle.
     */
    public void updateAll(float delta) {
        // Update active enemies and check for cleanup
        for (int i = activeEnemies.size - 1; i >= 0; i--) {
            PlayerActor enemy = activeEnemies.get(i);
            
            // Check if enemy should be removed (e.g., destroyed, out of bounds)
            if (!isEnemyActive(enemy) || shouldRemoveEnemy(enemy)) {
                free(enemy);
            }
        }
    }
    
    /**
     * Determines if an enemy should be removed from active gameplay.
     * Override this method to add custom removal logic.
     */
    protected boolean shouldRemoveEnemy(PlayerActor enemy) {
        // Basic check - enemy is no longer visible or on stage
        return !enemy.isVisible() || enemy.getStage() == null;
    }
    
    /**
     * Clears all active enemies without disposing the pool.
     * Useful for game over or level transitions.
     */
    public void clearAllActive() {
        for (PlayerActor enemy : activeEnemies) {
            resetEnemy(enemy);
        }
        activeEnemies.clear();
    }
    
    /**
     * Disposes the entire pool and all contained enemies.
     * Should be called when shutting down the game.
     */
    public void dispose() {
        clearAllActive();
        
        // Dispose all pooled enemies
        for (PlayerActor enemy : pool) {
            if (enemy.getStage() != null) {
                enemy.remove();
            }
            // Note: Individual enemies don't need explicit disposal
            // as they share the same World which will be disposed elsewhere
        }
        
        pool.clear();
    }
    
    // Pool monitoring and management methods
    
    /**
     * Gets the current number of active enemies.
     */
    public int getActiveCount() {
        return activeEnemies.size;
    }
    
    /**
     * Gets the total pool capacity.
     */
    public int getPoolSize() {
        return POOL_SIZE;
    }
    
    /**
     * Gets the number of available (inactive) enemies in the pool.
     */
    public int getAvailableCount() {
        int available = 0;
        for (PlayerActor enemy : pool) {
            if (!isEnemyActive(enemy)) {
                available++;
            }
        }
        return available;
    }
    
    /**
     * Gets pool utilization as a percentage (0.0 to 1.0).
     */
    public float getUtilization() {
        return (float) activeEnemies.size / POOL_SIZE;
    }
    
    /**
     * Checks if the pool has capacity for additional enemies.
     */
    public boolean hasCapacity() {
        return activeEnemies.size < POOL_SIZE;
    }
    
    /**
     * Gets performance statistics for monitoring.
     */
    public String getPerformanceStats() {
        float reuseRate = totalObtained > 0 ? (float) totalReused / totalObtained : 0.0f;
        
        return String.format(
            "EnemyPool Stats - Active: %d/%d (%.1f%%), Reuse Rate: %.1f%%, Peak: %d, Total Obtained: %d",
            activeEnemies.size, POOL_SIZE, getUtilization() * 100,
            reuseRate * 100, peakActiveCount, totalObtained
        );
    }
    
    /**
     * Resets performance statistics.
     */
    public void resetStats() {
        totalObtained = 0;
        totalReused = 0;
        peakActiveCount = 0;
    }
}