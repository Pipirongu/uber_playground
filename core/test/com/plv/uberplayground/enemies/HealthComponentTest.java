package com.plv.uberplayground.enemies;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for HealthComponent class functionality.
 * Tests health initialization, damage processing, and health percentage calculations.
 */
public class HealthComponentTest {
    
    private HealthComponent healthComponent;
    
    @BeforeEach
    void setUp() {
        // Initialize with standard enemy health from configuration
        healthComponent = new HealthComponent(SpawnConfiguration.ENEMY_HEALTH);
    }
    
    @Test
    void testHealthInitialization() {
        // Verify health initializes to 100 HP as specified in requirements
        assertEquals(SpawnConfiguration.ENEMY_HEALTH, healthComponent.getCurrentHealth());
        assertEquals(SpawnConfiguration.ENEMY_HEALTH, healthComponent.getMaxHealth());
        assertTrue(healthComponent.isAlive());
        assertEquals(1.0f, healthComponent.getHealthPercentage(), 0.001f);
    }
    
    @Test
    void testProjectileDamageProcessing() {
        // Test damage processing with 25 damage per projectile hit
        int initialHealth = healthComponent.getCurrentHealth();
        
        healthComponent.takeDamage(SpawnConfiguration.PROJECTILE_DAMAGE, 1.0f);
        
        assertEquals(initialHealth - SpawnConfiguration.PROJECTILE_DAMAGE, healthComponent.getCurrentHealth());
        assertEquals(75, healthComponent.getCurrentHealth()); // 100 - 25 = 75
        assertTrue(healthComponent.isAlive());
    }
    
    @Test
    void testHealthPercentageCalculation() {
        // Test health percentage calculation for visual display
        assertEquals(1.0f, healthComponent.getHealthPercentage(), 0.001f); // 100%
        
        healthComponent.takeDamage(25, 1.0f);
        assertEquals(0.75f, healthComponent.getHealthPercentage(), 0.001f); // 75%
        
        healthComponent.takeDamage(25, 2.0f);
        assertEquals(0.5f, healthComponent.getHealthPercentage(), 0.001f); // 50%
        
        healthComponent.takeDamage(50, 3.0f);
        assertEquals(0.0f, healthComponent.getHealthPercentage(), 0.001f); // 0%
        assertFalse(healthComponent.isAlive());
    }
    
    @Test
    void testMultipleProjectileHits() {
        // Test multiple projectile hits (4 hits = 100 damage = death)
        for (int i = 0; i < 4; i++) {
            healthComponent.takeDamage(SpawnConfiguration.PROJECTILE_DAMAGE, i + 1.0f);
            if (i < 3) {
                assertTrue(healthComponent.isAlive());
            }
        }
        
        assertEquals(0, healthComponent.getCurrentHealth());
        assertFalse(healthComponent.isAlive());
        assertEquals(0.0f, healthComponent.getHealthPercentage(), 0.001f);
    }
    
    @Test
    void testNegativeDamageRejection() {
        // Test that negative damage is rejected
        int initialHealth = healthComponent.getCurrentHealth();
        
        healthComponent.takeDamage(-10, 1.0f);
        
        assertEquals(initialHealth, healthComponent.getCurrentHealth());
        assertTrue(healthComponent.isAlive());
    }
    
    @Test
    void testOverkillDamage() {
        // Test that overkill damage is properly clamped
        healthComponent.takeDamage(150, 1.0f); // More than max health
        
        assertEquals(0, healthComponent.getCurrentHealth());
        assertFalse(healthComponent.isAlive());
        assertEquals(0.0f, healthComponent.getHealthPercentage(), 0.001f);
    }
}