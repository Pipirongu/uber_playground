package com.plv.uberplayground.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GameStateManager class
 */
public class GameStateManagerTest {
    
    private GameStateManager gameStateManager;
    private TestGameStateListener listener;
    
    @BeforeEach
    public void setUp() {
        gameStateManager = new GameStateManager();
        listener = new TestGameStateListener();
    }
    
    @Test
    public void testInitialState() {
        assertEquals(GameStateManager.GameState.MENU, gameStateManager.getCurrentState());
        assertEquals(GameStateManager.GameState.MENU, gameStateManager.getPreviousState());
        assertFalse(gameStateManager.isGameplayActive());
        assertFalse(gameStateManager.isPaused());
        assertFalse(gameStateManager.isGameOver());
    }
    
    @Test
    public void testStateTransitions() {
        // Test MENU -> PLAYING
        gameStateManager.setState(GameStateManager.GameState.PLAYING);
        assertEquals(GameStateManager.GameState.PLAYING, gameStateManager.getCurrentState());
        assertEquals(GameStateManager.GameState.MENU, gameStateManager.getPreviousState());
        assertTrue(gameStateManager.isGameplayActive());
        
        // Test PLAYING -> PAUSED
        gameStateManager.setState(GameStateManager.GameState.PAUSED);
        assertEquals(GameStateManager.GameState.PAUSED, gameStateManager.getCurrentState());
        assertEquals(GameStateManager.GameState.PLAYING, gameStateManager.getPreviousState());
        assertTrue(gameStateManager.isPaused());
        
        // Test PAUSED -> PLAYING
        gameStateManager.setState(GameStateManager.GameState.PLAYING);
        assertEquals(GameStateManager.GameState.PLAYING, gameStateManager.getCurrentState());
        assertEquals(GameStateManager.GameState.PAUSED, gameStateManager.getPreviousState());
        
        // Test PLAYING -> GAME_OVER
        gameStateManager.setState(GameStateManager.GameState.GAME_OVER);
        assertEquals(GameStateManager.GameState.GAME_OVER, gameStateManager.getCurrentState());
        assertTrue(gameStateManager.isGameOver());
    }
    
    @Test
    public void testConvenienceMethods() {
        // Test startNewGame
        gameStateManager.startNewGame();
        assertEquals(GameStateManager.GameState.PLAYING, gameStateManager.getCurrentState());
        assertTrue(gameStateManager.isGameplayActive());
        
        // Test pauseGame
        gameStateManager.pauseGame();
        assertEquals(GameStateManager.GameState.PAUSED, gameStateManager.getCurrentState());
        assertTrue(gameStateManager.isPaused());
        
        // Test resumeGame
        gameStateManager.resumeGame();
        assertEquals(GameStateManager.GameState.PLAYING, gameStateManager.getCurrentState());
        assertTrue(gameStateManager.isGameplayActive());
        
        // Test endGame
        gameStateManager.endGame();
        assertEquals(GameStateManager.GameState.GAME_OVER, gameStateManager.getCurrentState());
        assertTrue(gameStateManager.isGameOver());
        
        // Test returnToMenu
        gameStateManager.returnToMenu();
        assertEquals(GameStateManager.GameState.MENU, gameStateManager.getCurrentState());
    }
    
    @Test
    public void testInvalidStateTransition() {
        assertThrows(IllegalArgumentException.class, () -> {
            gameStateManager.setState(null);
        });
    }
    
    @Test
    public void testListenerNotifications() {
        gameStateManager.addListener(listener);
        
        // Test state change notification
        gameStateManager.setState(GameStateManager.GameState.PLAYING);
        assertEquals(1, listener.stateChangeCount);
        assertEquals(GameStateManager.GameState.MENU, listener.lastOldState);
        assertEquals(GameStateManager.GameState.PLAYING, listener.lastNewState);
        
        // Test duration update notification
        gameStateManager.update(1.0f);
        assertEquals(1, listener.durationUpdateCount);
        assertEquals(1.0f, listener.lastTotalDuration, 0.001f);
        assertEquals(1.0f, listener.lastActiveDuration, 0.001f);
    }
    
    @Test
    public void testDurationTracking() {
        gameStateManager.setState(GameStateManager.GameState.PLAYING);
        
        // Update for 2 seconds
        gameStateManager.update(1.0f);
        gameStateManager.update(1.0f);
        
        GameStateManager.GameSessionData sessionData = gameStateManager.getSessionData();
        assertEquals(2.0f, sessionData.getTotalGameDuration(), 0.001f);
        assertEquals(2.0f, sessionData.getActiveDuration(), 0.001f);
        assertEquals(0.0f, sessionData.getPausedDuration(), 0.001f);
        assertTrue(sessionData.isScalingActive());
    }
    
    @Test
    public void testPauseResumeDurationTracking() {
        gameStateManager.setState(GameStateManager.GameState.PLAYING);
        
        // Play for 1 second
        gameStateManager.update(1.0f);
        
        // Pause for 2 seconds
        gameStateManager.setState(GameStateManager.GameState.PAUSED);
        gameStateManager.update(1.0f);
        gameStateManager.update(1.0f);
        
        // Resume and play for 1 more second
        gameStateManager.setState(GameStateManager.GameState.PLAYING);
        gameStateManager.update(1.0f);
        
        GameStateManager.GameSessionData sessionData = gameStateManager.getSessionData();
        assertEquals(4.0f, sessionData.getTotalGameDuration(), 0.001f);
        assertEquals(2.0f, sessionData.getActiveDuration(), 0.001f);
        assertEquals(2.0f, sessionData.getPausedDuration(), 0.001f);
    }
    
    @Test
    public void testSessionReset() {
        gameStateManager.setState(GameStateManager.GameState.PLAYING);
        gameStateManager.update(2.0f);
        
        // Verify data exists
        GameStateManager.GameSessionData sessionData = gameStateManager.getSessionData();
        assertTrue(sessionData.getTotalGameDuration() > 0);
        
        // Reset and verify
        gameStateManager.resetSession();
        sessionData = gameStateManager.getSessionData();
        assertEquals(0.0f, sessionData.getTotalGameDuration(), 0.001f);
        assertEquals(0.0f, sessionData.getActiveDuration(), 0.001f);
        assertEquals(0.0f, sessionData.getPausedDuration(), 0.001f);
        assertFalse(sessionData.isScalingActive());
    }
    
    @Test
    public void testNewGameResetsSession() {
        // Play a game and accumulate some duration
        gameStateManager.setState(GameStateManager.GameState.PLAYING);
        gameStateManager.update(3.0f);
        
        // End game
        gameStateManager.setState(GameStateManager.GameState.GAME_OVER);
        
        // Start new game should reset session
        gameStateManager.startNewGame();
        GameStateManager.GameSessionData sessionData = gameStateManager.getSessionData();
        assertEquals(0.0f, sessionData.getTotalGameDuration(), 0.001f);
        assertEquals(0.0f, sessionData.getActiveDuration(), 0.001f);
    }
    
    @Test
    public void testListenerManagement() {
        TestGameStateListener listener2 = new TestGameStateListener();
        
        // Add listeners
        gameStateManager.addListener(listener);
        gameStateManager.addListener(listener2);
        
        // Test state change notifies both
        gameStateManager.setState(GameStateManager.GameState.PLAYING);
        assertEquals(1, listener.stateChangeCount);
        assertEquals(1, listener2.stateChangeCount);
        
        // Remove one listener
        gameStateManager.removeListener(listener);
        gameStateManager.setState(GameStateManager.GameState.PAUSED);
        assertEquals(1, listener.stateChangeCount); // Should not increase
        assertEquals(2, listener2.stateChangeCount); // Should increase
        
        // Clear all listeners
        gameStateManager.clearListeners();
        gameStateManager.setState(GameStateManager.GameState.PLAYING);
        assertEquals(2, listener2.stateChangeCount); // Should not increase
    }
    
    @Test
    public void testDuplicateListenerNotAdded() {
        gameStateManager.addListener(listener);
        gameStateManager.addListener(listener); // Add same listener again
        
        gameStateManager.setState(GameStateManager.GameState.PLAYING);
        assertEquals(1, listener.stateChangeCount); // Should only be called once
    }
    
    @Test
    public void testNullListenerHandling() {
        // Should not throw exception
        gameStateManager.addListener(null);
        gameStateManager.removeListener(null);
        
        // State changes should still work
        gameStateManager.setState(GameStateManager.GameState.PLAYING);
        assertEquals(GameStateManager.GameState.PLAYING, gameStateManager.getCurrentState());
    }
    
    @Test
    public void testSameStateTransition() {
        gameStateManager.addListener(listener);
        
        // Set to same state should not trigger notification
        gameStateManager.setState(GameStateManager.GameState.MENU);
        assertEquals(0, listener.stateChangeCount);
    }
    
    @Test
    public void testSessionDataImmutability() {
        gameStateManager.setState(GameStateManager.GameState.PLAYING);
        gameStateManager.update(1.0f);
        
        GameStateManager.GameSessionData sessionData1 = gameStateManager.getSessionData();
        GameStateManager.GameSessionData sessionData2 = gameStateManager.getSessionData();
        
        // Should be different instances
        assertNotSame(sessionData1, sessionData2);
        
        // But with same values
        assertEquals(sessionData1.getTotalGameDuration(), sessionData2.getTotalGameDuration(), 0.001f);
    }
    
    /**
     * Test implementation of GameStateListener for testing purposes
     */
    private static class TestGameStateListener implements GameStateManager.GameStateListener {
        public int stateChangeCount = 0;
        public int durationUpdateCount = 0;
        public GameStateManager.GameState lastOldState;
        public GameStateManager.GameState lastNewState;
        public float lastTotalDuration;
        public float lastActiveDuration;
        
        @Override
        public void onStateChanged(GameStateManager.GameState oldState, GameStateManager.GameState newState) {
            stateChangeCount++;
            lastOldState = oldState;
            lastNewState = newState;
        }
        
        @Override
        public void onDurationUpdated(float totalDuration, float activeDuration) {
            durationUpdateCount++;
            lastTotalDuration = totalDuration;
            lastActiveDuration = activeDuration;
        }
    }
}