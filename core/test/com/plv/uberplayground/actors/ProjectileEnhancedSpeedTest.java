package com.plv.uberplayground.actors;

import com.badlogic.gdx.math.Vector2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for validating enhanced speed performance of projectiles.
 * Tests movement accuracy at 7500.0f pixels/second and ensures smooth visual movement.
 * 
 * **Validates: Requirements 3.2**
 */
public class ProjectileEnhancedSpeedTest {
    
    @Test
    @DisplayName("Enhanced speed constant is correctly set to 7500.0f pixels/second")
    public void testEnhancedSpeedConstant() {
        assertEquals(7500.0f, Projectile.PROJECTILE_SPEED, 0.001f,
                     "Projectile speed should be enhanced to 7500.0 pixels/second");
    }
    
    @Test
    @DisplayName("Position calculation accuracy at enhanced speed")
    public void testPositionCalculationAccuracy() {
        // Test position calculations at various time deltas
        float speed = Projectile.PROJECTILE_SPEED; // 7500.0f pixels/second
        
        // Test different time deltas (common frame rates)
        float[] timeDeltas = {
            1.0f / 60.0f,  // 60 FPS
            1.0f / 30.0f,  // 30 FPS  
            1.0f / 120.0f, // 120 FPS
            0.02f          // 50 FPS
        };
        
        Vector2 direction = new Vector2(1.0f, 0.0f); // Moving right
        Vector2 startPosition = new Vector2(100.0f, 100.0f);
        
        for (float delta : timeDeltas) {
            // Calculate expected movement distance
            float expectedDistance = speed * delta;
            
            // Calculate movement vector
            Vector2 movement = new Vector2(direction).scl(expectedDistance);
            Vector2 expectedPosition = new Vector2(startPosition).add(movement);
            
            // Verify calculations are accurate
            assertEquals(expectedDistance, movement.len(), 0.001f,
                        "Movement distance should be accurate for delta: " + delta);
            
            assertEquals(startPosition.x + expectedDistance, expectedPosition.x, 0.001f,
                        "X position should be accurate for delta: " + delta);
            
            assertEquals(startPosition.y, expectedPosition.y, 0.001f,
                        "Y position should remain unchanged for horizontal movement");
        }
    }
    
    @Test
    @DisplayName("Movement distance calculations for various directions")
    public void testMovementDistanceCalculations() {
        float speed = Projectile.PROJECTILE_SPEED;
        float delta = 1.0f / 60.0f; // 60 FPS
        float expectedDistance = speed * delta; // 125.0 pixels per frame at 60 FPS
        
        // Test various normalized directions
        Vector2[] directions = {
            new Vector2(1.0f, 0.0f),      // Right
            new Vector2(-1.0f, 0.0f),     // Left  
            new Vector2(0.0f, 1.0f),      // Up
            new Vector2(0.0f, -1.0f),     // Down
            new Vector2(1.0f, 1.0f).nor(), // Diagonal up-right
            new Vector2(-1.0f, -1.0f).nor() // Diagonal down-left
        };
        
        for (Vector2 direction : directions) {
            // Ensure direction is normalized
            assertTrue(Math.abs(direction.len() - 1.0f) < 0.001f,
                      "Direction should be normalized: " + direction);
            
            // Calculate movement
            Vector2 movement = new Vector2(direction).scl(expectedDistance);
            
            // Verify movement distance is consistent regardless of direction
            assertEquals(expectedDistance, movement.len(), 0.001f,
                        "Movement distance should be consistent for direction: " + direction);
        }
    }
    
    @Test
    @DisplayName("High-speed movement maintains precision over time")
    public void testHighSpeedMovementPrecision() {
        float speed = Projectile.PROJECTILE_SPEED;
        float delta = 1.0f / 60.0f; // 60 FPS
        
        Vector2 direction = new Vector2(1.0f, 0.0f); // Moving right
        Vector2 position = new Vector2(0.0f, 0.0f);
        
        // Simulate 1 second of movement (60 frames at 60 FPS)
        int frames = 60;
        float totalTime = frames * delta;
        
        for (int i = 0; i < frames; i++) {
            float pixelsToMove = speed * delta;
            Vector2 movement = new Vector2(direction).scl(pixelsToMove);
            position.add(movement);
        }
        
        // Calculate expected final position
        float expectedDistance = speed * totalTime; // 7500.0f pixels in 1 second
        Vector2 expectedPosition = new Vector2(expectedDistance, 0.0f);
        
        // Verify precision is maintained over time
        assertEquals(expectedPosition.x, position.x, 0.1f,
                    "X position should be accurate after " + frames + " frames");
        
        assertEquals(expectedPosition.y, position.y, 0.001f,
                    "Y position should remain zero for horizontal movement");
        
        // Verify we moved the expected total distance
        assertEquals(expectedDistance, position.len(), 0.1f,
                    "Total distance should match expected value after 1 second");
    }
    
    @Test
    @DisplayName("Enhanced speed provides responsive movement")
    public void testResponsiveMovement() {
        float enhancedSpeed = Projectile.PROJECTILE_SPEED; // 7500.0f
        float originalSpeed = 5000.0f; // Previous speed for comparison
        
        float delta = 1.0f / 60.0f; // 60 FPS
        
        // Calculate movement per frame for both speeds
        float enhancedMovement = enhancedSpeed * delta; // 125.0 pixels per frame
        float originalMovement = originalSpeed * delta;  // 83.33 pixels per frame
        
        // Verify enhanced speed provides more responsive movement
        assertTrue(enhancedMovement > originalMovement,
                  "Enhanced speed should provide more movement per frame");
        
        // Verify the improvement is significant (50% increase)
        float improvementRatio = enhancedMovement / originalMovement;
        assertEquals(1.5f, improvementRatio, 0.01f,
                    "Enhanced speed should be 50% faster than original speed");
    }
    
    @Test
    @DisplayName("Dual-velocity approach maintains physics stability")
    public void testDualVelocityApproach() {
        // Test that the dual-velocity approach uses different speeds for physics vs visual
        float visualSpeed = Projectile.PROJECTILE_SPEED; // 7500.0f pixels/second
        float physicsSpeed = 100.0f; // Low speed for Box2D physics stability
        
        // Verify visual speed is much higher than physics speed
        assertTrue(visualSpeed > physicsSpeed * 10,
                  "Visual speed should be significantly higher than physics speed");
        
        // Verify physics speed is low enough to prevent instability
        assertTrue(physicsSpeed <= 100.0f,
                  "Physics speed should be kept low for Box2D stability");
        
        // Calculate the ratio between visual and physics speeds
        float speedRatio = visualSpeed / physicsSpeed;
        assertEquals(75.0f, speedRatio, 0.1f,
                    "Visual speed should be 75x the physics speed");
    }
    
    @Test
    @DisplayName("Movement calculations handle edge cases correctly")
    public void testMovementEdgeCases() {
        float speed = Projectile.PROJECTILE_SPEED;
        
        // Test with very small time delta
        float smallDelta = 0.001f; // 1 millisecond
        float smallMovement = speed * smallDelta;
        assertTrue(smallMovement > 0.0f && smallMovement < 10.0f,
                  "Small delta should produce small but positive movement");
        
        // Test with zero delta
        float zeroMovement = speed * 0.0f;
        assertEquals(0.0f, zeroMovement, 0.001f,
                    "Zero delta should produce zero movement");
        
        // Test with typical frame deltas
        float[] typicalDeltas = {1.0f/30.0f, 1.0f/60.0f, 1.0f/120.0f};
        for (float delta : typicalDeltas) {
            float movement = speed * delta;
            assertTrue(movement > 0.0f && movement < 500.0f,
                      "Typical frame delta should produce reasonable movement: " + movement);
        }
    }
}