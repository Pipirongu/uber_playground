package com.plv.uberplayground.scoring;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ScoreEvent enum.
 */
class ScoreEventTest {
    
    @Test
    void testEnemyKillEvent() {
        ScoreEvent event = ScoreEvent.ENEMY_KILL;
        
        assertEquals(ScoreConstants.POINTS_PER_ENEMY_KILL, event.getPointValue());
        assertEquals(1, event.getPointValue()); // Should be 1 point per kill
    }
    
    @Test
    void testGameStartEvent() {
        ScoreEvent event = ScoreEvent.GAME_START;
        
        assertEquals(0, event.getPointValue());
    }
    
    @Test
    void testGameOverEvent() {
        ScoreEvent event = ScoreEvent.GAME_OVER;
        
        assertEquals(0, event.getPointValue());
    }
    
    @Test
    void testAllEventsExist() {
        // Verify all expected events are defined
        ScoreEvent[] events = ScoreEvent.values();
        
        assertEquals(3, events.length);
        
        boolean hasEnemyKill = false;
        boolean hasGameStart = false;
        boolean hasGameOver = false;
        
        for (ScoreEvent event : events) {
            switch (event) {
                case ENEMY_KILL:
                    hasEnemyKill = true;
                    break;
                case GAME_START:
                    hasGameStart = true;
                    break;
                case GAME_OVER:
                    hasGameOver = true;
                    break;
            }
        }
        
        assertTrue(hasEnemyKill, "ENEMY_KILL event should exist");
        assertTrue(hasGameStart, "GAME_START event should exist");
        assertTrue(hasGameOver, "GAME_OVER event should exist");
    }
}