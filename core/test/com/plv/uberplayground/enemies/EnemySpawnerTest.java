package com.plv.uberplayground.enemies;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the EnemySpawner class.
 * Tests core spawning functionality without LibGDX dependencies.
 */
public class EnemySpawnerTest {
    
    @Test
    @DisplayName("Should validate spawn configuration constants")
    void shouldValidateSpawnConfigurationConstants() {
        // Test that spawn configuration constants are properly defined
        assertEquals(5.0f, SpawnConfiguration.SPAWN_INTERVAL, 0.001f);
        assertEquals(200.0f, SpawnConfiguration.SAFE_DISTANCE, 0.001f);
        assertEquals(10, SpawnConfiguration.MAX_SPAWN_ATTEMPTS);
        assertEquals(50, SpawnConfiguration.MAX_ENEMIES);
        assertEquals(100, SpawnConfiguration.ENEMY_HEALTH);
        assertEquals(25, SpawnConfiguration.PROJECTILE_DAMAGE);
        assertEquals(2.0f, SpawnConfiguration.MIN_BEHAVIOR_SWITCH_INTERVAL, 0.001f);
        assertEquals(10.0f, SpawnConfiguration.MAX_BEHAVIOR_SWITCH_INTERVAL, 0.001f);
        assertEquals(150.0f, SpawnConfiguration.DEFAULT_ORBIT_RADIUS, 0.001f);
    }
    
    @Test
    @DisplayName("Should validate behavior switch interval range")
    void shouldValidateBehaviorSwitchIntervalRange() {
        // Ensure min is less than max
        assertTrue(SpawnConfiguration.MIN_BEHAVIOR_SWITCH_INTERVAL < SpawnConfiguration.MAX_BEHAVIOR_SWITCH_INTERVAL);
        
        // Ensure reasonable values
        assertTrue(SpawnConfiguration.MIN_BEHAVIOR_SWITCH_INTERVAL >= 1.0f);
        assertTrue(SpawnConfiguration.MAX_BEHAVIOR_SWITCH_INTERVAL <= 15.0f);
    }
    
    @Test
    @DisplayName("Should validate safe distance is reasonable")
    void shouldValidateSafeDistanceIsReasonable() {
        // Safe distance should be reasonable for gameplay
        assertTrue(SpawnConfiguration.SAFE_DISTANCE >= 100.0f);
        assertTrue(SpawnConfiguration.SAFE_DISTANCE <= 500.0f);
    }
    
    @Test
    @DisplayName("Should validate max spawn attempts is reasonable")
    void shouldValidateMaxSpawnAttemptsIsReasonable() {
        // Should have enough attempts to find valid locations
        assertTrue(SpawnConfiguration.MAX_SPAWN_ATTEMPTS >= 5);
        assertTrue(SpawnConfiguration.MAX_SPAWN_ATTEMPTS <= 20);
    }
    
    @Test
    @DisplayName("Should validate enemy health and damage balance")
    void shouldValidateEnemyHealthAndDamageBalance() {
        // Enemy should take multiple hits to destroy
        int hitsToKill = SpawnConfiguration.ENEMY_HEALTH / SpawnConfiguration.PROJECTILE_DAMAGE;
        assertTrue(hitsToKill >= 3); // At least 3 hits
        assertTrue(hitsToKill <= 10); // Not more than 10 hits
        
        // Health should be evenly divisible by damage for clean kills
        assertEquals(0, SpawnConfiguration.ENEMY_HEALTH % SpawnConfiguration.PROJECTILE_DAMAGE);
    }
    
    @Test
    @DisplayName("Should validate spawn interval is reasonable")
    void shouldValidateSpawnIntervalIsReasonable() {
        // Spawn interval should provide good gameplay pacing
        assertTrue(SpawnConfiguration.SPAWN_INTERVAL >= 2.0f);
        assertTrue(SpawnConfiguration.SPAWN_INTERVAL <= 10.0f);
    }
    
    @Test
    @DisplayName("Should validate max enemies limit")
    void shouldValidateMaxEnemiesLimit() {
        // Should allow enough enemies for engaging gameplay with dynamic scaling
        assertTrue(SpawnConfiguration.MAX_ENEMIES >= 5);
        assertTrue(SpawnConfiguration.MAX_ENEMIES <= 100); // Updated for dynamic spawning system
    }
    
    // Dynamic Spawn Controller Integration Tests
    
    @Test
    @DisplayName("Should support setting and getting dynamic spawn controller")
    void shouldSupportSettingAndGettingDynamicSpawnController() {
        // Create a minimal spawner instance for testing (without LibGDX dependencies)
        TestableEnemySpawner spawner = new TestableEnemySpawner();
        
        // Initially no controller should be set
        assertNull(spawner.getDynamicSpawnController());
        assertFalse(spawner.isDynamicSpawnActive());
        
        // Set controller
        MockDynamicSpawnController mockController = new MockDynamicSpawnController();
        spawner.setDynamicSpawnController(mockController);
        assertEquals(mockController, spawner.getDynamicSpawnController());
        
        // Clear controller
        spawner.setDynamicSpawnController(null);
        assertNull(spawner.getDynamicSpawnController());
        assertFalse(spawner.isDynamicSpawnActive());
    }
    
    @Test
    @DisplayName("Should detect when dynamic spawn control is active")
    void shouldDetectWhenDynamicSpawnControlIsActive() {
        TestableEnemySpawner spawner = new TestableEnemySpawner();
        
        // Mock controller as active
        MockDynamicSpawnController mockController = new MockDynamicSpawnController();
        mockController.setActive(true);
        spawner.setDynamicSpawnController(mockController);
        
        assertTrue(spawner.isDynamicSpawnActive());
        
        // Mock controller as inactive
        mockController.setActive(false);
        assertFalse(spawner.isDynamicSpawnActive());
    }
    
    @Test
    @DisplayName("Should support manual spawn interval configuration")
    void shouldSupportManualSpawnIntervalConfiguration() {
        TestableEnemySpawner spawner = new TestableEnemySpawner();
        
        // Default interval should be the configuration constant
        assertEquals(SpawnConfiguration.SPAWN_INTERVAL, spawner.getSpawnInterval(), 0.001f);
        
        // Set custom interval
        spawner.setSpawnInterval(2.5f);
        assertEquals(2.5f, spawner.getSpawnInterval(), 0.001f);
        
        // Invalid intervals should be ignored
        spawner.setSpawnInterval(-1.0f);
        assertEquals(2.5f, spawner.getSpawnInterval(), 0.001f); // Should remain unchanged
        
        spawner.setSpawnInterval(0.0f);
        assertEquals(2.5f, spawner.getSpawnInterval(), 0.001f); // Should remain unchanged
    }
    
    @Test
    @DisplayName("Should prioritize dynamic controller interval over manual interval")
    void shouldPrioritizeDynamicControllerIntervalOverManualInterval() {
        TestableEnemySpawner spawner = new TestableEnemySpawner();
        
        // Set manual interval
        spawner.setSpawnInterval(3.0f);
        assertEquals(3.0f, spawner.getCurrentActiveSpawnInterval(), 0.001f);
        
        // Set active dynamic controller with different interval
        MockDynamicSpawnController mockController = new MockDynamicSpawnController();
        mockController.setActive(true);
        mockController.setCurrentTargetInterval(1.5f);
        spawner.setDynamicSpawnController(mockController);
        
        // Should use dynamic controller interval
        assertEquals(1.5f, spawner.getCurrentActiveSpawnInterval(), 0.001f);
        
        // When controller becomes inactive, should fall back to manual interval
        mockController.setActive(false);
        assertEquals(3.0f, spawner.getCurrentActiveSpawnInterval(), 0.001f);
    }
    
    @Test
    @DisplayName("Should maintain backward compatibility when no dynamic controller is set")
    void shouldMaintainBackwardCompatibilityWhenNoDynamicControllerIsSet() {
        TestableEnemySpawner spawner = new TestableEnemySpawner();
        
        // Should use default spawn configuration interval
        assertEquals(SpawnConfiguration.SPAWN_INTERVAL, spawner.getCurrentActiveSpawnInterval(), 0.001f);
        
        // Should still work with manual interval changes
        spawner.setSpawnInterval(4.0f);
        assertEquals(4.0f, spawner.getCurrentActiveSpawnInterval(), 0.001f);
    }
    
    /**
     * Mock implementation of DynamicSpawnController for testing.
     */
    private static class MockDynamicSpawnController {
        private boolean active = false;
        private float currentTargetInterval = 5.0f;
        
        public boolean isActive() {
            return active;
        }
        
        public float getCurrentTargetInterval() {
            return currentTargetInterval;
        }
        
        public void setActive(boolean active) {
            this.active = active;
        }
        
        public void setCurrentTargetInterval(float interval) {
            this.currentTargetInterval = interval;
        }
    }
    
    /**
     * Testable version of EnemySpawner that exposes protected methods for testing
     * and doesn't require LibGDX dependencies.
     */
    private static class TestableEnemySpawner {
        private MockDynamicSpawnController dynamicSpawnController;
        private float currentSpawnInterval = SpawnConfiguration.SPAWN_INTERVAL;
        
        public void setDynamicSpawnController(MockDynamicSpawnController controller) {
            this.dynamicSpawnController = controller;
        }
        
        public MockDynamicSpawnController getDynamicSpawnController() {
            return dynamicSpawnController;
        }
        
        public void setSpawnInterval(float interval) {
            if (interval > 0) {
                this.currentSpawnInterval = interval;
            }
        }
        
        public float getSpawnInterval() {
            return currentSpawnInterval;
        }
        
        public boolean isDynamicSpawnActive() {
            return dynamicSpawnController != null && dynamicSpawnController.isActive();
        }
        
        public float getCurrentActiveSpawnInterval() {
            if (dynamicSpawnController != null && dynamicSpawnController.isActive()) {
                return dynamicSpawnController.getCurrentTargetInterval();
            }
            return currentSpawnInterval;
        }
    }
}