package com.plv.uberplayground.enemies;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.plv.uberplayground.actors.PlayerActor;

/**
 * Central management system for all active enemies, coordinating their lifecycle and behaviors.
 * Enhanced for dynamic enemy spawning with improved tracking and performance optimization.
 * 
 * The EnemyManager provides:
 * - Enemy registration and deregistration with accurate count tracking
 * - Efficient enemy update coordination for up to 50 concurrent enemies
 * - Enemy count tracking and limits enforcement for dynamic spawning
 * - Integration with HealthSystem for enemy removal
 * - Performance optimization through batched updates and spatial partitioning
 * - Thread-safe enemy count tracking for concurrent access
 * 
 * Requirements: 2.2, 2.3, 2.4, 8.1, 8.2, 8.4
 */
public class EnemyManager {
    
    // Active enemies list with optimized capacity for 50 enemies
    private final Array<PlayerActor> activeEnemies;
    
    // Health system integration
    private final HealthSystem healthSystem;
    
    // Score tracking integration
    private com.plv.uberplayground.scoring.ScoreTracker scoreTracker;
    
    // GameScreen reference for deferred physics cleanup
    private com.plv.uberplayground.screens.GameScreen gameScreen;
    
    // EnemySpawner reference for pool integration
    private EnemySpawner enemySpawner;
    
    // Enhanced tracking for dynamic spawning
    private volatile int cachedEnemyCount;
    private boolean countCacheDirty;
    
    // Performance tracking
    private long lastUpdateTime;
    private int updateCount;
    
    // Performance optimization for larger enemy counts
    private static final int SPATIAL_PARTITIONING_THRESHOLD = 10;
    private static final int BATCH_UPDATE_SIZE = 5;
    
    /**
     * Creates a new EnemyManager instance with enhanced tracking capabilities.
     * Initializes with optimized capacity for dynamic enemy spawning.
     * @param healthSystem The health system to integrate with for enemy lifecycle management
     */
    public EnemyManager(HealthSystem healthSystem) {
        // Initialize with capacity for 50 enemies to avoid array resizing
        this.activeEnemies = new Array<>(SpawnConfiguration.MAX_ENEMIES);
        this.healthSystem = healthSystem;
        this.scoreTracker = null; // Will be set via setScoreTracker method
        this.gameScreen = null;
        this.lastUpdateTime = TimeUtils.millis();
        this.updateCount = 0;
        
        // Initialize enhanced tracking
        this.cachedEnemyCount = 0;
        this.countCacheDirty = false;
    }
    
    /**
     * Sets the ScoreTracker reference for enemy kill score tracking.
     * @param scoreTracker The ScoreTracker instance
     */
    public void setScoreTracker(com.plv.uberplayground.scoring.ScoreTracker scoreTracker) {
        this.scoreTracker = scoreTracker;
    }
    
    /**
     * Sets the GameScreen reference for deferred physics cleanup.
     * @param gameScreen The GameScreen instance
     */
    public void setGameScreen(com.plv.uberplayground.screens.GameScreen gameScreen) {
        this.gameScreen = gameScreen;
    }
    
    /**
     * Sets the EnemySpawner reference for pool integration.
     * @param enemySpawner The EnemySpawner instance
     */
    public void setEnemySpawner(EnemySpawner enemySpawner) {
        this.enemySpawner = enemySpawner;
    }
    
    /**
     * Registers a new enemy with the manager.
     * Enhanced with accurate count tracking and improved limit enforcement.
     * Initializes enemy-specific components and adds to active enemy list.
     * 
     * @param enemy The enemy to register
     * @return true if enemy was successfully registered, false if registration failed
     */
    public boolean addEnemy(PlayerActor enemy) {
        if (enemy == null) {
            return false;
        }
        
        // Thread-safe enemy count check with accurate tracking
        synchronized (activeEnemies) {
            // Check enemy count limit with precise enforcement
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
            
            // Update cached count for performance
            cachedEnemyCount = activeEnemies.size;
            countCacheDirty = false;
            
            return true;
        }
    }
    
    /**
     * Removes an enemy from the manager.
     * Enhanced with accurate count tracking and optimized cleanup.
     * Cleans up enemy-specific components and removes from active enemy list.
     * 
     * @param enemy The enemy to remove
     * @return true if enemy was successfully removed, false if enemy was not found
     */
    public boolean removeEnemy(PlayerActor enemy) {
        if (enemy == null) {
            return false;
        }
        
        // Thread-safe enemy removal with accurate count tracking
        synchronized (activeEnemies) {
            // Remove from active enemies list
            boolean removed = activeEnemies.removeValue(enemy, true);
            
            if (removed) {
                // Remove from health system
                healthSystem.removeEnemy(enemy);
                
                // Return enemy to pool if spawner is available
                if (enemySpawner != null && enemySpawner.getEnemyPool() != null) {
                    enemySpawner.getEnemyPool().free(enemy);
                } else {
                    // Fallback: remove from stage if no pool available
                    if (enemy.getStage() != null) {
                        enemy.remove();
                    }
                    // Clear enemy flag
                    enemy.setIsEnemy(false);
                }
                
                // Update cached count for performance
                cachedEnemyCount = activeEnemies.size;
                countCacheDirty = false;
            }
            
            return removed;
        }
    }
    
    /**
     * Updates all active enemies and processes any that need to be removed.
     * Enhanced with optimized batch processing for larger enemy counts.
     * This method should be called once per frame from the main game loop.
     * 
     * @param delta Time elapsed since last update in seconds
     */
    public void updateAll(float delta) {
        // Start performance monitoring
        PerformanceOptimizer.startFrame();
        
        updateCount++;
        
        // Thread-safe update processing
        synchronized (activeEnemies) {
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
                
                // Increment score for enemy kill (Requirement 1.1)
                if (scoreTracker != null) {
                    scoreTracker.incrementScore();
                    com.badlogic.gdx.Gdx.app.log("Score", "Enemy destroyed! Score incremented.");
                }
                
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
                
                // Remove from enemy manager (this will update cached count)
                removeEnemy(enemy);
            }
            
            // Optimized behavior updates with enhanced spatial partitioning for larger enemy counts
            if (activeEnemies.size > SPATIAL_PARTITIONING_THRESHOLD) {
                // Use spatial partitioning for performance when many enemies are active
                Array<PlayerActor>[][] spatialGrid = PerformanceOptimizer.spatialPartition(activeEnemies);
                
                // Batch update behaviors for better performance
                updateEnemiesBatched(delta, spatialGrid);
            } else {
                // Simple update for small enemy counts
                updateEnemiesSimple(delta);
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
        PerformanceOptimizer.endFrame(delta);
    }
    
    /**
     * Updates enemies in batches for improved performance with large enemy counts.
     * @param delta Time elapsed since last update
     * @param spatialGrid Spatial partitioning grid for optimization
     */
    private void updateEnemiesBatched(float delta, Array<PlayerActor>[][] spatialGrid) {
        int processed = 0;
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
            
            processed++;
            
            // Yield processing every batch to maintain frame rate
            if (processed % BATCH_UPDATE_SIZE == 0) {
                // Allow other systems to process if needed
                Thread.yield();
            }
        }
    }
    
    /**
     * Simple enemy update for small enemy counts.
     * @param delta Time elapsed since last update
     */
    private void updateEnemiesSimple(float delta) {
        for (PlayerActor enemy : activeEnemies) {
            if (enemy.getBehaviorState() != null) {
                enemy.getBehaviorState().update(delta);
            }
        }
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
     * Gets the current number of active enemies with optimized count tracking.
     * Uses cached count for performance when possible.
     * @return Number of enemies currently managed by this system
     */
    public int getEnemyCount() {
        // Use cached count if available and valid
        if (!countCacheDirty) {
            return cachedEnemyCount;
        }
        
        // Thread-safe count retrieval
        synchronized (activeEnemies) {
            cachedEnemyCount = activeEnemies.size;
            countCacheDirty = false;
            return cachedEnemyCount;
        }
    }
    
    /**
     * Checks if the enemy count limit has been reached.
     * Enhanced with accurate limit enforcement for dynamic spawning.
     * @return true if at maximum enemy capacity, false otherwise
     */
    public boolean isAtMaxCapacity() {
        return getEnemyCount() >= SpawnConfiguration.MAX_ENEMIES;
    }
    
    /**
     * Gets the maximum number of enemies allowed.
     * @return Maximum enemy count from configuration
     */
    public int getMaxEnemyCount() {
        return SpawnConfiguration.MAX_ENEMIES;
    }
    
    /**
     * Gets the current enemy capacity utilization as a percentage.
     * Useful for dynamic spawning decisions and performance monitoring.
     * @return Capacity utilization from 0.0 to 1.0
     */
    public float getCapacityUtilization() {
        return (float) getEnemyCount() / SpawnConfiguration.MAX_ENEMIES;
    }
    
    /**
     * Checks if there is room for additional enemies.
     * @param additionalEnemies Number of additional enemies to check for
     * @return true if there is room for the specified number of additional enemies
     */
    public boolean hasCapacityFor(int additionalEnemies) {
        return (getEnemyCount() + additionalEnemies) <= SpawnConfiguration.MAX_ENEMIES;
    }
    
    /**
     * Removes all enemies from the manager.
     * Enhanced with optimized cleanup for larger enemy counts and pool integration.
     * This should be called when resetting the game state or transitioning to game over.
     */
    public void clearAllEnemies() {
        synchronized (activeEnemies) {
            // Return all enemies to pool if available
            if (enemySpawner != null && enemySpawner.getEnemyPool() != null) {
                // Use pool's clearAllActive method for efficient cleanup
                enemySpawner.getEnemyPool().clearAllActive();
            } else {
                // Fallback: manual cleanup
                for (PlayerActor enemy : activeEnemies) {
                    enemy.setIsEnemy(false);
                    
                    // Remove from stage if it's on a stage
                    if (enemy.getStage() != null) {
                        enemy.remove();
                    }
                }
            }
            
            // Remove all enemies from health system
            for (PlayerActor enemy : activeEnemies) {
                healthSystem.removeEnemy(enemy);
            }
            
            // Clear the active enemies list
            activeEnemies.clear();
            
            // Update cached count
            cachedEnemyCount = 0;
            countCacheDirty = false;
        }
        
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
     * Enhanced with optimized spatial search for larger enemy counts.
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
        
        synchronized (activeEnemies) {
            // Use optimized search for larger enemy counts
            if (activeEnemies.size > SPATIAL_PARTITIONING_THRESHOLD) {
                // For large enemy counts, use spatial partitioning if available
                // Fall back to linear search for now, but could be optimized with spatial data structures
                for (PlayerActor enemy : activeEnemies) {
                    float dx = enemy.getX() - centerX;
                    float dy = enemy.getY() - centerY;
                    float distanceSquared = dx * dx + dy * dy;
                    
                    if (distanceSquared <= radiusSquared) {
                        nearbyEnemies.add(enemy);
                    }
                }
            } else {
                // Simple linear search for small enemy counts
                for (PlayerActor enemy : activeEnemies) {
                    float dx = enemy.getX() - centerX;
                    float dy = enemy.getY() - centerY;
                    float distanceSquared = dx * dx + dy * dy;
                    
                    if (distanceSquared <= radiusSquared) {
                        nearbyEnemies.add(enemy);
                    }
                }
            }
        }
        
        return nearbyEnemies;
    }
    
    /**
     * Gets performance statistics for monitoring system efficiency.
     * Enhanced with capacity utilization and performance metrics.
     * @return String containing current performance metrics
     */
    public String getPerformanceStats() {
        float utilization = getCapacityUtilization() * 100;
        return String.format("Enemies: %d/%d (%.1f%%), Updates/sec: %d", 
                           getEnemyCount(), 
                           SpawnConfiguration.MAX_ENEMIES,
                           utilization,
                           updateCount);
    }
    
    /**
     * Validates the integrity of the enemy management system.
     * Enhanced with additional consistency checks for dynamic spawning.
     * Checks for consistency between active enemies and health system registration.
     * 
     * @return true if system is in a valid state, false if inconsistencies are found
     */
    public boolean validateSystemIntegrity() {
        synchronized (activeEnemies) {
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
            
            // Validate cached count consistency
            if (!countCacheDirty && cachedEnemyCount != activeEnemies.size) {
                return false;
            }
            
            // Check that we don't exceed maximum capacity
            if (activeEnemies.size > SpawnConfiguration.MAX_ENEMIES) {
                return false;
            }
            
            return true;
        }
    }
    
    /**
     * Forces a refresh of the cached enemy count.
     * Useful for debugging or when count consistency is suspected.
     */
    public void refreshEnemyCount() {
        synchronized (activeEnemies) {
            cachedEnemyCount = activeEnemies.size;
            countCacheDirty = false;
        }
    }
    
    /**
     * Gets detailed system statistics for performance monitoring.
     * @return Detailed statistics including memory usage and performance metrics
     */
    public String getDetailedStats() {
        synchronized (activeEnemies) {
            StringBuilder stats = new StringBuilder();
            stats.append(getPerformanceStats()).append("\n");
            stats.append("Spatial Partitioning: ").append(activeEnemies.size > SPATIAL_PARTITIONING_THRESHOLD ? "Active" : "Inactive").append("\n");
            stats.append("Batch Size: ").append(BATCH_UPDATE_SIZE).append("\n");
            stats.append("Cache Status: ").append(countCacheDirty ? "Dirty" : "Clean").append("\n");
            return stats.toString();
        }
    }
}