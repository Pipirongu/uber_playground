package com.plv.uberplayground.integration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import com.plv.uberplayground.enemies.*;
import com.plv.uberplayground.game.GameStateManager;

/**
 * Integration tests for the complete dynamic enemy spawning system.
 * Tests the wiring and interaction between all components.
 */
public class DynamicSpawningSystemIntegrationTest {
    
    private GameStateManager gameStateManager;
    private HealthSystem healthSystem;
    private EnemyManager enemyManager;
    private DynamicSpawnController dynamicSpawnController;
    private EnhancedSpawnConfig spawnConfig;
    
    @BeforeEach
    void setUp() {
        // Initialize real components
        gameStateManager = new GameStateManager();
        healthSystem = new HealthSystem();
        enemyManager = new EnemyManager(healthSystem);
        
        // Create enhanced spawn configuration with defaults
        spawnConfig = new EnhancedSpawnConfig();
        spawnConfig.applyDefaults();
        
        // Initialize dynamic spawn controller (without enemy spawner for logic testing)
        dynamicSpawnController = new DynamicSpawnController(null, gameStateManager, spawnConfig);
    }
    
    @Test
    @DisplayName("Dynamic spawn controller initialization and basic functionality")
    void testDynamicSpawnControllerInitialization() {
        // Verify controller is properly initialized
        assertNotNull(dynamicSpawnController);
        assertNotNull(dynamicSpawnController.getConfig());
        assertNotNull(dynamicSpawnController.getScalingCalculator());
        
        // Verify initial state
        assertFalse(dynamicSpawnController.isActive());
        assertEquals(0.0f, dynamicSpawnController.getGameDuration(), 0.001f);
        
        // Start a new game
        gameStateManager.startNewGame();
        assertTrue(gameStateManager.isGameplayActive());
        assertTrue(dynamicSpawnController.isActive());
    }
    
    @Test
    @DisplayName("Game state transitions affect dynamic spawning correctly")
    void testGameStateTransitions() {
        // Start game and run for some time
        gameStateManager.startNewGame();
        dynamicSpawnController.update(1.0f);
        float activeSpawnRate = dynamicSpawnController.getCurrentSpawnRate();
        
        // Pause game
        gameStateManager.pauseGame();
        assertFalse(gameStateManager.isGameplayActive());
        assertFalse(dynamicSpawnController.isActive());
        
        // Update during pause - spawn rate should not change significantly
        dynamicSpawnController.update(1.0f);
        float pausedSpawnRate = dynamicSpawnController.getCurrentSpawnRate();
        assertEquals(activeSpawnRate, pausedSpawnRate, 0.01f); // Allow small tolerance
        
        // Resume game
        gameStateManager.resumeGame();
        assertTrue(gameStateManager.isGameplayActive());
        assertTrue(dynamicSpawnController.isActive());
        
        // Continue updating - spawn rate should continue to increase
        dynamicSpawnController.update(1.0f);
        assertTrue(dynamicSpawnController.getCurrentSpawnRate() >= activeSpawnRate);
    }
    
    @Test
    @DisplayName("Performance optimization integration works correctly")
    void testPerformanceOptimizationIntegration() {
        // Start game
        gameStateManager.startNewGame();
        
        // Simulate poor performance by enabling performance reduction directly
        dynamicSpawnController.setPerformanceReduction(true);
        
        // Update dynamic spawn controller
        dynamicSpawnController.update(1.0f);
        
        // Performance reduction should be active
        assertTrue(dynamicSpawnController.isPerformanceReductionActive());
        
        // Spawn rate should be reduced
        float reducedSpawnRate = dynamicSpawnController.getCurrentSpawnRate();
        float baseSpawnRate = dynamicSpawnController.getConfig().getBaseSpawnRate();
        assertTrue(reducedSpawnRate < baseSpawnRate * 2.0f, 
            "Spawn rate should be reduced due to poor performance");
    }
    
    @Test
    @DisplayName("Configuration changes affect spawn rate calculation")
    void testConfigurationChanges() {
        // Start game
        gameStateManager.startNewGame();
        
        // Get initial configuration values
        EnhancedSpawnConfig config = dynamicSpawnController.getConfig();
        float originalBaseRate = config.getBaseSpawnRate();
        
        // Update system with original configuration
        dynamicSpawnController.update(0.1f);
        float originalSpawnRate = dynamicSpawnController.getCurrentSpawnRate();
        
        // Simulate configuration change (direct modification)
        config.setBaseSpawnRate(originalBaseRate * 2.0f);
        
        // Reset and update system with new configuration
        dynamicSpawnController.resetForNewGame();
        gameStateManager.startNewGame();
        dynamicSpawnController.update(0.1f);
        
        // Verify new configuration affects spawn rate calculation
        float newSpawnRate = dynamicSpawnController.getCurrentSpawnRate();
        assertTrue(newSpawnRate > originalSpawnRate, 
            "Spawn rate should be higher with increased base rate");
        
        // Verify configuration value was actually changed
        assertEquals(originalBaseRate * 2.0f, config.getBaseSpawnRate(), 0.001f);
    }
    
    @Test
    @DisplayName("System handles maximum enemy limit correctly")
    void testMaximumEnemyLimitHandling() {
        // Verify maximum enemy limit is set correctly
        EnhancedSpawnConfig config = dynamicSpawnController.getConfig();
        assertEquals(50, config.getMaxEnemies());
        
        // Verify enemy manager respects the limit
        assertEquals(50, enemyManager.getMaxEnemyCount());
    }
    
    @Test
    @DisplayName("Scaling calculation integration produces expected results")
    void testScalingCalculationIntegration() {
        // Start game
        gameStateManager.startNewGame();
        
        // Test scaling at specific time points
        EnhancedSpawnConfig config = dynamicSpawnController.getConfig();
        float baseRate = config.getBaseSpawnRate();
        
        // At game start (0 seconds)
        dynamicSpawnController.update(0.0f);
        assertEquals(baseRate, dynamicSpawnController.getCurrentSpawnRate(), 0.001f);
        
        // At 30 seconds (halfway to target)
        dynamicSpawnController.resetForNewGame();
        gameStateManager.startNewGame();
        dynamicSpawnController.update(30.0f);
        float halfwayRate = dynamicSpawnController.getCurrentSpawnRate();
        assertTrue(halfwayRate > baseRate && halfwayRate < baseRate * 2.0f);
        
        // At 60 seconds (target duration)
        dynamicSpawnController.resetForNewGame();
        gameStateManager.startNewGame();
        dynamicSpawnController.update(60.0f);
        float targetRate = dynamicSpawnController.getCurrentSpawnRate();
        assertTrue(targetRate >= baseRate * 2.0f, 
            "Spawn rate should be at least doubled at target duration");
    }
    
    @Test
    @DisplayName("Configuration validation and error handling")
    void testConfigurationValidation() {
        // Test that configuration has valid defaults
        EnhancedSpawnConfig config = dynamicSpawnController.getConfig();
        assertTrue(config.getBaseSpawnRate() > 0);
        assertTrue(config.getMaxSpawnRate() >= config.getBaseSpawnRate());
        assertTrue(config.getMaxEnemies() > 0);
        assertTrue(config.getTargetDuration() > 0);
        assertTrue(config.getScalingFactor() >= 0);
    }
    
    @Test
    @DisplayName("Scaling factor calculation correctness")
    void testScalingFactorCalculation() {
        gameStateManager.startNewGame();
        
        // Test that scaling factor increases monotonically
        float factor1 = dynamicSpawnController.getCurrentScalingFactor();
        dynamicSpawnController.update(10.0f);
        float factor2 = dynamicSpawnController.getCurrentScalingFactor();
        dynamicSpawnController.update(10.0f);
        float factor3 = dynamicSpawnController.getCurrentScalingFactor();
        
        assertTrue(factor2 >= factor1, "Scaling factor should be monotonic");
        assertTrue(factor3 >= factor2, "Scaling factor should be monotonic");
    }
}