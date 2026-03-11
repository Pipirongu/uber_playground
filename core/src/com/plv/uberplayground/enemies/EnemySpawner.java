package com.plv.uberplayground.enemies;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.plv.uberplayground.actors.PlayerActor;
import com.plv.uberplayground.actors.PlayerShip;

/**
 * Manages the creation and placement of new enemy entities with safe spawning mechanics.
 * 
 * The EnemySpawner provides:
 * - Dynamic enemy spawning with configurable intervals via DynamicSpawnController
 * - Backward compatibility with fixed 5-second intervals when no dynamic controller is set
 * - Safe distance validation (200-pixel radius from player)
 * - Spawn location collision detection and retry logic
 * - Maximum enemy count enforcement (50 enemies)
 * - Random behavior assignment during spawning
 * - Integration with enhanced enemy object pooling
 * 
 * Dynamic Spawning Integration:
 * - When a DynamicSpawnController is set and active, spawn intervals are dynamically calculated
 * - Falls back to manual or default intervals when dynamic control is not available
 * - Maintains full backward compatibility with existing fixed-interval spawning
 * 
 * Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 2.1, 2.2
 */
public class EnemySpawner {
    
    // Core dependencies
    private final World world;
    private final Stage gameStage;
    private final EnemyManager enemyManager;
    private final PlayerShip playerShip;
    
    // Enhanced enemy object pooling
    private final EnemyPool enemyPool;
    
    // Dynamic spawn control
    private DynamicSpawnController dynamicSpawnController;
    
    // Spawn timing
    private float spawnTimer;
    private boolean spawningEnabled;
    private float currentSpawnInterval;
    
    // Game boundaries for spawn location validation
    private final float gameWidth;
    private final float gameHeight;
    private final float borderSize;
    
    // Collision detection for spawn validation
    private final Array<PlayerActor> existingEntities;
    
    // Spawn distribution tracking
    private final Array<Vector2> recentSpawnLocations;
    private final int maxRecentSpawns = 5; // Track last 5 spawn locations
    private final float distributionRadius = 100.0f; // Minimum distance between recent spawns
    
    /**
     * Creates a new EnemySpawner instance.
     * 
     * @param world The Box2D world for physics simulation
     * @param gameStage The stage where enemies will be added
     * @param enemyManager The enemy manager for registration
     * @param playerShip The player ship for safe distance calculations
     * @param gameWidth Width of the game area
     * @param gameHeight Height of the game area
     */
    public EnemySpawner(World world, Stage gameStage, EnemyManager enemyManager, 
                       PlayerShip playerShip, float gameWidth, float gameHeight) {
        this.world = world;
        this.gameStage = gameStage;
        this.enemyManager = enemyManager;
        this.playerShip = playerShip;
        this.gameWidth = gameWidth;
        this.gameHeight = gameHeight;
        this.borderSize = 50.0f; // Safe border from edges
        
        // Initialize enhanced enemy object pool
        this.enemyPool = new EnemyPool(world, gameStage);
        
        this.spawnTimer = 0.0f;
        this.spawningEnabled = true;
        this.currentSpawnInterval = SpawnConfiguration.SPAWN_INTERVAL; // Default to fixed interval
        this.existingEntities = new Array<>();
        this.recentSpawnLocations = new Array<>();
    }
    
    /**
     * Updates the spawner and handles timed enemy spawning.
     * This method should be called once per frame from the main game loop.
     * 
     * @param delta Time elapsed since last update in seconds
     */
    public void update(float delta) {
        if (!spawningEnabled) {
            return;
        }
        
        // Update enemy pool lifecycle
        enemyPool.updateAll(delta);
        
        // Update spawn timer
        spawnTimer += delta;
        
        // Get current spawn interval (dynamic or fixed)
        float activeSpawnInterval = getCurrentActiveSpawnInterval();
        
        // Check if it's time to spawn and if we can spawn
        if (spawnTimer >= activeSpawnInterval && canSpawn()) {
            attemptSpawn();
            spawnTimer = 0.0f; // Reset timer regardless of spawn success
        }
    }
    
    /**
     * Gets the current active spawn interval, prioritizing dynamic control if available.
     * @return Current spawn interval in seconds
     */
    private float getCurrentActiveSpawnInterval() {
        if (dynamicSpawnController != null && dynamicSpawnController.isActive()) {
            return dynamicSpawnController.getCurrentTargetInterval();
        }
        return currentSpawnInterval; // Fall back to current interval (default or manually set)
    }
    
    /**
     * Enables or disables enemy spawning.
     * @param enabled true to enable spawning, false to disable
     */
    public void setSpawningEnabled(boolean enabled) {
        this.spawningEnabled = enabled;
    }
    
    /**
     * Checks if spawning is currently possible.
     * @return true if spawning can occur, false otherwise
     */
    public boolean canSpawn() {
        return spawningEnabled && !enemyManager.isAtMaxCapacity() && enemyPool.hasCapacity();
    }
    
    /**
     * Attempts to spawn a new enemy with safe distance and collision validation.
     * Uses retry logic with maximum attempts to find a valid spawn location.
     */
    private void attemptSpawn() {
        Vector2 spawnLocation = findValidSpawnLocation();
        
        if (spawnLocation != null) {
            createEnemyAtLocation(spawnLocation);
        }
        // If no valid location found after max attempts, skip this spawn cycle
    }
    
    /**
     * Finds a valid spawn location with safe distance and collision avoidance.
     * Implements retry logic with maximum 10 attempts as specified in requirements.
     * Ensures good distribution across the game map by avoiding clustering.
     * 
     * @return Valid spawn location, or null if no location found after max attempts
     */
    private Vector2 findValidSpawnLocation() {
        Vector2 playerPosition = getPlayerPosition();
        int safeDistanceRejections = 0;
        int collisionRejections = 0;
        int distributionRejections = 0;
        
        for (int attempt = 0; attempt < SpawnConfiguration.MAX_SPAWN_ATTEMPTS; attempt++) {
            // Generate random location within game boundaries with better distribution
            Vector2 candidateLocation = generateDistributedSpawnLocation();
            
            // Check safe distance from player
            if (!isLocationSafe(candidateLocation, playerPosition)) {
                safeDistanceRejections++;
                continue;
            }
            
            // Check collision with existing entities
            if (hasCollisionWithEntities(candidateLocation)) {
                collisionRejections++;
                continue;
            }
            
            // Check distribution to avoid clustering
            if (!isWellDistributed(candidateLocation)) {
                distributionRejections++;
                continue;
            }
            
            // Valid location found - track it for distribution
            trackSpawnLocation(candidateLocation);
            return candidateLocation;
        }
        
        // Log spawn failure statistics
        System.out.println("Spawn failed after " + SpawnConfiguration.MAX_SPAWN_ATTEMPTS + 
                         " attempts. Safe distance: " + safeDistanceRejections + 
                         ", Collisions: " + collisionRejections + 
                         ", Distribution: " + distributionRejections);
        
        return null; // No valid location found after max attempts
    }
    
    /**
     * Checks if a spawn location is safe (outside safe distance from player).
     * Implements the 200-pixel safe distance requirement from the player position.
     * 
     * @param location The candidate spawn location
     * @param playerPosition The current player position
     * @return true if location is safe, false if too close to player
     */
    private boolean isLocationSafe(Vector2 location, Vector2 playerPosition) {
        if (playerPosition == null) {
            return true; // If no player position, assume safe
        }
        
        float distance = location.dst(playerPosition);
        boolean isSafe = distance >= SpawnConfiguration.SAFE_DISTANCE;
        
        // Log unsafe spawn attempts for debugging
        if (!isSafe) {
            System.out.println("Spawn location rejected: distance " + distance + 
                             " < safe distance " + SpawnConfiguration.SAFE_DISTANCE);
        }
        
        return isSafe;
    }
    
    /**
     * Generates a spawn location with improved distribution across the game map.
     * Uses weighted random selection to favor less crowded areas.
     * 
     * @return A candidate spawn location
     */
    private Vector2 generateDistributedSpawnLocation() {
        // Simple approach: generate random location within boundaries
        // Future enhancement could use grid-based or weighted distribution
        float x = borderSize + (float) Math.random() * (gameWidth - 2 * borderSize);
        float y = borderSize + (float) Math.random() * (gameHeight - 2 * borderSize);
        return new Vector2(x, y);
    }
    
    /**
     * Checks if a spawn location is well distributed relative to recent spawns.
     * Prevents clustering by ensuring minimum distance from recent spawn locations.
     * 
     * @param location The candidate spawn location
     * @return true if well distributed, false if too close to recent spawns
     */
    private boolean isWellDistributed(Vector2 location) {
        for (Vector2 recentSpawn : recentSpawnLocations) {
            float distance = location.dst(recentSpawn);
            if (distance < distributionRadius) {
                return false; // Too close to a recent spawn
            }
        }
        return true;
    }
    
    /**
     * Tracks a spawn location for distribution analysis.
     * Maintains a rolling window of recent spawn locations.
     * 
     * @param location The spawn location to track
     */
    private void trackSpawnLocation(Vector2 location) {
        recentSpawnLocations.add(location.cpy());
        
        // Maintain rolling window of recent spawns
        while (recentSpawnLocations.size > maxRecentSpawns) {
            recentSpawnLocations.removeIndex(0);
        }
    }
    
    /**
     * Checks if a spawn location would collide with existing entities.
     * Implements comprehensive collision detection with all game entities.
     * 
     * @param location The candidate spawn location
     * @return true if collision detected, false if location is clear
     */
    /**
     * Checks if a spawn location would collide with existing entities.
     * Implements comprehensive collision detection with all game entities.
     * 
     * @param location The candidate spawn location
     * @return true if collision detected, false if location is clear
     */
    private boolean hasCollisionWithEntities(Vector2 location) {
        // Update existing entities list
        updateExistingEntitiesList();
        
        float collisionRadius = 30.0f; // Minimum distance between entities
        
        // Check collision with active enemies
        for (PlayerActor entity : existingEntities) {
            if (entity != null && entity.getBody() != null) {
                Vector2 entityPosition = entity.getBody().getPosition();
                float distance = location.dst(entityPosition);
                
                if (distance < collisionRadius) {
                    return true; // Collision detected
                }
            }
        }
        
        // Check collision with player ship separately
        if (playerShip != null && playerShip.getBody() != null) {
            Vector2 playerPosition = playerShip.getBody().getPosition();
            float distance = location.dst(playerPosition);
            
            if (distance < collisionRadius) {
                return true; // Collision detected with player ship
            }
        }
        
        return false; // No collision
    }
    
    /**
     * Updates the list of existing entities for collision detection.
     * Includes all active enemies and the player ship for comprehensive collision avoidance.
     */
    private void updateExistingEntitiesList() {
        existingEntities.clear();
        
        // Add all active enemies
        Array<PlayerActor> activeEnemies = enemyManager.getActiveEnemies();
        existingEntities.addAll(activeEnemies);
        
        // Note: PlayerShip extends AnimatedPhysicsActor, not PlayerActor
        // We'll handle player ship collision separately in hasCollisionWithEntities
    }
    
    /**
     * Creates a new enemy at the specified location using object pooling.
     * Enhanced to use EnemyPool for efficient object reuse and reduced garbage collection.
     * @param location The spawn location for the new enemy
     */
    private void createEnemyAtLocation(Vector2 location) {
        // Obtain enemy from pool instead of creating new instance
        PlayerActor enemy = enemyPool.obtain(location);
        
        if (enemy == null) {
            // Pool exhausted - this shouldn't happen with proper capacity checking
            System.err.println("Warning: Enemy pool exhausted, cannot spawn enemy");
            return;
        }
        
        // Assign random behavior
        assignRandomBehavior(enemy);
        
        // Set initial player target for behaviors
        Vector2 playerPosition = getPlayerPosition();
        if (playerPosition != null) {
            enemy.setPlayerTarget(playerPosition);
        }
        
        // Register with enemy manager
        boolean registered = enemyManager.addEnemy(enemy);
        
        if (!registered) {
            // If registration failed, return enemy to pool
            enemyPool.free(enemy);
        }
    }
    
    /**
     * Assigns a random initial behavior to a newly spawned enemy.
     * Initializes behavior switching timers for autonomous behavior management.
     * Ensures proper behavior state initialization according to requirements.
     * 
     * @param enemy The enemy to assign behavior to
     */
    private void assignRandomBehavior(PlayerActor enemy) {
        if (enemy.getBehaviorState() == null) {
            System.err.println("Warning: Enemy has no behavior state - cannot assign behavior");
            return; // No behavior state available
        }
        
        // Assign random initial behavior from all available types
        SteeringBehaviorType randomBehavior = SteeringBehaviorType.getRandomBehavior();
        enemy.getBehaviorState().forceBehaviorSwitch(randomBehavior);
        
        // Set player target for behaviors that need it (seek, flee, orbit, intercept)
        if (playerShip != null) {
            Vector2 playerPosition = playerShip.getBody().getPosition();
            enemy.setPlayerTarget(playerPosition);
        }
        
        // Initialize behavior-specific data
        initializeBehaviorSpecificData(enemy, randomBehavior);
        
        // Log behavior assignment for debugging
        System.out.println("Assigned behavior " + randomBehavior + " to new enemy with switch interval " + 
                         String.format("%.1f", enemy.getBehaviorState().getSwitchInterval()) + "s");
    }
    
    /**
     * Initializes behavior-specific data for newly spawned enemies.
     * Ensures proper state initialization for each behavior type.
     * 
     * @param enemy The enemy to initialize
     * @param behavior The assigned behavior type
     */
    private void initializeBehaviorSpecificData(PlayerActor enemy, SteeringBehaviorType behavior) {
        switch (behavior) {
            case PATROL:
                // Generate random patrol waypoints within game boundaries
                Array<Vector2> waypoints = generateRandomPatrolWaypoints();
                enemy.setPatrolWaypoints(waypoints);
                enemy.setCurrentWaypointIndex(0);
                System.out.println("Initialized patrol behavior with " + waypoints.size + " waypoints");
                break;
                
            case ORBIT:
                // Initialize random orbit angle for varied starting positions
                float randomAngle = (float) (Math.random() * 2 * Math.PI);
                enemy.setOrbitAngle(randomAngle);
                System.out.println("Initialized orbit behavior with angle " + String.format("%.2f", randomAngle));
                break;
                
            case WANDER:
                // Wander behavior uses existing PlayerActor wander state
                System.out.println("Initialized wander behavior (uses existing state)");
                break;
                
            case SEEK:
                // Seek behavior targets player position (already set in assignRandomBehavior)
                System.out.println("Initialized seek behavior targeting player");
                break;
                
            case FLEE:
                // Flee behavior moves away from player (no special initialization needed)
                System.out.println("Initialized flee behavior");
                break;
                
            case INTERCEPT:
                // Intercept behavior predicts player movement (no special initialization needed)
                System.out.println("Initialized intercept behavior");
                break;
                
            case FLOCK:
                // Flocking behavior coordinates with nearby enemies (no special initialization needed)
                System.out.println("Initialized flock behavior");
                break;
                
            default:
                System.out.println("Unknown behavior type: " + behavior);
                break;
        }
    }
    
    /**
     * Generates random patrol waypoints for patrol behavior.
     * Creates 3-5 waypoints distributed across the game area for varied patrol patterns.
     * 
     * @return Array of waypoints within game boundaries
     */
    private Array<Vector2> generateRandomPatrolWaypoints() {
        Array<Vector2> waypoints = new Array<>();
        int waypointCount = 3 + (int) (Math.random() * 3); // 3-5 waypoints
        
        for (int i = 0; i < waypointCount; i++) {
            float x = borderSize + (float) Math.random() * (gameWidth - 2 * borderSize);
            float y = borderSize + (float) Math.random() * (gameHeight - 2 * borderSize);
            waypoints.add(new Vector2(x, y));
        }
        
        return waypoints;
    }
    
    /**
     * Gets the current player position for safe distance calculations.
     * @return Player position, or null if player not available
     */
    private Vector2 getPlayerPosition() {
        if (playerShip != null && playerShip.getBody() != null) {
            return playerShip.getBody().getPosition();
        }
        return null;
    }
    
    /**
     * Gets the current spawn timer value.
     * @return Current spawn timer in seconds
     */
    public float getSpawnTimer() {
        return spawnTimer;
    }
    
    /**
     * Gets whether spawning is currently enabled.
     * @return true if spawning is enabled, false otherwise
     */
    public boolean isSpawningEnabled() {
        return spawningEnabled;
    }
    
    /**
     * Resets the spawn timer to zero.
     * Useful for immediate spawning or timer synchronization.
     */
    public void resetSpawnTimer() {
        this.spawnTimer = 0.0f;
    }
    
    /**
     * Forces an immediate spawn attempt regardless of timer.
     * Still respects enemy count limits and safe distance validation.
     * @return true if spawn was successful, false otherwise
     */
    public boolean forceSpawn() {
        if (!canSpawn()) {
            return false;
        }
        
        Vector2 spawnLocation = findValidSpawnLocation();
        if (spawnLocation != null) {
            createEnemyAtLocation(spawnLocation);
            return true;
        }
        
        return false;
    }
    
    // Enhanced enemy pool management methods
    
    /**
     * Gets the enemy pool for direct access to pool operations.
     * @return The enemy pool instance
     */
    public EnemyPool getEnemyPool() {
        return enemyPool;
    }
    
    /**
     * Gets pool utilization statistics for monitoring.
     * @return Formatted string with pool performance metrics
     */
    public String getPoolStats() {
        return enemyPool.getPerformanceStats();
    }
    
    /**
     * Gets the current pool utilization as a percentage.
     * @return Pool utilization (0.0 to 1.0)
     */
    public float getPoolUtilization() {
        return enemyPool.getUtilization();
    }
    
    /**
     * Clears all active enemies from the pool.
     * Useful for game over or level transitions.
     */
    public void clearAllEnemies() {
        enemyPool.clearAllActive();
    }
    
    /**
     * Disposes the enemy pool and all resources.
     * Should be called when shutting down the spawner.
     */
    public void dispose() {
        enemyPool.dispose();
    }
    
    // Dynamic spawn controller integration methods
    
    /**
     * Sets the dynamic spawn controller for dynamic spawn rate management.
     * When set, the spawner will use dynamic intervals from the controller.
     * 
     * @param controller The dynamic spawn controller, or null to disable dynamic control
     */
    public void setDynamicSpawnController(DynamicSpawnController controller) {
        this.dynamicSpawnController = controller;
    }
    
    /**
     * Gets the current dynamic spawn controller.
     * @return The dynamic spawn controller, or null if not set
     */
    public DynamicSpawnController getDynamicSpawnController() {
        return dynamicSpawnController;
    }
    
    /**
     * Sets a manual spawn interval for backward compatibility.
     * This interval is used when no dynamic controller is active.
     * 
     * @param interval The spawn interval in seconds
     */
    public void setSpawnInterval(float interval) {
        if (interval > 0) {
            this.currentSpawnInterval = interval;
        }
    }
    
    /**
     * Gets the current manual spawn interval.
     * @return The current spawn interval in seconds
     */
    public float getSpawnInterval() {
        return currentSpawnInterval;
    }
    
    /**
     * Checks if dynamic spawn control is currently active.
     * @return true if dynamic controller is set and active, false otherwise
     */
    public boolean isDynamicSpawnActive() {
        return dynamicSpawnController != null && dynamicSpawnController.isActive();
    }
}