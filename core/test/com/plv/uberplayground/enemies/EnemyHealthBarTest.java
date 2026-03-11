package com.plv.uberplayground.enemies;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.math.Vector2;

/**
 * Unit tests for EnemyHealthBar class.
 * Tests health bar creation, dimensions, and basic functionality.
 */
public class EnemyHealthBarTest {
    
    private EnemyHealthBar healthBar;
    
    @BeforeEach
    public void setUp() {
        healthBar = new EnemyHealthBar();
    }
    
    @Test
    public void testHealthBarCreation() {
        // Test default constructor uses configuration values
        assertEquals(SpawnConfiguration.HEALTH_BAR_WIDTH, healthBar.getWidth());
        assertEquals(SpawnConfiguration.HEALTH_BAR_HEIGHT, healthBar.getHeight());
        assertEquals(SpawnConfiguration.HEALTH_BAR_OFFSET, healthBar.getOffsetY());
    }
    
    @Test
    public void testCustomDimensions() {
        // Test custom constructor
        EnemyHealthBar customBar = new EnemyHealthBar(50f, 8f, 25f);
        assertEquals(50f, customBar.getWidth());
        assertEquals(8f, customBar.getHeight());
        assertEquals(25f, customBar.getOffsetY());
    }
    
    @Test
    public void testReset() {
        // Test that reset method doesn't throw exceptions
        assertDoesNotThrow(() -> {
            healthBar.reset();
        });
    }
    
    @Test
    public void testForceNextRender() {
        // Test that forceNextRender method doesn't throw exceptions
        assertDoesNotThrow(() -> {
            healthBar.forceNextRender();
        });
    }
    
    @Test
    public void testHealthBarDimensions() {
        // Test that dimensions are positive
        assertTrue(healthBar.getWidth() > 0);
        assertTrue(healthBar.getHeight() > 0);
        assertTrue(healthBar.getOffsetY() > 0);
    }
    
    @Test
    public void testCustomHealthBarDimensions() {
        // Test custom dimensions
        float width = 60f;
        float height = 10f;
        float offset = 35f;
        
        EnemyHealthBar customBar = new EnemyHealthBar(width, height, offset);
        
        assertEquals(width, customBar.getWidth());
        assertEquals(height, customBar.getHeight());
        assertEquals(offset, customBar.getOffsetY());
    }
    
    @Test
    public void testHealthBarInstantiation() {
        // Test that health bar can be created without errors
        assertNotNull(healthBar);
        
        // Test multiple instances
        EnemyHealthBar bar1 = new EnemyHealthBar();
        EnemyHealthBar bar2 = new EnemyHealthBar(30f, 5f, 20f);
        
        assertNotNull(bar1);
        assertNotNull(bar2);
        assertNotSame(bar1, bar2);
    }
}