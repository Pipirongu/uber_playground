package com.plv.uberplayground.enemies;

import com.plv.uberplayground.game.GameStateManager;
import com.plv.uberplayground.game.GameStateManager.GameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DynamicSpawnController class.
 * Tests the central coordination of dynamic enemy spawning system.
 * Note: These tests focus on the controller logic without requiring full EnemySpawner integration.
 */
class DynamicSpawnControllerTest {
    
    private EnhancedSpawnConfig config;
    private GameStateManager gameStateManager;
    private DynamicSpawnController controller;
    
    @BeforeEach
    void setUp() {
        // Create configuration with test values
        config = new EnhancedSpawnConfig();
        config.applyDefaults();
        
        // Create real game state manager
        gameStateManager = new GameStateManager();
        
        // Create controller with null spawner for logic-only testing
        // The controller should handle null spawner gracefully for testing
        controller = new DynamicSpawnController(null, gameStateManager, config);
    }
    
    @Test
    @DisplayName("Should initialize with correct default values")
    void shouldInitializeWithCorrectDefaults() {
        // Then
        assertEquals(0.0f, controller.getGameDuration(), 0.001f);
        assertFalse(controller.isActive());
        assertEquals(config.getBaseSpawnRate(), controller.getCurrentSpawnRate(), 0.001f);
        assertFalse(controller.isPerformanceReductionActive());
    }
    
    @Test
    @DisplayName("Should start new game correctly")
    void shouldStartNewGameCorrectly() {
        // When
        controller.resetForNewGame();
        
        // Then
        assertEquals(0.0f, controller.getGameDuration(), 0.001f);
        assertTrue(controller.isActive());
        assertEquals(config.getBaseSpawnRate(), controller.getCurrentSpawnRate(), 0.001f);
    }
    
    @Test
    @DisplayName("Should update game duration during active gameplay")
    void shouldUpdateGameDurationDuringActiveGameplay() {
        // Given
        gameStateManager.setState(GameState.PLAYING);
        controller.resetForNewGame();
        float deltaTime = 0.016f; // ~60 FPS
        
        // When
        controller.update(deltaTime);
        
        // Then
        assertEquals(deltaTime, controller.getGameDuration(), 0.001f);
    }
    
    @Test
    @DisplayName("Should not update when inactive")
    void shouldNotUpdateWhenInactive() {
        // Given - controller starts inactive, but setting game state to PLAYING will trigger activation
        // So we need to pause it again to test the inactive behavior
        gameStateManager.setState(GameState.PLAYING);
        controller.pauseScaling(); // Explicitly pause to test inactive behavior
        float deltaTime = 0.016f;
        
        // Verify initial state
        assertFalse(controller.isActive(), "Controller should be inactive after pausing");
        assertTrue(gameStateManager.isGameplayActive(), "GameStateManager should report gameplay as active");
        
        // When
        controller.update(deltaTime);
        
        // Then - should not update because controller itself is not active
        assertEquals(0.0f, controller.getGameDuration(), 0.001f);
    }
    
    @Test
    @DisplayName("Should not update when gameplay is not active")
    void shouldNotUpdateWhenGameplayNotActive() {
        // Given
        controller.resetForNewGame();
        gameStateManager.setState(GameState.PAUSED);
        float deltaTime = 0.016f;
        
        // When
        controller.update(deltaTime);
        
        // Then
        assertEquals(0.0f, controller.getGameDuration(), 0.001f);
    }
    
    @Test
    @DisplayName("Should increase spawn rate over time")
    void shouldIncreaseSpawnRateOverTime() {
        // Given
        gameStateManager.setState(GameState.PLAYING);
        controller.resetForNewGame();
        float initialRate = controller.getCurrentSpawnRate();
        
        // When - simulate 30 seconds of gameplay
        for (int i = 0; i < 30; i++) {
            controller.update(1.0f); // 1 second per update
        }
        
        // Then
        float finalRate = controller.getCurrentSpawnRate();
        assertTrue(finalRate > initialRate, 
                  "Spawn rate should increase over time. Initial: " + initialRate + ", Final: " + finalRate);
    }
    
    @Test
    @DisplayName("Should reach target scaling at 60 seconds")
    void shouldReachTargetScalingAt60Seconds() {
        // Given
        gameStateManager.setState(GameState.PLAYING);
        controller.resetForNewGame();
        float baseRate = config.getBaseSpawnRate();
        
        // When - simulate 60 seconds of gameplay
        for (int i = 0; i < 60; i++) {
            controller.update(1.0f);
        }
        
        // Then - should have doubled the spawn rate (100% increase)
        float finalRate = controller.getCurrentSpawnRate();
        float expectedMinimumRate = baseRate * 2.0f; // 100% increase
        assertTrue(finalRate >= expectedMinimumRate, 
                  "Spawn rate should at least double after 60 seconds. Base: " + baseRate + 
                  ", Final: " + finalRate + ", Expected minimum: " + expectedMinimumRate);
    }
    
    @Test
    @DisplayName("Should respect maximum spawn rate bounds")
    void shouldRespectMaximumSpawnRateBounds() {
        // Given
        gameStateManager.setState(GameState.PLAYING);
        controller.resetForNewGame();
        float maxRate = config.getMaxSpawnRate();
        
        // When - simulate very long gameplay (300 seconds)
        for (int i = 0; i < 300; i++) {
            controller.update(1.0f);
        }
        
        // Then
        float finalRate = controller.getCurrentSpawnRate();
        assertTrue(finalRate <= maxRate, 
                  "Spawn rate should not exceed maximum. Final: " + finalRate + ", Max: " + maxRate);
    }
    
    @Test
    @DisplayName("Should handle pause and resume correctly")
    void shouldHandlePauseAndResumeCorrectly() {
        // Given
        gameStateManager.setState(GameState.PLAYING);
        controller.resetForNewGame();
        controller.update(10.0f); // Build up some duration
        float durationBeforePause = controller.getGameDuration();
        
        // When
        controller.pauseScaling();
        controller.update(5.0f); // This should not increase duration
        
        // Then
        assertFalse(controller.isActive());
        assertEquals(durationBeforePause, controller.getGameDuration(), 0.001f);
        
        // When
        controller.resumeScaling();
        
        // Then
        assertTrue(controller.isActive());
    }
    
    @Test
    @DisplayName("Should apply performance reduction when enabled")
    void shouldApplyPerformanceReductionWhenEnabled() {
        // Given
        gameStateManager.setState(GameState.PLAYING);
        controller.resetForNewGame();
        controller.update(30.0f); // Build up spawn rate
        float normalRate = controller.getCurrentSpawnRate();
        
        // When
        controller.setPerformanceReduction(true);
        
        // Then
        float reducedRate = controller.getCurrentSpawnRate();
        float expectedRate = normalRate * config.getPerformanceReductionFactor();
        assertEquals(expectedRate, reducedRate, 0.001f);
        assertTrue(controller.isPerformanceReductionActive());
    }
    
    @Test
    @DisplayName("Should handle game state transitions correctly")
    void shouldHandleGameStateTransitionsCorrectly() {
        // Test PLAYING state
        controller.onStateChanged(GameState.MENU, GameState.PLAYING);
        assertTrue(controller.isActive());
        
        // Test PAUSED state
        controller.onStateChanged(GameState.PLAYING, GameState.PAUSED);
        assertFalse(controller.isActive());
        
        // Test GAME_OVER state
        controller.onStateChanged(GameState.PAUSED, GameState.GAME_OVER);
        assertEquals(0.0f, controller.getGameDuration(), 0.001f);
        
        // Test return to MENU
        controller.onStateChanged(GameState.GAME_OVER, GameState.MENU);
        assertEquals(0.0f, controller.getGameDuration(), 0.001f);
    }
    
    @Test
    @DisplayName("Should calculate progress to target correctly")
    void shouldCalculateProgressToTargetCorrectly() {
        // Given
        gameStateManager.setState(GameState.PLAYING);
        controller.resetForNewGame();
        
        // When - simulate half the target duration
        float halfTarget = config.getTargetDuration() / 2.0f;
        controller.update(halfTarget);
        
        // Then
        assertEquals(0.5f, controller.getProgressToTarget(), 0.01f);
        
        // When - simulate full target duration
        controller.update(halfTarget);
        
        // Then
        assertEquals(1.0f, controller.getProgressToTarget(), 0.01f);
        
        // When - simulate beyond target duration
        controller.update(30.0f);
        
        // Then - should cap at 1.0
        assertEquals(1.0f, controller.getProgressToTarget(), 0.01f);
    }
    
    @Test
    @DisplayName("Should detect maximum spawn rate correctly")
    void shouldDetectMaximumSpawnRateCorrectly() {
        // Given
        gameStateManager.setState(GameState.PLAYING);
        controller.resetForNewGame();
        
        // Initially should not be at max
        assertFalse(controller.isAtMaximumSpawnRate());
        
        // When - simulate very long gameplay to reach max rate
        for (int i = 0; i < 300; i++) {
            controller.update(1.0f);
        }
        
        // Then
        assertTrue(controller.isAtMaximumSpawnRate());
    }
    
    @Test
    @DisplayName("Should provide access to scaling calculator")
    void shouldProvideAccessToScalingCalculator() {
        // Then
        assertNotNull(controller.getScalingCalculator());
        assertSame(config, controller.getScalingCalculator().getConfig());
    }
    
    @Test
    @DisplayName("Should provide access to configuration")
    void shouldProvideAccessToConfiguration() {
        // Then
        assertNotNull(controller.getConfig());
        assertSame(config, controller.getConfig());
    }
    
    @Test
    @DisplayName("Should calculate scaling factor correctly")
    void shouldCalculateScalingFactorCorrectly() {
        // Given
        gameStateManager.setState(GameState.PLAYING);
        controller.resetForNewGame();
        
        // Initially should be 0
        assertEquals(0.0f, controller.getCurrentScalingFactor(), 0.001f);
        
        // When - simulate half target duration
        controller.update(config.getTargetDuration() / 2.0f);
        
        // Then - should be around 0.5 for linear scaling
        assertEquals(0.5f, controller.getCurrentScalingFactor(), 0.01f);
        
        // When - simulate full target duration
        controller.update(config.getTargetDuration() / 2.0f);
        
        // Then - should be 1.0
        assertEquals(1.0f, controller.getCurrentScalingFactor(), 0.01f);
    }
    
    @Test
    @DisplayName("Should calculate target interval correctly")
    void shouldCalculateTargetIntervalCorrectly() {
        // Given
        controller.resetForNewGame();
        
        // Then - initial interval should be 1/baseRate
        float expectedInterval = 1.0f / config.getBaseSpawnRate();
        assertEquals(expectedInterval, controller.getCurrentTargetInterval(), 0.001f);
    }
}