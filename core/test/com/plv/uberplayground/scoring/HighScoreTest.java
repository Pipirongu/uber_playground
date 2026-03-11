package com.plv.uberplayground.scoring;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for HighScore data structure.
 */
class HighScoreTest {
    
    @Test
    void testHighScoreCreation() {
        HighScore score = new HighScore(1000);
        
        assertEquals(1000, score.getScore());
        assertTrue(score.getTimestamp() > 0);
        assertTrue(score.getTimestamp() <= System.currentTimeMillis());
    }
    
    @Test
    void testHighScoreWithTimestamp() {
        long timestamp = 1234567890L;
        HighScore score = new HighScore(500, timestamp);
        
        assertEquals(500, score.getScore());
        assertEquals(timestamp, score.getTimestamp());
    }
    
    @Test
    void testCompareTo() {
        HighScore score1 = new HighScore(1000);
        HighScore score2 = new HighScore(500);
        HighScore score3 = new HighScore(1000);
        
        // Higher score should come first (negative result)
        assertTrue(score1.compareTo(score2) < 0);
        
        // Lower score should come after (positive result)
        assertTrue(score2.compareTo(score1) > 0);
        
        // Equal scores should return 0
        assertEquals(0, score1.compareTo(score3));
    }
    
    @Test
    void testEquals() {
        long timestamp = 1234567890L;
        HighScore score1 = new HighScore(1000, timestamp);
        HighScore score2 = new HighScore(1000, timestamp);
        HighScore score3 = new HighScore(500, timestamp);
        HighScore score4 = new HighScore(1000, timestamp + 1);
        
        assertEquals(score1, score2);
        assertNotEquals(score1, score3);
        assertNotEquals(score1, score4);
        assertNotEquals(score1, null);
        assertNotEquals(score1, "not a HighScore");
    }
    
    @Test
    void testHashCode() {
        long timestamp = 1234567890L;
        HighScore score1 = new HighScore(1000, timestamp);
        HighScore score2 = new HighScore(1000, timestamp);
        
        assertEquals(score1.hashCode(), score2.hashCode());
    }
    
    @Test
    void testToString() {
        HighScore score = new HighScore(1000, 1234567890L);
        String result = score.toString();
        
        assertTrue(result.contains("1000"));
        assertTrue(result.contains("1234567890"));
        assertTrue(result.contains("HighScore"));
    }
}