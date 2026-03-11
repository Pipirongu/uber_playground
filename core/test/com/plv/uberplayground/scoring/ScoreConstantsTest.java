package com.plv.uberplayground.scoring;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ScoreConstants to verify constant values and prevent instantiation.
 */
class ScoreConstantsTest {
    
    @Test
    void testConstantValues() {
        assertEquals(1, ScoreConstants.POINTS_PER_ENEMY_KILL);
        assertEquals(0, ScoreConstants.INITIAL_SCORE);
        assertEquals(5, ScoreConstants.MAX_HIGH_SCORES);
        assertEquals("highscores.txt", ScoreConstants.HIGH_SCORE_FILENAME);
        assertEquals(24f, ScoreConstants.SCORE_FONT_SIZE);
        assertEquals(50f, ScoreConstants.SCORE_DISPLAY_Y_OFFSET);
    }
    
    @Test
    void testCannotInstantiate() {
        // ScoreConstants should not be instantiable
        // This test verifies the private constructor throws AssertionError
        assertThrows(Exception.class, () -> {
            // Use reflection to access private constructor
            var constructor = ScoreConstants.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        });
    }
}