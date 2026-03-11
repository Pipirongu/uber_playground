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
        assertEquals(10, SpawnConfiguration.MAX_ENEMIES);
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
        // Should allow enough enemies for engaging gameplay
        assertTrue(SpawnConfiguration.MAX_ENEMIES >= 5);
        assertTrue(SpawnConfiguration.MAX_ENEMIES <= 20);
    }
}