package com.plv.uberplayground.enemies;

import com.badlogic.gdx.utils.Array;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the EnemyManager class.
 * Tests core enemy lifecycle management functionality without LibGDX dependencies.
 */
class EnemyManagerTest {
    
    private EnemyManager enemyManager;
    private HealthSystem healthSystem;
    
    @BeforeEach
    void setUp() {
        healthSystem = new HealthSystem();
        enemyManager = new EnemyManager(healthSystem);
    }
    
    @Test
    @DisplayName("Should initialize with empty enemy list")
    void shouldInitializeWithEmptyEnemyList() {
        // Then
        assertEquals(0, enemyManager.getEnemyCount());
        assertFalse(enemyManager.isAtMaxCapacity());
        assertTrue(enemyManager.getActiveEnemies().isEmpty());
    }
    
    @Test
    @DisplayName("Should handle null enemies gracefully")
    void shouldHandleNullEnemiesGracefully() {
        // When/Then - should not throw exception
        assertDoesNotThrow(() -> {
            assertFalse(enemyManager.addEnemy(null));
            assertFalse(enemyManager.removeEnemy(null));
            assertFalse(enemyManager.isEnemyActive(null));
        });
        
        assertEquals(0, enemyManager.getEnemyCount());
    }
    
    @Test
    @DisplayName("Should track enemy count correctly")
    void shouldTrackEnemyCountCorrectly() {
        // Given
        assertEquals(0, enemyManager.getEnemyCount());
        
        // When - without actual PlayerActor instances, count should remain 0
        // Then
        assertEquals(0, enemyManager.getEnemyCount());
        assertFalse(enemyManager.isAtMaxCapacity());
    }
    
    @Test
    @DisplayName("Should report max capacity correctly")
    void shouldReportMaxCapacityCorrectly() {
        // Then
        assertEquals(SpawnConfiguration.MAX_ENEMIES, enemyManager.getMaxEnemyCount());
        assertFalse(enemyManager.isAtMaxCapacity());
    }
    
    @Test
    @DisplayName("Should clear all enemies when requested")
    void shouldClearAllEnemiesWhenRequested() {
        // Given
        assertEquals(0, enemyManager.getEnemyCount());
        
        // When
        enemyManager.clearAllEnemies();
        
        // Then
        assertEquals(0, enemyManager.getEnemyCount());
        assertTrue(enemyManager.getActiveEnemies().isEmpty());
        assertFalse(enemyManager.isAtMaxCapacity());
    }
    
    @Test
    @DisplayName("Should update all enemies without errors")
    void shouldUpdateAllEnemiesWithoutErrors() {
        // When/Then - should not throw exception
        assertDoesNotThrow(() -> {
            enemyManager.updateAll(0.016f); // 60 FPS delta
            enemyManager.updateAll(0.033f); // 30 FPS delta
            enemyManager.updateAll(0.0f);   // Zero delta
        });
        
        assertEquals(0, enemyManager.getEnemyCount());
    }
    
    @Test
    @DisplayName("Should get enemies in radius without errors")
    void shouldGetEnemiesInRadiusWithoutErrors() {
        // When
        Array<?> nearbyEnemies = enemyManager.getEnemiesInRadius(100.0f, 100.0f, 50.0f);
        
        // Then
        assertNotNull(nearbyEnemies);
        assertEquals(0, nearbyEnemies.size);
    }
    
    @Test
    @DisplayName("Should provide performance stats")
    void shouldProvidePerformanceStats() {
        // When
        String stats = enemyManager.getPerformanceStats();
        
        // Then
        assertNotNull(stats);
        assertTrue(stats.contains("Enemies: 0/" + SpawnConfiguration.MAX_ENEMIES));
        assertTrue(stats.contains("Updates/sec:"));
    }
    
    @Test
    @DisplayName("Should validate system integrity")
    void shouldValidateSystemIntegrity() {
        // When
        boolean isValid = enemyManager.validateSystemIntegrity();
        
        // Then
        assertTrue(isValid);
    }
    
    @Test
    @DisplayName("Should handle multiple clear operations")
    void shouldHandleMultipleClearOperations() {
        // When/Then - should not throw exception
        assertDoesNotThrow(() -> {
            enemyManager.clearAllEnemies();
            enemyManager.clearAllEnemies();
            enemyManager.clearAllEnemies();
        });
        
        assertEquals(0, enemyManager.getEnemyCount());
    }
    
    @Test
    @DisplayName("Should handle edge case radius searches")
    void shouldHandleEdgeCaseRadiusSearches() {
        // When/Then - should not throw exception
        assertDoesNotThrow(() -> {
            Array<?> result1 = enemyManager.getEnemiesInRadius(0.0f, 0.0f, 0.0f);
            Array<?> result2 = enemyManager.getEnemiesInRadius(-100.0f, -100.0f, 1000.0f);
            Array<?> result3 = enemyManager.getEnemiesInRadius(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
            
            assertNotNull(result1);
            assertNotNull(result2);
            assertNotNull(result3);
        });
    }
    
    @Test
    @DisplayName("Should maintain accurate count tracking during operations")
    void shouldMaintainAccurateCountTrackingDuringOperations() {
        // Given
        int initialCount = enemyManager.getEnemyCount();
        
        // When - perform multiple count queries
        int count1 = enemyManager.getEnemyCount();
        int count2 = enemyManager.getEnemyCount();
        int count3 = enemyManager.getEnemyCount();
        
        // Then - all counts should be consistent
        assertEquals(initialCount, count1);
        assertEquals(initialCount, count2);
        assertEquals(initialCount, count3);
    }
    
    @Test
    @DisplayName("Should validate enhanced system integrity")
    void shouldValidateEnhancedSystemIntegrity() {
        // When
        boolean isValid = enemyManager.validateSystemIntegrity();
        
        // Then
        assertTrue(isValid);
        
        // Additional validation after operations
        enemyManager.clearAllEnemies();
        assertTrue(enemyManager.validateSystemIntegrity());
        
        enemyManager.refreshEnemyCount();
        assertTrue(enemyManager.validateSystemIntegrity());
    }
}