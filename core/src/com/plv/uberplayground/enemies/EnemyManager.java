package com.plv.uberplayground.enemies;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.plv.uberplayground.actors.PlayerActor;

/**
 * Central management system for all active enemies, coordinating their lifecycle and behaviors.
 * 
 * The EnemyManager provides:
 * - Enemy registration and deregistration
 * - Efficient enemy update coordination
 * - Enemy count tracking and limits enforcement
 * - Integration with HealthSystem for enemy removal
 * - Performance optimization through batched updates
 * 
 * Requirements: 8.1, 8.2, 8.4
 */
public class EnemyManager {
    
    // Active enemies list
    private final Array<PlayerActor> activeEnemies;
    
    // Health system integration
    private final HealthSystem healthSystem;
    
    // GameScreen reference for deferred physics cleanup
    private com.plv.uberplayground.screens.GameScreen gameScreen;
    
    // Performance tracking
    private long lastUpdateTime;
    private int updateCount;
    
    /**
     * Creates a new EnemyManager instance.
     * @param healthSystem The health system to integrate with for enemy lifecycle management
     */
    public EnemyManager(HealthSystem healthSystem) {
        this.activeEnemies = new Array<>();
        this.healthSystem = healthSystem;
        this.gameScreen = null;
        this.lastUpdateTime = TimeUtils.millis();
        this.updateCount = 0;
    }
    
    /**
     * Sets the GameScreen reference for deferred physics cleanup.
     * @param gameScreen The GameScreen instance
     */
    public void setGameScreen(com.plv.uberplayground.screens.GameScreen gameScreen) {
        this.gameScreen = gameScreen;
    }
    
    /**
     * Registers a new enemy with the manager.
     * Initializes enemy-specific components and adds to active enemy list.
     * 
     * @param enemy The enemy to register
     * @return true if enemy was successfully registered, false if registration failed
     */
    public boolean addEnemy(PlayerActor enemy) {
        if (enemy == null) {
            return false;
        }
        
        // Check enemy count limit
        if (activeEnemies.size >= SpawnConfiguration.MAX_ENEMIES) {
            return false;
        }
        
        // Avoid duplicate registration
        if (activeEnemies.contains(enemy, true)) {
            return false;
        }
        
        // Configure as enemy if not already set
        if (!enemy.isEnemy()) {
            enemy.setIsEnemy(true);
        }
        
        // Register with health system
        healthSystem.initializeHealth(enemy);
        
        // Initialize behavior state if needed
        if (enemy.getBehaviorState() == null) {
            enemy.setIsEnemy(true); // This will initialize behavior state
        }
        
        // Add to active enemies list
        activeEnemies.add(enemy);
        
        return true;
    }
    
    /**
     * Removes an enemy from the manager.
     * Cleans up enemy-specific components and removes from active enemy list.
     * 
     * @param enemy The enemy to remove
     * @return true if enemy was successfully removed, false if enemy was not found
     */
    public boolean removeEnemy(PlayerActor enemy) {
        if (enemy == null) {
            return false;
        }
        
        // Remove from active enemies list
        boolean removed = activeEnemies.removeValue(enemy, true);
        
        if (removed) {
            // Remove from health system
            healthSystem.removeEnemy(enemy);
            
            // Remove from stage if it's on a stage
            if (enemy.getStage() != null) {
                enemy.remove();
            }
            
            // Clear enemy flag
            enemy.setIsEnemy(false);
        }
        
        return removed;
    }
    
    /**
     * Updates all active enemies and processes any that need to be removed.
     * This method should be called once per frame from the main game loop.
     * Enhanced with performance optimization for 60 FPS target.
     * 
     * @param delta Time elapsed since last update in seconds
     */
    public void updateAll(float delta) {
        // Start performance monitoring
        PerformanceOptimizer.startFrame();
        
        updateCount++;
        
        // Synchronize health percentages from health system to PlayerActor components
        for (PlayerActor enemy : activeEnemies) {
            if (enemy.isEnemy()) {
                float healthPercentage = healthSystem.getHealthPercentage(enemy);
                enemy.setHealthPercentage(healthPercentage);
            }
        }
        
        // Process enemy removals from health system
        Array<PlayerActor> enemiesToRemove = healthSystem.processRemovals();
        for (PlayerActor enemy : enemiesToRemove) {
            // Clear health bar cache when removing enemy
            PerformanceOptimizer.clearHealthBarCache(enemy);
            
            // Debug logging for enemy removal
            com.badlogic.gdx.Gdx.app.log("EnemyManager", "Removing dead enemy from game");
            
            // Remove from stage if it's on a stage
            if (enemy.getStage() != null) {
                enemy.remove();
            }
            
            // Queue for deferred physics body cleanup
            if (gameScreen != null) {
                gameScreen.queueEnemyForPhysicsCleanup(enemy);
            } else {
                // Fallback: force dispose immediately (risky but better than memory leak)
                com.badlogic.gdx.Gdx.app.log("EnemyManager", "Warning: No GameScreen reference, force disposing physics body");
                enemy.forceDispose();
            }
            
            // Remove from enemy manager
            removeEnemy(enemy);
        }
        
        // Optimized behavior updates with spatial partitioning for large enemy counts
        if (activeEnemies.size > 5) {
            // Use spatial partitioning for performance when many enemies are active
            Array<PlayerActor>[][] spatialGrid = PerformanceOptimizer.spatialPartition(activeEnemies);
            
            // Update behaviors with spatial optimization
            for (PlayerActor enemy : activeEnemies) {
                if (enemy.getBehaviorState() != null) {
                    // Get nearby enemies for flocking behavior optimization
                    Array<PlayerActor> nearbyEnemies = PerformanceOptimizer.getNearbyEnemies(spatialGrid, enemy);
                    
                    // Update behavior state
                    enemy.getBehaviorState().update(delta);
                    
                    // Set nearby enemies for flocking behavior if needed
                    if (enemy.getBehaviorState().getCurrentType() == SteeringBehaviorType.FLOCK) {
                        enemy.setNearbyEnemies(nearbyEnemies);
                    }
                }
            }
        } else {
            // Simple update for small enemy counts
            for (PlayerActor enemy : activeEnemies) {
                if (enemy.getBehaviorState() != null) {
                    enemy.getBehaviorState().update(delta);
                }
            }
        }
        
        // Update performance tracking
        long currentTime = TimeUtils.millis();
        if (currentTime - lastUpdateTime >= 1000) { // Every second
            lastUpdateTime = currentTime;
            
            // Perform periodic memory cleanup
            PerformanceOptimizer.performMemoryCleanup();
            
            // Log performance if below target
            if (!PerformanceOptimizer.isPerformanceAcceptable()) {
                System.out.println("Performance warning: " + PerformanceOptimizer.getPerformanceStats());
            }
            
            // Reset update count for next second
            updateCount = 0;
        }
        
        // End performance monitoring
        PerformanceOptimizer.endFrame();
    }
    /**
     * Updates the player target position for all active enemies.
     * This should be called each frame to ensure enemies have current player position for behaviors.
     *
     * @param playerPosition The current player position
     */
    public void updatePlayerTargets(com.badlogic.gdx.math.Vector2 playerPosition) {
        if (playerPosition == null) {
            return;
        }

        for (PlayerActor enemy : activeEnemies) {
            enemy.setPlayerTarget(playerPosition);
        }
    }

    /**
     * Updates all active enemies and processes any that need to be removed.
     * This method should be called once per frame from the main game loop.
     *
     * @param delta Time elapsed since last update in seconds
     */
    
    /**
     * Gets a copy of the active enemies list.
     * @return Array containing all currently active enemies
     */
    public Array<PlayerActor> getActiveEnemies() {
        return new Array<>(activeEnemies);
    }
    
    /**
     * Gets the current number of active enemies.
     * @return Number of enemies currently managed by this system
     */
    public int getEnemyCount() {
        return activeEnemies.size;
    }
    
    /**
     * Checks if the enemy count limit has been reached.
     * @return true if at maximum enemy capacity, false otherwise
     */
    public boolean isAtMaxCapacity() {
        return activeEnemies.size >= SpawnConfiguration.MAX_ENEMIES;
    }
    
    /**
     * Gets the maximum number of enemies allowed.
     * @return Maximum enemy count from configuration
     */
    public int getMaxEnemyCount() {
        return SpawnConfiguration.MAX_ENEMIES;
    }
    
    /**
     * Removes all enemies from the manager.
     * This should be called when resetting the game state or transitioning to game over.
     * Enhanced with performance optimization cleanup.
     */
    public void clearAllEnemies() {
        // Remove all enemies from health system and stage
        for (PlayerActor enemy : activeEnemies) {
            healthSystem.removeEnemy(enemy);
            enemy.setIsEnemy(false);
            
            // Remove from stage if it's on a stage
            if (enemy.getStage() != null) {
                enemy.remove();
            }
        }
        
        // Clear the active enemies list
        activeEnemies.clear();
        
        // Clear performance optimization caches
        PerformanceOptimizer.clearAllHealthBarCache();
        
        // Reset performance tracking
        updateCount = 0;
        lastUpdateTime = TimeUtils.millis();
    }
    
    /**
     * Checks if a specific enemy is currently managed by this system.
     * @param enemy The enemy to check
     * @return true if the enemy is active, false otherwise
     */
    public boolean isEnemyActive(PlayerActor enemy) {
        return enemy != null && activeEnemies.contains(enemy, true);
    }
    
    /**
     * Gets enemies within a specified radius of a target position.
     * Useful for flocking behavior and proximity-based AI decisions.
     * 
     * @param centerX X coordinate of the center position
     * @param centerY Y coordinate of the center position
     * @param radius Search radius in pixels
     * @return Array of enemies within the specified radius
     */
    public Array<PlayerActor> getEnemiesInRadius(float centerX, float centerY, float radius) {
        Array<PlayerActor> nearbyEnemies = new Array<>();
        float radiusSquared = radius * radius;
        
        for (PlayerActor enemy : activeEnemies) {
            float dx = enemy.getX() - centerX;
            float dy = enemy.getY() - centerY;
            float distanceSquared = dx * dx + dy * dy;
            
            if (distanceSquared <= radiusSquared) {
                nearbyEnemies.add(enemy);
            }
        }
        
        return nearbyEnemies;
    }
    
    /**
     * Gets performance statistics for monitoring system efficiency.
     * @return String containing current performance metrics
     */
    public String getPerformanceStats() {
        return String.format("Enemies: %d/%d, Updates/sec: %d", 
                           activeEnemies.size, 
                           SpawnConfiguration.MAX_ENEMIES, 
                           updateCount);
    }
    
    /**
     * Validates the integrity of the enemy management system.
     * Checks for consistency between active enemies and health system registration.
     * 
     * @return true if system is in a valid state, false if inconsistencies are found
     */
    public boolean validateSystemIntegrity() {
        // Check that all active enemies are registered in health system
        for (PlayerActor enemy : activeEnemies) {
            if (!healthSystem.isAlive(enemy) && healthSystem.getCurrentHealth(enemy) == 0) {
                // Enemy is in active list but not properly registered in health system
                return false;
            }
        }
        
        // Check that health system count matches active enemy count
        int healthSystemCount = healthSystem.getRegisteredEnemyCount();
        if (healthSystemCount != activeEnemies.size) {
            return false;
        }
        
        return true;
    }
}