package com.plv.uberplayground.screens;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import com.plv.uberplayground.game.GameStateManager;
import com.plv.uberplayground.game.GameStateManager.GameState;

/**
 * Integration tests for GameScreen and GameStateManager integration.
 * Tests proper state transitions and lifecycle management.
 */
public class GameScreenStateIntegrationTest {
    
    private GameStateManager gameStateManager;
    
    @BeforeEach
    void setUp() {
        gameStateManager = new GameStateManager();
    }
    
    @Test
    @DisplayName("GameStateManager should be initialized in PLAYING state")
    void testGameStateManagerInitialization() {
        // This test verifies the expected behavior based on the implementation
        // In a real GameScreen, the GameStateManager should start in PLAYING state
        
        gameStateManager.startNewGame();
        
        assertEquals(GameState.PLAYING, gameStateManager.getCurrentState());
        assertTrue(gameStateManager.isGameplayActive());
        assertFalse(gameStateManager.isPaused());
        assertFalse(gameStateManager.isGameOver());
    }
    
    @Test
    @DisplayName("GameStateManager should handle pause/resume transitions")
    void testPauseResumeTransitions() {
        gameStateManager.startNewGame();
        
        // Test pause
        gameStateManager.pauseGame();
        assertEquals(GameState.PAUSED, gameStateManager.getCurrentState());
        assertTrue(gameStateManager.isPaused());
        assertFalse(gameStateManager.isGameplayActive());
        
        // Test resume
        gameStateManager.resumeGame();
        assertEquals(GameState.PLAYING, gameStateManager.getCurrentState());
        assertTrue(gameStateManager.isGameplayActive());
        assertFalse(gameStateManager.isPaused());
    }
    
    @Test
    @DisplayName("GameStateManager should handle game over transition")
    void testGameOverTransition() {
        gameStateManager.startNewGame();
        
        // Test game over
        gameStateManager.endGame();
        assertEquals(GameState.GAME_OVER, gameStateManager.getCurrentState());
        assertTrue(gameStateManager.isGameOver());
        assertFalse(gameStateManager.isGameplayActive());
        assertFalse(gameStateManager.isPaused());
    }
    
    @Test
    @DisplayName("GameStateManager should track game duration during active gameplay")
    void testGameDurationTracking() {
        gameStateManager.startNewGame();
        
        // Simulate frame updates
        float deltaTime = 0.016f; // ~60 FPS
        
        // Update for 1 second of gameplay
        for (int i = 0; i < 60; i++) {
            gameStateManager.update(deltaTime);
        }
        
        GameStateManager.GameSessionData sessionData = gameStateManager.getSessionData();
        
        // Should have tracked approximately 1 second of active duration
        assertTrue(sessionData.getActiveDuration() > 0.9f);
        assertTrue(sessionData.getActiveDuration() < 1.1f);
        assertTrue(sessionData.isScalingActive());
    }
    
    @Test
    @DisplayName("GameStateManager should pause duration tracking when paused")
    void testPausedDurationTracking() {
        gameStateManager.startNewGame();
        
        float deltaTime = 0.016f;
        
        // Update for 0.5 seconds
        for (int i = 0; i < 30; i++) {
            gameStateManager.update(deltaTime);
        }
        
        // Pause the game
        gameStateManager.pauseGame();
        float activeDurationBeforePause = gameStateManager.getSessionData().getActiveDuration();
        
        // Update while paused for 0.5 seconds
        for (int i = 0; i < 30; i++) {
            gameStateManager.update(deltaTime);
        }
        
        GameStateManager.GameSessionData sessionData = gameStateManager.getSessionData();
        
        // Active duration should not have increased during pause
        assertEquals(activeDurationBeforePause, sessionData.getActiveDuration(), 0.001f);
        
        // Total duration should have increased
        assertTrue(sessionData.getTotalGameDuration() > activeDurationBeforePause);
        
        // Paused duration should be tracked
        assertTrue(sessionData.getPausedDuration() > 0.4f);
        assertFalse(sessionData.isScalingActive());
    }
    
    @Test
    @DisplayName("GameStateManager should reset session data for new game")
    void testNewGameReset() {
        gameStateManager.startNewGame();
        
        float deltaTime = 0.016f;
        
        // Update for some time
        for (int i = 0; i < 60; i++) {
            gameStateManager.update(deltaTime);
        }
        
        // Verify we have some duration tracked
        assertTrue(gameStateManager.getSessionData().getActiveDuration() > 0);
        
        // Start new game
        gameStateManager.startNewGame();
        
        // Session data should be reset
        GameStateManager.GameSessionData sessionData = gameStateManager.getSessionData();
        assertEquals(0.0f, sessionData.getActiveDuration());
        assertEquals(0.0f, sessionData.getTotalGameDuration());
        assertEquals(0.0f, sessionData.getPausedDuration());
        assertEquals(GameState.PLAYING, gameStateManager.getCurrentState());
    }
    
    @Test
    @DisplayName("GameStateManager should handle state listener notifications")
    void testStateListenerNotifications() {
        // Create a simple listener implementation to track state changes
        final GameState[] lastOldState = {null};
        final GameState[] lastNewState = {null};
        final int[] stateChangeCount = {0};
        
        GameStateManager.GameStateListener listener = new GameStateManager.GameStateListener() {
            @Override
            public void onStateChanged(GameState oldState, GameState newState) {
                lastOldState[0] = oldState;
                lastNewState[0] = newState;
                stateChangeCount[0]++;
            }
            
            @Override
            public void onDurationUpdated(float totalDuration, float activeDuration) {
                // Not testing duration updates in this test
            }
        };
        
        gameStateManager.addListener(listener);
        
        // Test state transitions
        gameStateManager.startNewGame();
        assertEquals(GameState.MENU, lastOldState[0]);
        assertEquals(GameState.PLAYING, lastNewState[0]);
        assertEquals(1, stateChangeCount[0]);
        
        gameStateManager.pauseGame();
        assertEquals(GameState.PLAYING, lastOldState[0]);
        assertEquals(GameState.PAUSED, lastNewState[0]);
        assertEquals(2, stateChangeCount[0]);
        
        gameStateManager.resumeGame();
        assertEquals(GameState.PAUSED, lastOldState[0]);
        assertEquals(GameState.PLAYING, lastNewState[0]);
        assertEquals(3, stateChangeCount[0]);
        
        gameStateManager.endGame();
        assertEquals(GameState.PLAYING, lastOldState[0]);
        assertEquals(GameState.GAME_OVER, lastNewState[0]);
        assertEquals(4, stateChangeCount[0]);
    }
}