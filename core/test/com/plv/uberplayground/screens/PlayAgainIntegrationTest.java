package com.plv.uberplayground.screens;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import com.plv.uberplayground.scoring.ScoreTracker;
import com.plv.uberplayground.scoring.ScoreConstants;

/**
 * Integration test for Play Again functionality to ensure proper score reset.
 * 
 * This test verifies that when starting a new game (simulating Play Again button),
 * the score is properly reset to zero as required by Requirements 4.2, 4.3, 4.4.
 */
class PlayAgainIntegrationTest {
    
    private ScoreTracker scoreTracker;
    
    @BeforeEach
    void setUp() {
        // Create a fresh ScoreTracker to simulate new game creation
        scoreTracker = new ScoreTracker();
    }
    
    /**
     * Test that verifies the Play Again functionality properly resets score.
     * 
     * This test simulates:
     * 1. A game session with some score accumulated
     * 2. Game over and transition to HighScoreScreen
     * 3. Play Again button pressed, creating new GameScreen
     * 4. Verification that score is reset to zero
     * 
     * Validates: Requirements 4.2, 4.3, 4.4
     */
    @Test
    void testPlayAgainResetsScore() {
        // Simulate a game session with accumulated score
        scoreTracker.incrementScore(); // Score = 1
        scoreTracker.incrementScore(); // Score = 2
        scoreTracker.incrementScore(); // Score = 3
        
        assertEquals(3 * ScoreConstants.POINTS_PER_ENEMY_KILL, scoreTracker.getCurrentScore());
        
        // Simulate Play Again button functionality
        // This mimics what happens in GameScreen constructor when called from HighScoreScreen
        scoreTracker.resetScore();
        
        // Verify score is reset to initial value (Requirements 4.3)
        assertEquals(ScoreConstants.INITIAL_SCORE, scoreTracker.getCurrentScore());
        assertEquals(0, scoreTracker.getCurrentScore());
    }
    
    /**
     * Test that verifies new ScoreTracker instances start with zero score.
     * 
     * This test simulates the creation of a new GameScreen from HighScoreScreen,
     * which creates a new ScoreTracker instance and explicitly resets it.
     * 
     * Validates: Requirements 4.3
     */
    @Test
    void testNewGameSessionStartsWithZeroScore() {
        // Simulate creating a new ScoreTracker (as done in GameScreen constructor)
        ScoreTracker newGameScoreTracker = new ScoreTracker();
        
        // Verify it starts with initial score
        assertEquals(ScoreConstants.INITIAL_SCORE, newGameScoreTracker.getCurrentScore());
        
        // Simulate the explicit reset call added to GameScreen constructor
        newGameScoreTracker.resetScore();
        
        // Verify score remains at zero after explicit reset
        assertEquals(0, newGameScoreTracker.getCurrentScore());
    }
    
    /**
     * Test that verifies score reset doesn't affect listener functionality.
     * 
     * This ensures that after a Play Again reset, the score tracking system
     * continues to work correctly for the new game session.
     * 
     * Validates: Requirements 1.1, 4.3
     */
    @Test
    void testScoreTrackingWorksAfterReset() {
        // Simulate accumulated score
        scoreTracker.incrementScore();
        scoreTracker.incrementScore();
        assertEquals(2, scoreTracker.getCurrentScore());
        
        // Reset score (Play Again functionality)
        scoreTracker.resetScore();
        assertEquals(0, scoreTracker.getCurrentScore());
        
        // Verify score tracking still works after reset
        scoreTracker.incrementScore();
        assertEquals(1, scoreTracker.getCurrentScore());
        
        scoreTracker.incrementScore();
        assertEquals(2, scoreTracker.getCurrentScore());
    }
}