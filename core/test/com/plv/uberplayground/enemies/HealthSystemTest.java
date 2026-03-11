package com.plv.uberplayground.enemies;

import com.badlogic.gdx.utils.Array;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the HealthSystem class.
 * Tests core health management functionality without LibGDX dependencies.
 */
class HealthSystemTest {
    
    private HealthSystem healthSystem;
    
    @BeforeEach
    void setUp() {
        healthSystem = new HealthSystem();
    }
    
    @Test
    @DisplayName("Should handle null enemies gracefully")
    void shouldHandleNullEnemiesGracefully() {
        // When/Then - should not throw exception
        assertDoesNotThrow(() -> {
            healthSystem.initializeHealth(null);
            healthSystem.initializeHealth(null, 100);
            
            boolean result1 = healthSystem.takeDamage(null, null, 1.0f);
            boolean result2 = healthSystem.takeDamage(null, 25, 1.0f);
            
            assertFalse(result1);
            assertFalse(result2);
            
            assertFalse(healthSystem.isAlive(null));
            assertEquals(0.0f, healthSystem.getHealthPercentage(null), 0.001f);
            assertEquals(0, healthSystem.getCurrentHealth(null));
            assertEquals(0, healthSystem.getMaxHealth(null));
            
            healthSystem.removeEnemy(null);
            healthSystem.resetHealth(null);
        });
    }
    
    @Test
    @DisplayName("Should track registered enemy count correctly")
    void shouldTrackRegisteredEnemyCountCorrectly() {
        // Given
        assertEquals(0, healthSystem.getRegisteredEnemyCount());
        
        // When/Then - without actual enemies, count should remain 0
        assertEquals(0, healthSystem.getRegisteredEnemyCount());
    }
    
    @Test
    @DisplayName("Should clear all enemies when requested")
    void shouldClearAllEnemiesWhenRequested() {
        // Given
        assertEquals(0, healthSystem.getRegisteredEnemyCount());
        
        // When
        healthSystem.clear();
        
        // Then
        assertEquals(0, healthSystem.getRegisteredEnemyCount());
    }
    
    @Test
    @DisplayName("Should process removals without errors")
    void shouldProcessRemovalsWithoutErrors() {
        // When
        Array<?> removedEnemies = healthSystem.processRemovals();
        
        // Then
        assertNotNull(removedEnemies);
        assertEquals(0, removedEnemies.size);
    }
    
    @Test
    @DisplayName("Should get registered enemies without errors")
    void shouldGetRegisteredEnemiesWithoutErrors() {
        // When
        Array<?> enemies = healthSystem.getRegisteredEnemies();
        
        // Then
        assertNotNull(enemies);
        assertEquals(0, enemies.size);
    }
    
    @Test
    @DisplayName("Should handle multiple clear operations")
    void shouldHandleMultipleClearOperations() {
        // When/Then - should not throw exception
        assertDoesNotThrow(() -> {
            healthSystem.clear();
            healthSystem.clear();
            healthSystem.clear();
        });
        
        assertEquals(0, healthSystem.getRegisteredEnemyCount());
    }
}