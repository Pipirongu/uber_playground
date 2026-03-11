package com.plv.uberplayground.enemies;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.plv.uberplayground.actors.PlayerShip;
import com.plv.uberplayground.game.GameStateManager;
import com.plv.uberplayground.game.GameStateManager.GameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for DynamicSpawnController.
 * Tests integration with EnemySpawner, game state change handling, and configuration updates.
 * 
 * **Validates: Requirements 1.1, 1.2, 1.3, 1.4**
 */
class DynamicSpawnControllerIntegrationTest {
    
    private World world;
    private Stage gameStage;
    private PlayerShip playerShip;
    private HealthSystem healthSystem;
    private EnemyManager enemyManager;
    private EnemySpawner enemySpawner;
    private GameStateManager gameStateManager;
    private EnhancedSpawnConfig config;
    private DynamicSpawnController controller;
    
    @BeforeEach
    void setUp() {
        // Create minimal test dependencies
        world = new World(new Vector2(0, 0), true);
        gameStage = new Stage();
        playerShip = null; // Can be null for these tests
        
        // Create real components for integration testing
        config = new EnhancedSpawnConfig();
        config.applyDefaults();
        
        gameStateManager = new GameStateManager();
        healthSystem = new HealthSystem();
        enemyManager = new EnemyManager(healthSystem);
        
        // Create EnemySpawner with proper constructor parameters
        enemySpawner = new EnemySpawner(world, gameStage, enemyManager, playerShip, 800.0f, 600.0f);
        
        // Create controller with real EnemySpawner
        controller = new DynamicSpawnController(enemySpawner, gameStateManager, config);
        
        // Set up the integration
        enemySpawner.setDynamicSpawnController(controller);
    }
    
    @Test
    @DisplayName("Should integrate with EnemySpawner and update spawn intervals")
    void shouldIntegrateWithEnemySpawnerAndUpdateSpawnIntervals() {
        // Given
        gameStateManager.setState(GameState.PLAYING);
        controller.resetForNewGame();
        
        float initialInterval = enemySpawner.getSpawnInterval();
        
        // When - simulate time progression to increase spawn rate
        for (int i = 0; i < 30; i++) {
            controller.update(1.0f); // 30 seconds
        }
        
        // Then - spawn interval should decrease (faster spawning)
        float finalInterval = enemySpawner.getSpawnInterval();
        assertTrue(finalInterval < initialInterval, 
                  "Spawn interval should decrease over time. Initial: " + initialInterval + 
                  ", Final: " + finalInterval);
        
        // Verify dynamic spawn is active
        assertTrue(enemySpawner.isDynamicSpawnActive());
        assertSame(controller, enemySpawner.getDynamicSpawnController());
    }
    
    @Test
    @DisplayName("Should handle game state changes and affect spawning behavior")
    void shouldHandleGameStateChangesAndAffectSpawningBehavior() {
        // Given - start game and build up some scaling
        gameStateManager.setState(GameState.PLAYING);
        controller.resetForNewGame();
        controller.update(20.0f); // Build up some duration
        
        float activeSpawnRate = controller.getCurrentSpawnRate();
        assertTrue(activeSpawnRate > config.getBaseSpawnRate(), 
                  "Spawn rate should have increased during gameplay");
        
        // When - pause the game
        gameStateManager.setState(GameState.PAUSED);
        
        // Then - controller should become inactive
        assertFalse(controller.isActive());
        
        // When - resume the game
        gameStateManager.setState(GameState.PLAYING);
        
        // Then - controller should become active again and preserve state
        assertTrue(controller.isActive());
        assertEquals(20.0f, controller.getGameDuration(), 0.001f);
        
        // When - game over
        gameStateManager.setState(GameState.GAME_OVER);
        
        // Then - should reset for next game
        assertEquals(0.0f, controller.getGameDuration(), 0.001f);
        assertEquals(config.getBaseSpawnRate(), controller.getCurrentSpawnRate(), 0.001f);
    }
    
    @Test
    @DisplayName("Should handle configuration updates during runtime")
    void shouldHandleConfigurationUpdatesDuringRuntime() {
        // Given
        gameStateManager.setState(GameState.PLAYING);
        controller.resetForNewGame();
        
        float originalBaseRate = config.getBaseSpawnRate();
        float originalMaxRate = config.getMaxSpawnRate();
        
        // When - update configuration
        config.setBaseSpawnRate(originalBaseRate * 2.0f);
        config.setMaxSpawnRate(originalMaxRate * 1.5f);
        
        // Trigger configuration reload
        assertTrue(controller.reloadConfiguration());
        
        // Then - new configuration should be applied
        assertEquals(originalBaseRate * 2.0f, controller.getCurrentSpawnRate(), 0.001f);
        
        // When - simulate gameplay to test new max rate
        for (int i = 0; i < 300; i++) {
            controller.update(1.0f);
        }
        
        // Then - should respect new maximum
        assertTrue(controller.getCurrentSpawnRate() <= originalMaxRate * 1.5f);
    }
    
    @Test
    @DisplayName("Should coordinate spawn timing with EnemySpawner update cycle")
    void shouldCoordinateSpawnTimingWithEnemySpawnerUpdateCycle() {
        // Given
        gameStateManager.setState(GameState.PLAYING);
        controller.resetForNewGame();
        enemySpawner.setSpawningEnabled(true);
        
        // When - update both controller and spawner in sync
        float deltaTime = 0.016f; // ~60 FPS
        
        for (int i = 0; i < 100; i++) {
            controller.update(deltaTime);
            enemySpawner.update(deltaTime);
        }
        
        // Then - spawner should be using dynamic intervals
        assertTrue(enemySpawner.isDynamicSpawnActive());
        
        // Verify spawn interval matches controller's target
        float expectedInterval = controller.getCurrentTargetInterval();
        assertEquals(expectedInterval, enemySpawner.getSpawnInterval(), 0.001f);
    }
    
    @Test
    @DisplayName("Should handle performance reduction affecting spawn rates")
    void shouldHandlePerformanceReductionAffectingSpawnRates() {
        // Given
        gameStateManager.setState(GameState.PLAYING);
        controller.resetForNewGame();
        controller.update(30.0f); // Build up spawn rate
        
        float normalRate = controller.getCurrentSpawnRate();
        
        // When - enable performance reduction
        controller.setPerformanceReduction(true);
        
        // Then - spawn rate should be reduced
        float reducedRate = controller.getCurrentSpawnRate();
        assertTrue(reducedRate < normalRate);
        assertTrue(controller.isPerformanceReductionActive());
        
        // Verify EnemySpawner gets the reduced interval
        float expectedInterval = 1.0f / reducedRate;
        assertEquals(expectedInterval, enemySpawner.getSpawnInterval(), 0.001f);
        
        // When - disable performance reduction
        controller.setPerformanceReduction(false);
        
        // Then - should return to normal rate
        assertEquals(normalRate, controller.getCurrentSpawnRate(), 0.001f);
        assertFalse(controller.isPerformanceReductionActive());
    }
    
    @Test
    @DisplayName("Should maintain spawn rate bounds during integration")
    void shouldMaintainSpawnRateBoundsDuringIntegration() {
        // Given
        gameStateManager.setState(GameState.PLAYING);
        controller.resetForNewGame();
        
        // When - simulate very long gameplay
        for (int i = 0; i < 500; i++) {
            controller.update(1.0f);
            enemySpawner.update(0.016f);
        }
        
        // Then - spawn rate should not exceed maximum
        float finalRate = controller.getCurrentSpawnRate();
        assertTrue(finalRate <= config.getMaxSpawnRate());
        
        // Verify EnemySpawner respects the bounds
        float minInterval = 1.0f / config.getMaxSpawnRate();
        assertTrue(enemySpawner.getSpawnInterval() >= minInterval);
    }
    
    @Test
    @DisplayName("Should handle rapid state transitions gracefully")
    void shouldHandleRapidStateTransitionsGracefully() {
        // Given
        controller.resetForNewGame();
        
        // When - rapid state changes
        gameStateManager.setState(GameState.PLAYING);
        controller.update(5.0f);
        
        gameStateManager.setState(GameState.PAUSED);
        controller.update(2.0f);
        
        gameStateManager.setState(GameState.PLAYING);
        controller.update(3.0f);
        
        gameStateManager.setState(GameState.GAME_OVER);
        controller.update(1.0f);
        
        // Then - should handle transitions without errors
        assertEquals(0.0f, controller.getGameDuration(), 0.001f);
        assertFalse(controller.isActive());
        
        // Verify EnemySpawner state is consistent
        assertTrue(enemySpawner.isDynamicSpawnActive());
        assertSame(controller, enemySpawner.getDynamicSpawnController());
    }
    
    @Test
    @DisplayName("Should synchronize scaling progress with spawn interval updates")
    void shouldSynchronizeScalingProgressWithSpawnIntervalUpdates() {
        // Given
        gameStateManager.setState(GameState.PLAYING);
        controller.resetForNewGame();
        
        // When - update in small increments to test synchronization
        float totalTime = 0.0f;
        float deltaTime = 0.1f;
        
        for (int i = 0; i < 100; i++) {
            controller.update(deltaTime);
            totalTime += deltaTime;
            
            // Verify synchronization at each step
            float expectedRate = controller.getCurrentSpawnRate();
            float expectedInterval = 1.0f / expectedRate;
            
            assertEquals(expectedInterval, enemySpawner.getSpawnInterval(), 0.001f,
                        "Spawn interval should match controller rate at time " + totalTime);
        }
        
        // Verify final state
        assertEquals(10.0f, controller.getGameDuration(), 0.001f);
        assertTrue(controller.getCurrentSpawnRate() > config.getBaseSpawnRate());
    }
    
    @Test
    @DisplayName("Should handle configuration validation during integration")
    void shouldHandleConfigurationValidationDuringIntegration() {
        // Given
        gameStateManager.setState(GameState.PLAYING);
        controller.resetForNewGame();
        
        // When - set invalid configuration values
        config.setBaseSpawnRate(-1.0f); // Invalid negative rate
        config.setMaxSpawnRate(0.0f);   // Invalid zero rate
        
        // Attempt to reload configuration
        boolean reloadSuccess = controller.reloadConfiguration();
        
        // Then - should handle gracefully and use safe defaults
        assertTrue(reloadSuccess, "Configuration reload should succeed with fallback to defaults");
        
        // Verify safe values are used
        assertTrue(controller.getCurrentSpawnRate() > 0.0f);
        assertTrue(enemySpawner.getSpawnInterval() > 0.0f);
        
        // System should continue functioning
        controller.update(1.0f);
        assertTrue(controller.getCurrentSpawnRate() > 0.0f);
    }
}