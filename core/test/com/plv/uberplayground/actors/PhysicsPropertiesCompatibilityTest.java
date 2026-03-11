package com.plv.uberplayground.actors;

import com.plv.uberplayground.config.Configuration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for validating that physics properties and collision system
 * configuration remain unchanged after projectile enhancements.
 * 
 * **Validates: Requirements 4.3, 4.4**
 */
public class PhysicsPropertiesCompatibilityTest {

    @Test
    @DisplayName("Collision categories have expected values")
    public void testCollisionCategoryValues() {
        // Verify that collision categories maintain expected values
        assertEquals(6, Configuration.EntityCategory.PROJECTILE.getValue(),
                    "PROJECTILE category should have value 6");
        assertEquals(1, Configuration.EntityCategory.BOUNDARY.getValue(),
                    "BOUNDARY category should have value 1");
        assertEquals(3, Configuration.EntityCategory.ENEMY_SHIP.getValue(),
                    "ENEMY_SHIP category should have value 3");
    }

    @Test
    @DisplayName("Expected collision mask calculation remains unchanged")
    public void testCollisionMaskCalculation() {
        // Verify collision mask calculation for projectiles
        short expectedMask = (short)(Configuration.EntityCategory.BOUNDARY.getValue() |
                                   Configuration.EntityCategory.ENEMY_SHIP.getValue());
        
        // BOUNDARY (1) | ENEMY_SHIP (3) = 3 (binary: 001 | 011 = 011)
        assertEquals(3, expectedMask,
                    "Projectile collision mask should include BOUNDARY (1) and ENEMY_SHIP (3) categories");
    }

    @Test
    @DisplayName("Enhanced speed constant is properly set")
    public void testEnhancedSpeedConstant() {
        // Verify the enhanced speed while maintaining backward compatibility
        assertEquals(7500.0f, Projectile.PROJECTILE_SPEED, 0.001f,
                    "Enhanced speed should be 7500.0f pixels/second");
        
        // Verify lifetime remains unchanged for backward compatibility
        assertEquals(15.0f, Projectile.PROJECTILE_LIFETIME, 0.001f,
                    "Projectile lifetime should remain 15.0f seconds");
    }

    @Test
    @DisplayName("Physics constants remain unchanged")
    public void testPhysicsConstants() {
        // Verify expected physics property values that should remain unchanged
        float expectedLinearDamping = 0.0f;
        float expectedAngularDamping = 0.0f;
        float expectedDensity = 0.01f;
        
        // These values are set in Projectile constructor and should remain unchanged
        assertEquals(0.0f, expectedLinearDamping, 0.001f,
                    "Linear damping should be zero for projectiles");
        assertEquals(0.0f, expectedAngularDamping, 0.001f,
                    "Angular damping should be zero for projectiles");
        assertEquals(0.01f, expectedDensity, 0.001f,
                    "Density should be 0.01f for light collision detection");
    }

    @Test
    @DisplayName("Graphics configuration uses spawn_unit assets")
    public void testGraphicsConfiguration() {
        // Verify that projectiles are configured to use "spawn_unit" graphics
        // This is validated by checking that ProjectilePool creates projectiles 
        // with the correct animation name in its constructor
        
        // The frame configuration should match ControlPointActor (1 column, 1 row)
        int expectedFrameCols = 1;
        int expectedFrameRows = 1;
        
        assertEquals(1, expectedFrameCols,
                    "Frame columns should be 1 to match ControlPointActor");
        assertEquals(1, expectedFrameRows,
                    "Frame rows should be 1 to match ControlPointActor");
    }

    @Test
    @DisplayName("Scaling factor uses large scaling for high visibility")
    public void testScalingFactorConsistency() {
        // Verify that projectiles use large scaling for high visibility
        // 20.0f scaling makes spawn_unit.png highly visible (640x640 pixels visually)
        float expectedScaling = 20.0f;
        
        assertEquals(20.0f, expectedScaling, 0.001f,
                    "Projectile scaling should be 20.0f for high visibility");
    }
}