package com.plv.uberplayground.enemies;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * Utility class for creating common patrol patterns.
 * Provides pre-defined waypoint configurations for typical patrol behaviors.
 */
public class PatrolPatterns {
    
    /**
     * Creates a rectangular patrol pattern.
     * @param center Center point of the rectangle
     * @param width Width of the rectangle
     * @param height Height of the rectangle
     * @return Array of waypoints forming a rectangle
     */
    public static Array<Vector2> createRectangularPatrol(Vector2 center, float width, float height) {
        Array<Vector2> waypoints = new Array<>();
        
        float halfWidth = width / 2;
        float halfHeight = height / 2;
        
        // Start at top-left, go clockwise
        waypoints.add(new Vector2(center.x - halfWidth, center.y + halfHeight));
        waypoints.add(new Vector2(center.x + halfWidth, center.y + halfHeight));
        waypoints.add(new Vector2(center.x + halfWidth, center.y - halfHeight));
        waypoints.add(new Vector2(center.x - halfWidth, center.y - halfHeight));
        
        return waypoints;
    }
    
    /**
     * Creates a circular patrol pattern with specified number of points.
     * @param center Center point of the circle
     * @param radius Radius of the circle
     * @param numPoints Number of waypoints around the circle (minimum 3)
     * @return Array of waypoints forming a circle
     */
    public static Array<Vector2> createCircularPatrol(Vector2 center, float radius, int numPoints) {
        if (numPoints < 3) {
            numPoints = 3; // Minimum for a meaningful circular pattern
        }
        
        Array<Vector2> waypoints = new Array<>();
        
        for (int i = 0; i < numPoints; i++) {
            float angle = (float) (2 * Math.PI * i / numPoints);
            float x = center.x + radius * (float) Math.cos(angle);
            float y = center.y + radius * (float) Math.sin(angle);
            waypoints.add(new Vector2(x, y));
        }
        
        return waypoints;
    }
    
    /**
     * Creates a linear back-and-forth patrol pattern.
     * @param start Starting point
     * @param end Ending point
     * @return Array of waypoints for back-and-forth movement
     */
    public static Array<Vector2> createLinearPatrol(Vector2 start, Vector2 end) {
        Array<Vector2> waypoints = new Array<>();
        waypoints.add(new Vector2(start));
        waypoints.add(new Vector2(end));
        return waypoints;
    }
    
    /**
     * Creates an L-shaped patrol pattern.
     * @param corner Corner point of the L
     * @param horizontalLength Length of horizontal segment
     * @param verticalLength Length of vertical segment
     * @param clockwise Whether to patrol clockwise or counter-clockwise
     * @return Array of waypoints forming an L shape
     */
    public static Array<Vector2> createLShapedPatrol(Vector2 corner, float horizontalLength, 
                                                     float verticalLength, boolean clockwise) {
        Array<Vector2> waypoints = new Array<>();
        
        if (clockwise) {
            waypoints.add(new Vector2(corner.x, corner.y));
            waypoints.add(new Vector2(corner.x + horizontalLength, corner.y));
            waypoints.add(new Vector2(corner.x + horizontalLength, corner.y + verticalLength));
        } else {
            waypoints.add(new Vector2(corner.x, corner.y));
            waypoints.add(new Vector2(corner.x, corner.y + verticalLength));
            waypoints.add(new Vector2(corner.x + horizontalLength, corner.y + verticalLength));
        }
        
        return waypoints;
    }
    
    /**
     * Creates a figure-8 patrol pattern.
     * @param center Center point of the figure-8
     * @param width Width of the figure-8
     * @param height Height of the figure-8
     * @return Array of waypoints forming a figure-8 shape
     */
    public static Array<Vector2> createFigure8Patrol(Vector2 center, float width, float height) {
        Array<Vector2> waypoints = new Array<>();
        
        float halfWidth = width / 2;
        float halfHeight = height / 2;
        
        // Create figure-8 with 8 points
        waypoints.add(new Vector2(center.x, center.y));                           // Center
        waypoints.add(new Vector2(center.x - halfWidth, center.y + halfHeight));  // Top-left
        waypoints.add(new Vector2(center.x, center.y));                           // Center
        waypoints.add(new Vector2(center.x + halfWidth, center.y + halfHeight));  // Top-right
        waypoints.add(new Vector2(center.x, center.y));                           // Center
        waypoints.add(new Vector2(center.x + halfWidth, center.y - halfHeight));  // Bottom-right
        waypoints.add(new Vector2(center.x, center.y));                           // Center
        waypoints.add(new Vector2(center.x - halfWidth, center.y - halfHeight));  // Bottom-left
        
        return waypoints;
    }
    
    /**
     * Creates a random patrol pattern within specified bounds.
     * @param bounds Rectangle defining the patrol area
     * @param numWaypoints Number of random waypoints to generate
     * @return Array of random waypoints within bounds
     */
    public static Array<Vector2> createRandomPatrol(com.badlogic.gdx.math.Rectangle bounds, int numWaypoints) {
        Array<Vector2> waypoints = new Array<>();
        
        for (int i = 0; i < numWaypoints; i++) {
            float x = bounds.x + (float) Math.random() * bounds.width;
            float y = bounds.y + (float) Math.random() * bounds.height;
            waypoints.add(new Vector2(x, y));
        }
        
        return waypoints;
    }
}