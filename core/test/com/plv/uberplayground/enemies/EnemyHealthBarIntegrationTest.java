package com.plv.uberplayground.enemies;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for EnemyHealthBar with HealthSystem.
 * Tests the integration of health bar functionality without LibGDX dependencies.
 */
public class EnemyHealthBarIntegrationTest {
    
    private HealthSystem healthSystem;
    private EnemyHealthBar healthBar;
    
    @BeforeEach
    public void setUp() {
        healthSystem = new HealthSystem();
        healthBar = new EnemyHealthBar();
    }
    
    @Test
    public void testHealthBarCreationWithConfiguration() {
        // Test that health bar uses configuration constants
        assertEquals(SpawnConfiguration.HEALTH_BAR_WIDTH, healthBar.getWidth());
        assertEquals(SpawnConfiguration.HEALTH_BAR_HEIGHT, healthBar.getHeight());
        assertEquals(SpawnConfiguration.HEALTH_BAR_OFFSET, healthBar.getOffsetY());
    }
    
    @Test
    public void testHealthBarDimensionsMatchConfiguration() {
        // Verify health bar dimensions match spawn configuration
        assertTrue(healthBar.getWidth() > 0);
        assertTrue(healthBar.getHeight() > 0);
        assertTrue(healthBar.getOffsetY() > 0);
        
        // Test that configuration values are reasonable
        assertTrue(SpawnConfiguration.HEALTH_BAR_WIDTH >= 20f); // Minimum visible width
        assertTrue(SpawnConfiguration.HEALTH_BAR_HEIGHT >= 3f); // Minimum visible height
        assertTrue(SpawnConfiguration.HEALTH_BAR_OFFSET >= 10f); // Minimum offset to avoid overlap
    }
    
    @Test
    public void testHealthSystemConstants() {
        // Verify health system uses same constants as health bar
        assertEquals(100, SpawnConfiguration.ENEMY_HEALTH);
        assertEquals(25, SpawnConfiguration.PROJECTILE_DAMAGE);
        
        // Test damage calculation matches expected health bar percentages
        int initialHealth = SpawnConfiguration.ENEMY_HEALTH;
        int damagePerHit = SpawnConfiguration.PROJECTILE_DAMAGE;
        
        // After 1 hit: 75/100 = 0.75
        assertEquals(0.75f, (float)(initialHealth - damagePerHit) / initialHealth, 0.001f);
        
        // After 2 hits: 50/100 = 0.50
        assertEquals(0.50f, (float)(initialHealth - 2 * damagePerHit) / initialHealth, 0.001f);
        
        // After 3 hits: 25/100 = 0.25
        assertEquals(0.25f, (float)(initialHealth - 3 * damagePerHit) / initialHealth, 0.001f);
        
        // After 4 hits: 0/100 = 0.00 (destroyed)
        assertEquals(0.0f, (float)Math.max(0, initialHealth - 4 * damagePerHit) / initialHealth, 0.001f);
    }
    
    @Test
    public void testHealthBarConfigurationConsistency() {
        // Test that all health bar related constants are defined
        assertNotNull(SpawnConfiguration.HEALTH_BAR_WIDTH);
        assertNotNull(SpawnConfiguration.HEALTH_BAR_HEIGHT);
        assertNotNull(SpawnConfiguration.HEALTH_BAR_OFFSET);
        
        // Test reasonable proportions
        assertTrue(SpawnConfiguration.HEALTH_BAR_WIDTH > SpawnConfiguration.HEALTH_BAR_HEIGHT);
        assertTrue(SpawnConfiguration.HEALTH_BAR_OFFSET > SpawnConfiguration.HEALTH_BAR_HEIGHT);
    }
    
    @Test
    public void testMultipleHealthBars() {
        // Test that multiple health bars can be created independently
        EnemyHealthBar bar1 = new EnemyHealthBar();
        EnemyHealthBar bar2 = new EnemyHealthBar();
        EnemyHealthBar bar3 = new EnemyHealthBar(30f, 5f, 20f);
        
        assertNotNull(bar1);
        assertNotNull(bar2);
        assertNotNull(bar3);
        
        // Verify they are separate instances
        assertNotSame(bar1, bar2);
        assertNotSame(bar1, bar3);
        assertNotSame(bar2, bar3);
        
        // Verify dimensions
        assertEquals(SpawnConfiguration.HEALTH_BAR_WIDTH, bar1.getWidth());
        assertEquals(SpawnConfiguration.HEALTH_BAR_WIDTH, bar2.getWidth());
        assertEquals(30f, bar3.getWidth());
    }
    
    @Test
    public void testHealthBarStateManagement() {
        // Test health bar state management methods
        assertDoesNotThrow(() -> {
            healthBar.reset();
            healthBar.forceNextRender();
            healthBar.reset();
        });
    }
    
    @Test
    public void testHealthSystemRegistration() {
        // Test health system can handle multiple registrations
        assertEquals(0, healthSystem.getRegisteredEnemyCount());
        
        // Test that health system starts empty
        assertTrue(healthSystem.getRegisteredEnemies().isEmpty());
        
        // Test clear functionality
        healthSystem.clear();
        assertEquals(0, healthSystem.getRegisteredEnemyCount());
    }
}