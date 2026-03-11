package com.plv.uberplayground.actors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Projectile error handling and edge cases.
 * Tests constants and logic without requiring Box2D physics initialization.
 */
public class ProjectileErrorHandlingTest {
    
    @Test
    @DisplayName("Projectile speed constant is correct")
    public void testProjectileSpeedConstant() {
        assertEquals(7500.0f, Projectile.PROJECTILE_SPEED, 0.001f, 
                     "Projectile speed should be 7500.0 pixels/second");
    }
    
    @Test
    @DisplayName("Projectile lifetime constant is correct")
    public void testProjectileLifetimeConstant() {
        assertEquals(15.0f, Projectile.PROJECTILE_LIFETIME, 0.001f, 
                     "Projectile lifetime should be 15.0 seconds");
    }
}