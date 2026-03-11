package com.plv.uberplayground.enemies;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.plv.uberplayground.enemies.EnhancedSpawnConfig.ConfigurationChangeListener;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test runtime configuration updates functionality.
 * Validates that configuration changes are properly validated, applied, and notified.
 */
public class RuntimeConfigurationUpdateTest {
    
    private EnhancedSpawnConfig config;
    private TestConfigurationChangeListener testListener;
    
    private static class TestConfigurationChangeListener implements ConfigurationChangeListener {
        private int changeCount = 0;
        private EnhancedSpawnConfig lastConfig = null;
        
        @Override
        public void onConfigurationChanged(EnhancedSpawnConfig config) {
            changeCount++;
            lastConfig = config;
        }
        
        public int getChangeCount() {
            return changeCount;
        }
        
        public EnhancedSpawnConfig getLastConfig() {
            return lastConfig;
        }
        
        public void reset() {
            changeCount = 0;
            lastConfig = null;
        }
    }
    
    @BeforeEach
    void setUp() {
        config = new EnhancedSpawnConfig();
        config.applyDefaults();
        testListener = new TestConfigurationChangeListener();
        config.addConfigurationChangeListener(testListener);
    }
    
    @Test
    void testConfigurationChangeNotification() {
        // When: Configuration value is changed
        config.setBaseSpawnRate(0.5f);
        
        // Then: Listener should be notified
        assertEquals(1, testListener.getChangeCount());
        assertEquals(config, testListener.getLastConfig());
        assertEquals(0.5f, config.getBaseSpawnRate(), 0.001f);
    }
    
    @Test
    void testMultipleConfigurationUpdates() {
        // When: Multiple configuration values are updated at once
        config.updateConfiguration(0.3f, 1.5f, 75, 1.2f, 90.0f, EnhancedSpawnConfig.ScalingType.EXPONENTIAL);
        
        // Then: Listener should be notified only once
        assertEquals(1, testListener.getChangeCount());
        
        // And: All values should be updated
        assertEquals(0.3f, config.getBaseSpawnRate(), 0.001f);
        assertEquals(1.5f, config.getMaxSpawnRate(), 0.001f);
        assertEquals(75, config.getMaxEnemies());
        assertEquals(1.2f, config.getScalingFactor(), 0.001f);
        assertEquals(90.0f, config.getTargetDuration(), 0.001f);
        assertEquals(EnhancedSpawnConfig.ScalingType.EXPONENTIAL, config.getScalingType());
    }
    
    @Test
    void testInvalidConfigurationRejection() {
        float originalBaseRate = config.getBaseSpawnRate();
        
        // When: Invalid configuration value is set
        config.setBaseSpawnRate(-0.1f);
        
        // Then: Value should not be changed
        assertEquals(originalBaseRate, config.getBaseSpawnRate(), 0.001f);
        
        // And: Listener should not be notified
        assertEquals(0, testListener.getChangeCount());
    }
    
    @Test
    void testMaxSpawnRateValidation() {
        config.setBaseSpawnRate(0.5f);
        testListener.reset(); // Reset listener after initial change
        
        float originalMaxRate = config.getMaxSpawnRate();
        
        // When: Max spawn rate is set lower than base rate
        config.setMaxSpawnRate(0.3f); // Lower than base rate of 0.5f
        
        // Then: Value should not be changed
        assertEquals(originalMaxRate, config.getMaxSpawnRate(), 0.001f);
        
        // And: Listener should not be notified
        assertEquals(0, testListener.getChangeCount());
    }
    
    @Test
    void testListenerManagement() {
        TestConfigurationChangeListener secondListener = new TestConfigurationChangeListener();
        
        // When: Second listener is added
        config.addConfigurationChangeListener(secondListener);
        config.setTargetDuration(120.0f);
        
        // Then: Both listeners should be notified
        assertEquals(1, testListener.getChangeCount());
        assertEquals(1, secondListener.getChangeCount());
        
        // When: First listener is removed
        config.removeConfigurationChangeListener(testListener);
        testListener.reset();
        secondListener.reset();
        config.setTargetDuration(180.0f);
        
        // Then: Only second listener should be notified
        assertEquals(0, testListener.getChangeCount());
        assertEquals(1, secondListener.getChangeCount());
    }
    
    @Test
    void testNoNotificationForSameValue() {
        // When: Same value is set
        float currentRate = config.getBaseSpawnRate();
        config.setBaseSpawnRate(currentRate);
        
        // Then: Listener should not be notified
        assertEquals(0, testListener.getChangeCount());
    }
    
    @Test
    void testPerformanceReductionFactorValidation() {
        float originalFactor = config.getPerformanceReductionFactor();
        
        // Test invalid values
        config.setPerformanceReductionFactor(-0.1f); // Negative
        assertEquals(originalFactor, config.getPerformanceReductionFactor(), 0.001f);
        
        config.setPerformanceReductionFactor(1.5f); // Greater than 1
        assertEquals(originalFactor, config.getPerformanceReductionFactor(), 0.001f);
        
        config.setPerformanceReductionFactor(0.0f); // Zero
        assertEquals(originalFactor, config.getPerformanceReductionFactor(), 0.001f);
        
        // Test valid value
        config.setPerformanceReductionFactor(0.7f);
        assertEquals(0.7f, config.getPerformanceReductionFactor(), 0.001f);
        
        assertEquals(1, testListener.getChangeCount());
    }
}