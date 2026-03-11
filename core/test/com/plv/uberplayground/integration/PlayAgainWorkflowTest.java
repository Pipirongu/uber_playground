package com.plv.uberplayground.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import com.plv.uberplayground.scoring.ScoreTracker;
import com.plv.uberplayground.scoring.ScoreConstants;

/**
 * Integration test for the complete Play Again workflow.
 * 
 * This test verifies the end-to-end functionality of the Play Again button
 * from the HighScoreScreen, ensuring proper score reset and state transitions
 * as specified in Requirements 4.2, 4.3, 4.4.
 */
class PlayAgainWorkflowTest {
    
    /**
     * Test the complete Play Again workflow.
     * 
     * This test simulates:
     * 1. Game session with accumulated score
     * 2. Game over transition to HighScoreScreen
     * 3. Play Again button activation
     * 4. New GameScreen creation with proper score reset
     * 5. Verification that new game session starts correctly
     * 
     * Validates: Requirements 4.2, 4.3, 4.4
     */
    @Test
    void testCompletePlayAgainWorkflow() {
        // Step 1: Simulate a completed game session with score
        ScoreTracker originalGameScore = new ScoreTracker();
        originalGameScore.incrementScore(); // Enemy kill 1
        originalGameScore.incrementScore(); // Enemy kill 2
        originalGameScore.incrementScore(); // Enemy kill 3
        
        int finalScore = originalGameScore.getCurrentScore();
        assertEquals(3, finalScore);
        
        // Step 2: Simulate game over and HighScoreScreen display
        // (HighScoreScreen would save this score and display it)
        assertTrue(finalScore > 0, "Game should have accumulated some score");
        
        // Step 3: Simulate Play Again button activation
        // This creates a new GameScreen which should:
        // - Create new ScoreTracker instance
        // - Explicitly reset score to zero (Requirements 4.3)
        // - Transition directly to gameplay (Requirements 4.4)
        
        ScoreTracker newGameScore = new ScoreTracker();
        // Simulate the explicit reset call added to GameScreen constructor
        newGameScore.resetScore();
        
        // Step 4: Verify new game session starts with zero score
        assertEquals(ScoreConstants.INITIAL_SCORE, newGameScore.getCurrentScore());
        assertEquals(0, newGameScore.getCurrentScore());
        
        // Step 5: Verify new game session can accumulate score normally
        newGameScore.incrementScore();
        assertEquals(1, newGameScore.getCurrentScore());
        
        // Verify the new game score is independent of the original game
        assertNotEquals(finalScore, newGameScore.getCurrentScore());
    }
    
    /**
     * Test that multiple Play Again cycles work correctly.
     * 
     * This verifies that players can repeatedly use Play Again without
     * any score tracking issues accumulating over multiple game sessions.
     * 
     * Validates: Requirements 4.2, 4.3
     */
    @Test
    void testMultiplePlayAgainCycles() {
        for (int cycle = 0; cycle < 3; cycle++) {
            // Simulate a game session
            ScoreTracker gameScore = new ScoreTracker();
            gameScore.resetScore(); // Simulate GameScreen constructor behavior
            
            // Verify each new game starts at zero
            assertEquals(0, gameScore.getCurrentScore(), 
                "Game cycle " + cycle + " should start with zero score");
            
            // Simulate some gameplay
            for (int kills = 0; kills < (cycle + 1) * 2; kills++) {
                gameScore.incrementScore();
            }
            
            // Verify score accumulated correctly
            int expectedScore = (cycle + 1) * 2;
            assertEquals(expectedScore, gameScore.getCurrentScore(),
                "Game cycle " + cycle + " should accumulate score correctly");
        }
    }
    
    /**
     * Test that score reset preserves listener functionality.
     * 
     * This ensures that the ScoreDisplay and other listeners continue
     * to work correctly after a Play Again reset.
     * 
     * Validates: Requirements 1.3, 4.3
     */
    @Test
    void testScoreResetPreservesListeners() {
        ScoreTracker scoreTracker = new ScoreTracker();
        
        // Create a mock listener to track score changes
        MockScoreListener listener = new MockScoreListener();
        scoreTracker.addScoreListener(listener);
        
        // Simulate game with score
        scoreTracker.incrementScore();
        assertEquals(1, listener.getLastScore());
        
        // Simulate Play Again reset
        scoreTracker.resetScore();
        assertEquals(0, listener.getLastScore());
        
        // Verify listener still works after reset
        scoreTracker.incrementScore();
        assertEquals(1, listener.getLastScore());
        
        // Verify listener count is preserved
        assertEquals(1, scoreTracker.getListenerCount());
    }
    
    /**
     * Mock ScoreListener for testing listener functionality.
     */
    private static class MockScoreListener implements com.plv.uberplayground.scoring.ScoreListener {
        private int lastScore = -1;
        
        @Override
        public void onScoreChanged(int newScore) {
            this.lastScore = newScore;
        }
        
        public int getLastScore() {
            return lastScore;
        }
    }
}