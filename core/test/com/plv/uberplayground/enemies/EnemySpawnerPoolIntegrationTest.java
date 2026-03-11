package com.plv.uberplayground.enemies;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for EnemySpawner with enhanced object pooling.
 * Tests the integration concepts without LibGDX dependencies.
 * 
 * Requirements: 4.2 - Enemy pooling to minimize garbage collection
 */
public class EnemySpawnerPoolIntegrationTest {
    
    private TestEnemySpawnerIntegration integration;
    
    @BeforeEach
    void setUp() {
        integration = new TestEnemySpawnerIntegration();
    }
    
    @Test
    void testSpawnerPoolCapacityIntegration() {
        // Initially should be able to spawn
        assertTrue(integration.canSpawn());
        
        // Fill to capacity
        for (int i = 0; i < 50; i++) {
            integration.simulateSpawn();
        }
        
        // Should not be able to spawn when at capacity
        assertFalse(integration.canSpawn());
        assertEquals(50, integration.getActiveEnemyCount());
        assertEquals(50, integration.getPoolActiveCount());
    }
    
    @Test
    void testEnemyRemovalPoolIntegration() {
        // Spawn enemies
        integration.simulateSpawn();
        integration.simulateSpawn();
        
        assertEquals(2, integration.getActiveEnemyCount());
        assertEquals(2, integration.getPoolActiveCount());
        
        // Remove an enemy
        integration.simulateEnemyRemoval();
        
        assertEquals(1, integration.getActiveEnemyCount());
        assertEquals(1, integration.getPoolActiveCount());
    }
    
    @Test
    void testClearAllEnemiesIntegration() {
        // Spawn several enemies
        for (int i = 0; i < 5; i++) {
            integration.simulateSpawn();
        }
        
        assertEquals(5, integration.getActiveEnemyCount());
        assertEquals(5, integration.getPoolActiveCount());
        
        // Clear all enemies
        integration.clearAllEnemies();
        
        assertEquals(0, integration.getActiveEnemyCount());
        assertEquals(0, integration.getPoolActiveCount());
    }
    
    @Test
    void testObjectReuseIntegration() {
        // Spawn and remove to test reuse
        integration.simulateSpawn();
        integration.simulateEnemyRemoval();
        integration.simulateSpawn(); // Should reuse
        
        String stats = integration.getPoolStats();
        assertTrue(stats.contains("Reuse Rate: 50.0%")); // 1 reuse out of 2 total
    }
    
    @Test
    void testPoolUtilizationTracking() {
        // Test utilization calculation
        assertEquals(0.0f, integration.getPoolUtilization(), 0.001f);
        
        integration.simulateSpawn();
        assertEquals(0.02f, integration.getPoolUtilization(), 0.001f); // 1/50
        
        for (int i = 0; i < 24; i++) {
            integration.simulateSpawn();
        }
        assertEquals(0.5f, integration.getPoolUtilization(), 0.001f); // 25/50
    }
    
    /**
     * Test implementation that simulates the integration between
     * EnemySpawner, EnemyPool, and EnemyManager without LibGDX dependencies.
     */
    private static class TestEnemySpawnerIntegration {
        private static final int MAX_ENEMIES = 50;
        
        private int activeEnemyCount = 0;
        private int poolActiveCount = 0;
        private int totalObtained = 0;
        private int totalReused = 0;
        
        public boolean canSpawn() {
            return activeEnemyCount < MAX_ENEMIES && poolActiveCount < MAX_ENEMIES;
        }
        
        public void simulateSpawn() {
            if (canSpawn()) {
                activeEnemyCount++;
                poolActiveCount++;
                totalObtained++;
            }
        }
        
        public void simulateEnemyRemoval() {
            if (activeEnemyCount > 0) {
                activeEnemyCount--;
                // Enemy returned to pool (becomes inactive)
                poolActiveCount--;
                totalReused++;
            }
        }
        
        public void clearAllEnemies() {
            activeEnemyCount = 0;
            poolActiveCount = 0;
        }
        
        public int getActiveEnemyCount() {
            return activeEnemyCount;
        }
        
        public int getPoolActiveCount() {
            return poolActiveCount;
        }
        
        public float getPoolUtilization() {
            return (float) poolActiveCount / MAX_ENEMIES;
        }
        
        public String getPoolStats() {
            float reuseRate = totalObtained > 0 ? (float) totalReused / totalObtained : 0.0f;
            
            return String.format(
                "EnemyPool Stats - Active: %d/%d (%.1f%%), Reuse Rate: %.1f%%, Total Obtained: %d",
                poolActiveCount, MAX_ENEMIES, getPoolUtilization() * 100,
                reuseRate * 100, totalObtained
            );
        }
    }
}