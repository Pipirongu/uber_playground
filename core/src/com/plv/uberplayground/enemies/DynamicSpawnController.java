package com.plv.uberplayground.enemies;

import com.plv.uberplayground.enemies.EnhancedSpawnConfig.ConfigurationChangeListener;
import com.plv.uberplayground.game.GameStateManager;
import com.plv.uberplayground.game.GameStateManager.GameState;
import com.plv.uberplayground.game.GameStateManager.GameStateListener;

/**
 * Central coordinator for dynamic enemy spawning system.
 * Integrates ScalingCalculator and GameStateManager to provide dynamic spawn rate calculation
 * and updates based on game duration and state changes.
 * Responds to runtime configuration changes through the ConfigurationChangeListener interface.
 */
public class DynamicSpawnController implements GameStateListener, ConfigurationChangeListener {
    
    // Core state management
    private float gameDuration;
    private boolean isActive;
    private GameState currentState;
    
    // Scaling components
    private ScalingCalculator scalingCalculator;
    private final EnhancedSpawnConfig config;
    
    // Integration points
    private final EnemySpawner enemySpawner;
    private final GameStateManager gameStateManager;
    
    // Performance monitoring
    private float lastSpawnRate;
    private boolean performanceReductionActive;
    private float currentTargetInterval;
    
    /**
     * Create a new DynamicSpawnController with the required dependencies.
     * 
     * @param enemySpawner The enemy spawner to control
     * @param gameStateManager The game state manager for state tracking
     * @param config The enhanced spawn configuration
     */
    public DynamicSpawnController(EnemySpawner enemySpawner, GameStateManager gameStateManager, 
                                 EnhancedSpawnConfig config) {
        this.enemySpawner = enemySpawner;
        this.gameStateManager = gameStateManager;
        this.config = config;
        this.scalingCalculator = new ScalingCalculator(config);
        
        this.gameDuration = 0.0f;
        this.isActive = false;
        this.currentState = GameState.MENU;
        this.lastSpawnRate = config.getBaseSpawnRate();
        this.performanceReductionActive = false;
        this.currentTargetInterval = 1.0f / config.getBaseSpawnRate();
        
        // Register as listener for game state changes
        gameStateManager.addListener(this);
        
        // Register as listener for configuration changes
        config.addConfigurationChangeListener(this);
    }
    
    /**
     * Update the dynamic spawn controller with the current frame delta.
     * This should be called every frame during active gameplay.
     * 
     * @param delta The time elapsed since the last frame in seconds
     */
    public void update(float delta) {
        if (!isActive || !gameStateManager.isGameplayActive()) {
            return;
        }
        
        // Update game duration only during active gameplay
        gameDuration += delta;
        
        // Calculate new spawn rate based on current game duration
        float newSpawnRate = scalingCalculator.calculateSpawnRate(gameDuration);
        
        // Apply performance reduction if active
        if (performanceReductionActive) {
            newSpawnRate *= config.getPerformanceReductionFactor();
        }
        
        // Update spawn rate if it has changed significantly
        if (Math.abs(newSpawnRate - lastSpawnRate) > 0.001f) {
            updateSpawnRate(newSpawnRate);
            lastSpawnRate = newSpawnRate;
        }
        
        // Handle dynamic spawning by manipulating the existing spawn system
        handleDynamicSpawning(delta, newSpawnRate);
    }
    
    /**
     * Handle dynamic spawning by working with the existing EnemySpawner's fixed interval system.
     * This method implements a probability-based approach to achieve variable spawn rates.
     * 
     * @param delta The frame delta time
     * @param targetSpawnRate The desired spawn rate in enemies per second
     */
    private void handleDynamicSpawning(float delta, float targetSpawnRate) {
        if (enemySpawner == null) {
            // Handle null spawner gracefully for testing
            return;
        }
        
        // Calculate the probability of spawning in this frame based on target rate
        float spawnProbabilityPerSecond = targetSpawnRate;
        float spawnProbabilityThisFrame = spawnProbabilityPerSecond * delta;
        
        // If the spawn timer is close to the fixed interval and we want faster spawning,
        // we can trigger spawning early based on probability
        float currentTimer = enemySpawner.getSpawnTimer();
        float fixedInterval = 5.0f; // SpawnConfiguration.SPAWN_INTERVAL
        
        // If we're approaching the fixed interval time
        if (currentTimer >= fixedInterval * 0.8f) {
            // Check if we should spawn early based on our target rate
            float desiredInterval = 1.0f / targetSpawnRate;
            
            if (desiredInterval < fixedInterval && currentTimer >= desiredInterval) {
                // Force spawn if we've reached our desired interval
                if (enemySpawner.canSpawn()) {
                    enemySpawner.forceSpawn();
                }
            }
        }
    }
    
    /**
     * Update the spawn rate in the enemy spawner.
     * Since the EnemySpawner uses a fixed interval, we simulate dynamic rates by
     * controlling when spawning is enabled and manipulating the spawn timer.
     * 
     * @param spawnRate The new spawn rate in enemies per second
     */
    private void updateSpawnRate(float spawnRate) {
        if (enemySpawner == null) {
            // Handle null spawner gracefully for testing
            return;
        }
        
        if (spawnRate <= 0) {
            // Disable spawning if rate is zero or negative
            enemySpawner.setSpawningEnabled(false);
            return;
        }
        
        // Enable spawning
        enemySpawner.setSpawningEnabled(true);
        
        // Calculate desired spawn interval from rate
        float desiredInterval = 1.0f / spawnRate;
        
        // If desired interval is shorter than current timer, reset to spawn immediately
        // This allows for faster spawning when rates increase
        if (enemySpawner.getSpawnTimer() > desiredInterval) {
            enemySpawner.resetSpawnTimer();
        }
        
        // Store the current target interval for future reference
        this.currentTargetInterval = desiredInterval;
    }
    
    /**
     * Set the current game state and handle state transitions.
     * 
     * @param state The new game state
     */
    public void setGameState(GameState state) {
        GameState previousState = this.currentState;
        this.currentState = state;
        
        handleStateTransition(previousState, state);
    }
    
    /**
     * Handle transitions between game states.
     * 
     * @param oldState The previous game state
     * @param newState The new game state
     */
    private void handleStateTransition(GameState oldState, GameState newState) {
        switch (newState) {
            case PLAYING:
                if (oldState == GameState.PAUSED && config.isPreserveScalingOnPause()) {
                    // Resume with preserved scaling state
                    resumeScaling();
                } else if (oldState == GameState.MENU || oldState == GameState.GAME_OVER) {
                    // Start new game - always reset when coming from GAME_OVER or MENU
                    resetForNewGame();
                }
                break;
                
            case PAUSED:
                pauseScaling();
                break;
                
            case GAME_OVER:
                // Always pause scaling during game over
                pauseScaling();
                // Reset will happen when new game starts (PLAYING state from GAME_OVER)
                break;
                
            case MENU:
                resetForNewGame();
                break;
        }
    }
    
    /**
     * Get the current spawn rate calculated by the scaling system.
     * 
     * @return The current spawn rate in enemies per second
     */
    public float getCurrentSpawnRate() {
        if (!isActive) {
            return config.getBaseSpawnRate();
        }
        
        float baseRate = scalingCalculator.calculateSpawnRate(gameDuration);
        
        if (performanceReductionActive) {
            return baseRate * config.getPerformanceReductionFactor();
        }
        
        return baseRate;
    }
    
    /**
     * Get the current game duration being tracked.
     * 
     * @return The game duration in seconds
     */
    public float getGameDuration() {
        return gameDuration;
    }
    
    /**
     * Check if the dynamic scaling is currently active.
     * 
     * @return true if scaling is active, false otherwise
     */
    public boolean isActive() {
        return isActive;
    }
    
    /**
     * Reset the controller for a new game session.
     * Resets game duration and scaling state to initial values.
     */
    public void resetForNewGame() {
        gameDuration = 0.0f;
        isActive = true;
        performanceReductionActive = false;
        lastSpawnRate = config.getBaseSpawnRate();
        
        // Reset spawn rate to base rate
        updateSpawnRate(config.getBaseSpawnRate());
    }
    
    /**
     * Pause the scaling system.
     * Stops duration tracking and spawn rate updates.
     */
    public void pauseScaling() {
        isActive = false;
        if (enemySpawner != null) {
            enemySpawner.setSpawningEnabled(false);
        }
    }
    
    /**
     * Resume the scaling system.
     * Continues duration tracking and spawn rate updates from where it left off.
     */
    public void resumeScaling() {
        isActive = true;
        
        // Recalculate and apply current spawn rate
        float currentRate = getCurrentSpawnRate();
        updateSpawnRate(currentRate);
    }
    
    /**
     * Enable or disable performance reduction mode.
     * When enabled, spawn rates are reduced by the configured performance reduction factor.
     * 
     * @param enabled true to enable performance reduction, false to disable
     */
    public void setPerformanceReduction(boolean enabled) {
        if (this.performanceReductionActive != enabled) {
            this.performanceReductionActive = enabled;
            
            // Immediately update spawn rate with new performance setting
            if (isActive) {
                float currentRate = getCurrentSpawnRate();
                updateSpawnRate(currentRate);
            }
        }
    }
    
    /**
     * Check if performance reduction is currently active.
     * 
     * @return true if performance reduction is active, false otherwise
     */
    public boolean isPerformanceReductionActive() {
        return performanceReductionActive;
    }
    
    /**
     * Get the scaling calculator used by this controller.
     * 
     * @return The scaling calculator instance
     */
    public ScalingCalculator getScalingCalculator() {
        return scalingCalculator;
    }
    
    /**
     * Get the current target spawn interval being used.
     * 
     * @return The target spawn interval in seconds
     */
    public float getCurrentTargetInterval() {
        return currentTargetInterval;
    }
    
    /**
     * Get the scaling factor for the current game duration.
     * 
     * @return The current scaling factor
     */
    public float getCurrentScalingFactor() {
        return scalingCalculator.calculateScalingFactor(gameDuration);
    }
    
    /**
     * Check if the spawn rate has reached the maximum configured rate.
     * 
     * @return true if at maximum rate, false otherwise
     */
    public boolean isAtMaximumSpawnRate() {
        float currentRate = getCurrentSpawnRate();
        return Math.abs(currentRate - config.getMaxSpawnRate()) < 0.001f;
    }
    
    /**
     * Get the percentage progress toward the target duration.
     * 
     * @return Progress as a value between 0.0 and 1.0
     */
    public float getProgressToTarget() {
        return Math.min(gameDuration / config.getTargetDuration(), 1.0f);
    }
    
    /**
     * Get the configuration used by this controller.
     * 
     * @return The enhanced spawn configuration
     */
    public EnhancedSpawnConfig getConfig() {
        return config;
    }
    
    // GameStateListener implementation
    
    @Override
    public void onStateChanged(GameState oldState, GameState newState) {
        setGameState(newState);
    }
    
    @Override
    public void onDurationUpdated(float totalDuration, float activeDuration) {
        // The controller manages its own duration tracking during active gameplay
        // This callback can be used for additional synchronization if needed
    }
    
    // ConfigurationChangeListener implementation
    
    @Override
    public void onConfigurationChanged(EnhancedSpawnConfig config) {
        handleConfigurationChange();
    }
    
    /**
     * Handle configuration changes by updating internal components and recalculating spawn rates.
     * This method ensures that runtime configuration updates take effect immediately.
     */
    private void handleConfigurationChange() {
        try {
            // Recreate the scaling calculator with the updated configuration
            this.scalingCalculator = new ScalingCalculator(config);
            
            // If we're currently active, recalculate and apply the new spawn rate
            if (isActive && gameStateManager.isGameplayActive()) {
                float newSpawnRate = scalingCalculator.calculateSpawnRate(gameDuration);
                
                // Apply performance reduction if active
                if (performanceReductionActive) {
                    newSpawnRate *= config.getPerformanceReductionFactor();
                }
                
                // Update spawn rate immediately
                updateSpawnRate(newSpawnRate);
                lastSpawnRate = newSpawnRate;
                
                logInfo("Configuration updated - new spawn rate: " + newSpawnRate);
            }
            
            // If configuration changes affect game state behavior, handle appropriately
            if (!config.isPreserveScalingOnPause() && currentState == GameState.PAUSED) {
                // If preserve scaling on pause is now disabled and we're paused, reset scaling
                resetForNewGame();
            }
            
        } catch (Exception e) {
            logError("Error handling configuration change: " + e.getMessage());
        }
    }
    
    /**
     * Reload configuration from file and apply changes immediately.
     * This method provides a convenient way to trigger hot-reload of configuration.
     * 
     * @return true if configuration was successfully reloaded, false otherwise
     */
    public boolean reloadConfiguration() {
        return config.reloadConfiguration();
    }
    
    private void logInfo(String message) {
        if (com.badlogic.gdx.Gdx.app != null) {
            com.badlogic.gdx.Gdx.app.log("DynamicSpawnController", message);
        } else {
            System.out.println("DynamicSpawnController: " + message);
        }
    }
    
    private void logError(String message) {
        if (com.badlogic.gdx.Gdx.app != null) {
            com.badlogic.gdx.Gdx.app.error("DynamicSpawnController", message);
        } else {
            System.err.println("DynamicSpawnController: " + message);
        }
    }
    
    /**
     * Clean up resources and remove listeners.
     * Should be called when the controller is no longer needed.
     */
    public void dispose() {
        if (gameStateManager != null) {
            gameStateManager.removeListener(this);
        }
        if (config != null) {
            config.removeConfigurationChangeListener(this);
        }
    }
}