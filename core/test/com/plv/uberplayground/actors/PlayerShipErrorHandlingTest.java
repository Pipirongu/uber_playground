package com.plv.uberplayground.actors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for PlayerShip error handling and edge cases.
 * Tests constants and logic without requiring Box2D physics initialization.
 */
public class PlayerShipErrorHandlingTest {
    
    @Test
    @DisplayName("Fire rate cooldown constant is correct")
    public void testFireRateCooldownConstant() {
        assertEquals(0.2f, PlayerShip.FIRE_RATE_COOLDOWN, 0.001f, 
                     "Fire rate cooldown should be 0.2 seconds");
    }
    
    @Test
    @DisplayName("Max linear velocity constant is correct")
    public void testMaxLinearVelocityConstant() {
        assertEquals(300.0f, PlayerShip.MAX_LINEAR_VELOCITY, 0.001f, 
                     "Max linear velocity should be 300.0 units/second");
    }

    @Test
    @DisplayName("Rotation speed constant is correct")
    public void testRotationSpeedConstant() {
        assertEquals(5.0f, PlayerShip.ROTATION_SPEED, 0.001f, 
                     "Rotation speed should be 5.0 rad/second");
    }

    @Test
    @DisplayName("Thrust force constant is correct")
    public void testThrustForceConstant() {
        assertEquals(15000.0f, PlayerShip.THRUST_FORCE, 0.001f, 
                     "Thrust force should be 15000.0");
    }
    
    
}