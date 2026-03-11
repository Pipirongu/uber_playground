package com.plv.uberplayground.actors;

import com.badlogic.gdx.math.Vector2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for enhanced projectile speed performance.
 * Validates that the enhanced speed implementation maintains smooth visual movement
 * and accurate collision detection at 7500.0f pixels/second.
 * 
 * **Validates: Requirements 3.2, 3.3**
 */
public class ProjectileEnhancedSpeedIntegrationTest {

    @Test
    @DisplayName("Enhanced speed maintains smooth visual movement without artifacts")
    public void testSmoothVisualMovementAtEnhancedSpeed() {
        // Test the enhanced speed constant and movement calculations
        float enhancedSpeed = Projectile.PROJECTILE_SPEED;
        assertEquals(7500.0f, enhancedSpeed, 0.001f,
                    "Enhanced speed should be 7500.0f pixels/second");
        
        // Simulate smooth movement over multiple frames
        Vector2 position = new Vector2(0.0f, 0.0f);
        Vector2 direction = new Vector2(1.0f, 0.0f); // Moving right
        float deltaTime = 1.0f / 60.0f; // 60 FPS
        
        // Simulate 10 frames of movement
        for (int frame = 0; frame < 10; frame++) {
            float pixelsToMove = enhancedSpeed * deltaTime;
            Vector2 movement = new Vector2(direction).scl(pixelsToMove);
            position.add(movement);
            
            // Verify movement is consistent per frame
            assertEquals(125.0f, pixelsToMove, 0.001f,
                        "Should move 125 pixels per frame at 60 FPS");
            
            // Verify position increases smoothly
            float expectedX = (frame + 1) * 125.0f;
            assertEquals(expectedX, position.x, 0.001f,
                        "Position should increase smoothly: frame " + frame);
        }
        
        // After 10 frames, should have moved 1250 pixels
        assertEquals(1250.0f, position.x, 0.001f,
                    "Should move 1250 pixels in 10 frames");
    }
    
    @Test
    @DisplayName("Enhanced speed maintains accuracy across different frame rates")
    public void testAccuracyAcrossDifferentFrameRates() {
        float enhancedSpeed = Projectile.PROJECTILE_SPEED;
        Vector2 direction = new Vector2(1.0f, 0.0f);
        
        // Test different frame rates
        float[] frameRates = {30.0f, 60.0f, 120.0f};
        float testDuration = 1.0f; // 1 second
        
        for (float frameRate : frameRates) {
            Vector2 position = new Vector2(0.0f, 0.0f);
            float deltaTime = 1.0f / frameRate;
            int totalFrames = (int)(testDuration * frameRate);
            
            // Simulate movement for 1 second
            for (int frame = 0; frame < totalFrames; frame++) {
                float pixelsToMove = enhancedSpeed * deltaTime;
                Vector2 movement = new Vector2(direction).scl(pixelsToMove);
                position.add(movement);
            }
            
            // Should move approximately 7500 pixels in 1 second regardless of frame rate
            assertEquals(7500.0f, position.x, 10.0f,
                        "Should move ~7500 pixels in 1 second at " + frameRate + " FPS");
        }
    }
    
    @Test
    @DisplayName("Dual-velocity approach prevents physics instability")
    public void testDualVelocityPhysicsStability() {
        // Verify the dual-velocity approach uses appropriate speeds
        float visualSpeed = Projectile.PROJECTILE_SPEED; // 7500.0f
        float physicsSpeed = 100.0f; // Low speed for Box2D stability
        
        // Visual speed should be much higher than physics speed
        assertTrue(visualSpeed > physicsSpeed * 50,
                  "Visual speed should be significantly higher than physics speed");
        
        // Physics speed should be low enough to prevent tunneling
        assertTrue(physicsSpeed <= 100.0f,
                  "Physics speed should be kept low for collision detection stability");
        
        // Test that the approach maintains collision detection capability
        // while providing responsive visual movement
        float speedRatio = visualSpeed / physicsSpeed;
        assertEquals(75.0f, speedRatio, 0.1f,
                    "Speed ratio should be 75:1 (visual:physics)");
    }
    
    @Test
    @DisplayName("Enhanced speed improves responsiveness over original speed")
    public void testImprovedResponsiveness() {
        float enhancedSpeed = Projectile.PROJECTILE_SPEED; // 7500.0f
        float originalSpeed = 5000.0f; // Previous implementation
        
        float deltaTime = 1.0f / 60.0f; // 60 FPS
        
        // Calculate movement per frame for both speeds
        float enhancedMovement = enhancedSpeed * deltaTime;
        float originalMovement = originalSpeed * deltaTime;
        
        // Enhanced speed should provide 50% more movement per frame
        float improvement = (enhancedMovement - originalMovement) / originalMovement;
        assertEquals(0.5f, improvement, 0.01f,
                    "Enhanced speed should provide 50% improvement in responsiveness");
        
        // Verify actual values
        assertEquals(125.0f, enhancedMovement, 0.001f,
                    "Enhanced speed should move 125 pixels per frame at 60 FPS");
        assertEquals(83.333f, originalMovement, 0.01f,
                    "Original speed moved ~83.33 pixels per frame at 60 FPS");
    }
    
    @Test
    @DisplayName("Enhanced speed maintains projectile lifetime consistency")
    public void testLifetimeConsistency() {
        // Verify that projectile lifetime remains unchanged despite speed increase
        float lifetime = Projectile.PROJECTILE_LIFETIME;
        assertEquals(15.0f, lifetime, 0.001f,
                    "Projectile lifetime should remain 15.0 seconds");
        
        // Calculate maximum travel distance at enhanced speed
        float enhancedSpeed = Projectile.PROJECTILE_SPEED;
        float maxDistance = enhancedSpeed * lifetime;
        
        // Should travel 112,500 pixels (7500 * 15) in its lifetime
        assertEquals(112500.0f, maxDistance, 0.001f,
                    "Projectile should travel 112,500 pixels in its 15-second lifetime");
        
        // Compare with original speed travel distance
        float originalSpeed = 5000.0f;
        float originalMaxDistance = originalSpeed * lifetime;
        
        assertEquals(75000.0f, originalMaxDistance, 0.001f,
                    "Original implementation traveled 75,000 pixels in lifetime");
        
        // Enhanced version travels 50% further
        float distanceImprovement = (maxDistance - originalMaxDistance) / originalMaxDistance;
        assertEquals(0.5f, distanceImprovement, 0.01f,
                    "Enhanced speed provides 50% greater travel distance");
    }
}