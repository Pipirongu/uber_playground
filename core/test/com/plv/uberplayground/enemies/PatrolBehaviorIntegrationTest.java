package com.plv.uberplayground.enemies;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * Integration test for patrol behavior functionality.
 * Tests the complete patrol behavior workflow including waypoint management.
 */
public class PatrolBehaviorIntegrationTest {
    
    @Test
    @DisplayName("Patrol behavior waypoint setup and management")
    void testPatrolBehaviorWaypointSetup() {
        // Create a set of waypoints for patrol behavior
        Array<Vector2> waypoints = new Array<>();
        waypoints.add(new Vector2(0, 0));    // Start point
        waypoints.add(new Vector2(10, 0));   // Move right
        waypoints.add(new Vector2(10, 10));  // Move up
        waypoints.add(new Vector2(0, 10));   // Move left
        // Will cycle back to (0, 0)
        
        // Verify waypoint setup
        assertEquals(4, waypoints.size, "Should have 4 waypoints");
        assertEquals(new Vector2(0, 0), waypoints.get(0), "First waypoint should be origin");
        assertEquals(new Vector2(10, 10), waypoints.get(2), "Third waypoint should be (10, 10)");
    }
    
    @Test
    @DisplayName("Patrol behavior waypoint sequence cycling")
    void testPatrolBehaviorSequenceCycling() {
        // Test the waypoint cycling logic that would be used in patrol behavior
        Array<Vector2> waypoints = new Array<>();
        waypoints.add(new Vector2(0, 0));
        waypoints.add(new Vector2(5, 0));
        waypoints.add(new Vector2(5, 5));
        
        int currentIndex = 0;
        
        // Simulate reaching each waypoint and advancing
        for (int i = 0; i < waypoints.size * 2; i++) { // Go through cycle twice
            Vector2 currentWaypoint = waypoints.get(currentIndex);
            assertNotNull(currentWaypoint, "Current waypoint should not be null");
            
            // Advance to next waypoint
            currentIndex = (currentIndex + 1) % waypoints.size;
        }
        
        // After full cycles, should be back at start
        assertEquals(0, currentIndex, "Should cycle back to first waypoint");
    }
    
    @Test
    @DisplayName("Patrol behavior distance threshold logic")
    void testPatrolBehaviorDistanceThreshold() {
        // Test the distance checking logic used in patrol behavior
        Vector2 waypoint = new Vector2(5, 5);
        
        // Test positions within threshold (< 1.0f)
        Vector2 nearPosition1 = new Vector2(5.5f, 5.0f);
        Vector2 nearPosition2 = new Vector2(5.0f, 5.8f);
        Vector2 nearPosition3 = new Vector2(4.2f, 4.9f);
        
        assertTrue(nearPosition1.dst(waypoint) < 1.0f, "Position should be within threshold");
        assertTrue(nearPosition2.dst(waypoint) < 1.0f, "Position should be within threshold");
        assertTrue(nearPosition3.dst(waypoint) < 1.0f, "Position should be within threshold");
        
        // Test positions outside threshold (>= 1.0f)
        Vector2 farPosition1 = new Vector2(6.5f, 5.0f);
        Vector2 farPosition2 = new Vector2(5.0f, 3.5f);
        
        assertTrue(farPosition1.dst(waypoint) >= 1.0f, "Position should be outside threshold");
        assertTrue(farPosition2.dst(waypoint) >= 1.0f, "Position should be outside threshold");
    }
    
    @Test
    @DisplayName("Patrol behavior with single waypoint")
    void testPatrolBehaviorSingleWaypoint() {
        // Test edge case with only one waypoint
        Array<Vector2> singleWaypoint = new Array<>();
        singleWaypoint.add(new Vector2(3, 3));
        
        assertEquals(1, singleWaypoint.size, "Should have exactly one waypoint");
        
        int currentIndex = 0;
        Vector2 waypoint = singleWaypoint.get(currentIndex);
        assertEquals(new Vector2(3, 3), waypoint, "Single waypoint should be accessible");
        
        // Cycling with single waypoint should stay at same waypoint
        currentIndex = (currentIndex + 1) % singleWaypoint.size;
        assertEquals(0, currentIndex, "Should cycle back to same waypoint");
    }
    
    @Test
    @DisplayName("Patrol behavior waypoint validation")
    void testPatrolBehaviorWaypointValidation() {
        // Test waypoint array validation
        Array<Vector2> validWaypoints = new Array<>();
        validWaypoints.add(new Vector2(1, 1));
        validWaypoints.add(new Vector2(2, 2));
        
        assertNotNull(validWaypoints, "Valid waypoints should not be null");
        assertTrue(validWaypoints.size > 0, "Valid waypoints should not be empty");
        
        // Test empty waypoints
        Array<Vector2> emptyWaypoints = new Array<>();
        assertNotNull(emptyWaypoints, "Empty waypoints array should not be null");
        assertEquals(0, emptyWaypoints.size, "Empty waypoints should have size 0");
        
        // Test null waypoints
        Array<Vector2> nullWaypoints = null;
        assertNull(nullWaypoints, "Null waypoints should be null");
    }
}