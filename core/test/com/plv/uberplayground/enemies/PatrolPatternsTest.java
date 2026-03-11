package com.plv.uberplayground.enemies;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * Test class for PatrolPatterns utility methods.
 */
public class PatrolPatternsTest {
    
    @Test
    @DisplayName("Rectangular patrol pattern creation")
    void testRectangularPatrolCreation() {
        Vector2 center = new Vector2(5, 5);
        float width = 4;
        float height = 6;
        
        Array<Vector2> waypoints = PatrolPatterns.createRectangularPatrol(center, width, height);
        
        assertEquals(4, waypoints.size, "Rectangular patrol should have 4 waypoints");
        
        // Check corners (clockwise from top-left)
        assertEquals(new Vector2(3, 8), waypoints.get(0), "Top-left corner");
        assertEquals(new Vector2(7, 8), waypoints.get(1), "Top-right corner");
        assertEquals(new Vector2(7, 2), waypoints.get(2), "Bottom-right corner");
        assertEquals(new Vector2(3, 2), waypoints.get(3), "Bottom-left corner");
    }
    
    @Test
    @DisplayName("Circular patrol pattern creation")
    void testCircularPatrolCreation() {
        Vector2 center = new Vector2(0, 0);
        float radius = 5;
        int numPoints = 4;
        
        Array<Vector2> waypoints = PatrolPatterns.createCircularPatrol(center, radius, numPoints);
        
        assertEquals(4, waypoints.size, "Circular patrol should have requested number of waypoints");
        
        // Check that all points are approximately at the correct distance from center
        for (Vector2 waypoint : waypoints) {
            float distance = waypoint.dst(center);
            assertEquals(radius, distance, 0.1f, "All waypoints should be at radius distance from center");
        }
    }
    
    @Test
    @DisplayName("Circular patrol with minimum points")
    void testCircularPatrolMinimumPoints() {
        Vector2 center = new Vector2(0, 0);
        float radius = 3;
        
        // Test with less than minimum (should default to 3)
        Array<Vector2> waypoints = PatrolPatterns.createCircularPatrol(center, radius, 2);
        assertEquals(3, waypoints.size, "Should default to minimum 3 points");
        
        // Test with exactly minimum
        waypoints = PatrolPatterns.createCircularPatrol(center, radius, 3);
        assertEquals(3, waypoints.size, "Should accept exactly 3 points");
    }
    
    @Test
    @DisplayName("Linear patrol pattern creation")
    void testLinearPatrolCreation() {
        Vector2 start = new Vector2(0, 0);
        Vector2 end = new Vector2(10, 5);
        
        Array<Vector2> waypoints = PatrolPatterns.createLinearPatrol(start, end);
        
        assertEquals(2, waypoints.size, "Linear patrol should have 2 waypoints");
        assertEquals(start, waypoints.get(0), "First waypoint should be start point");
        assertEquals(end, waypoints.get(1), "Second waypoint should be end point");
    }
    
    @Test
    @DisplayName("L-shaped patrol pattern creation")
    void testLShapedPatrolCreation() {
        Vector2 corner = new Vector2(2, 3);
        float horizontalLength = 4;
        float verticalLength = 6;
        
        // Test clockwise L-shape
        Array<Vector2> clockwiseWaypoints = PatrolPatterns.createLShapedPatrol(
            corner, horizontalLength, verticalLength, true);
        
        assertEquals(3, clockwiseWaypoints.size, "L-shaped patrol should have 3 waypoints");
        assertEquals(new Vector2(2, 3), clockwiseWaypoints.get(0), "First point should be corner");
        assertEquals(new Vector2(6, 3), clockwiseWaypoints.get(1), "Second point should be horizontal end");
        assertEquals(new Vector2(6, 9), clockwiseWaypoints.get(2), "Third point should be vertical end");
        
        // Test counter-clockwise L-shape
        Array<Vector2> counterClockwiseWaypoints = PatrolPatterns.createLShapedPatrol(
            corner, horizontalLength, verticalLength, false);
        
        assertEquals(3, counterClockwiseWaypoints.size, "L-shaped patrol should have 3 waypoints");
        assertEquals(new Vector2(2, 3), counterClockwiseWaypoints.get(0), "First point should be corner");
        assertEquals(new Vector2(2, 9), counterClockwiseWaypoints.get(1), "Second point should be vertical end");
        assertEquals(new Vector2(6, 9), counterClockwiseWaypoints.get(2), "Third point should be horizontal end");
    }
    
    @Test
    @DisplayName("Figure-8 patrol pattern creation")
    void testFigure8PatrolCreation() {
        Vector2 center = new Vector2(5, 5);
        float width = 8;
        float height = 6;
        
        Array<Vector2> waypoints = PatrolPatterns.createFigure8Patrol(center, width, height);
        
        assertEquals(8, waypoints.size, "Figure-8 patrol should have 8 waypoints");
        
        // Check that center point appears multiple times (characteristic of figure-8)
        int centerCount = 0;
        for (Vector2 waypoint : waypoints) {
            if (waypoint.equals(center)) {
                centerCount++;
            }
        }
        assertTrue(centerCount >= 3, "Figure-8 should pass through center multiple times");
    }
    
    @Test
    @DisplayName("Random patrol pattern creation")
    void testRandomPatrolCreation() {
        Rectangle bounds = new Rectangle(0, 0, 20, 15);
        int numWaypoints = 5;
        
        Array<Vector2> waypoints = PatrolPatterns.createRandomPatrol(bounds, numWaypoints);
        
        assertEquals(numWaypoints, waypoints.size, "Should create requested number of waypoints");
        
        // Check that all waypoints are within bounds
        for (Vector2 waypoint : waypoints) {
            assertTrue(waypoint.x >= bounds.x && waypoint.x <= bounds.x + bounds.width,
                      "Waypoint X should be within bounds");
            assertTrue(waypoint.y >= bounds.y && waypoint.y <= bounds.y + bounds.height,
                      "Waypoint Y should be within bounds");
        }
    }
    
    @Test
    @DisplayName("Random patrol with zero waypoints")
    void testRandomPatrolZeroWaypoints() {
        Rectangle bounds = new Rectangle(0, 0, 10, 10);
        
        Array<Vector2> waypoints = PatrolPatterns.createRandomPatrol(bounds, 0);
        
        assertEquals(0, waypoints.size, "Should create empty array for zero waypoints");
    }
}