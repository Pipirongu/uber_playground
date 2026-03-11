package com.plv.uberplayground.enemies;

import net.jqwik.api.*;
import net.jqwik.api.constraints.FloatRange;
import net.jqwik.api.constraints.IntRange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EnhancedSpawnConfig class.
 * Tests configuration validation and fallback behavior.
 * Note: File loading tests are excluded as they require LibGDX runtime.
 */
class EnhancedSpawnConfigTest {
    
    private EnhancedSpawnConfig config;
    
    @BeforeEach
    void setUp() {
        config = new EnhancedSpawnConfig();
    }
    
    @Test
    void testDefaultConfiguration() {
        config.applyDefaults();
        
        assertEquals(0.2f, config.getBaseSpawnRate(), 0.001f);
        assertEquals(1.0f, config.getMaxSpawnRate(), 0.001f);
        assertEquals(50, config.getMaxEnemies());
        assertEquals(1.0f, config.getScalingFactor(), 0.001f);
        assertEquals(60.0f, config.getTargetDuration(), 0.001f);
        assertEquals(EnhancedSpawnConfig.ScalingType.LINEAR, config.getScalingType());
        assertEquals(30.0f, config.getMinFrameRate(), 0.001f);
        assertEquals(0.8f, config.getPerformanceReductionFactor(), 0.001f);
        assertTrue(config.isPreserveScalingOnPause());
        assertTrue(config.isResetOnGameOver());
        assertTrue(config.isResetOnNewGame());
        assertTrue(config.isConfigurationValid());
    }
    
    @Test
    void testValidationWithValidValues() {
        config.applyDefaults();
        config.setBaseSpawnRate(0.5f);
        config.setMaxSpawnRate(2.0f);
        config.setMaxEnemies(100);
        config.setScalingFactor(1.5f);
        config.setTargetDuration(120.0f);
        config.setMinFrameRate(60.0f);
        config.setPerformanceReductionFactor(0.9f);
        
        assertTrue(config.isConfigurationValid());
    }
    
    @Test
    void testScalingTypeEnumHandling() {
        config.setScalingType(EnhancedSpawnConfig.ScalingType.EXPONENTIAL);
        assertEquals(EnhancedSpawnConfig.ScalingType.EXPONENTIAL, config.getScalingType());
        
        config.setScalingType(EnhancedSpawnConfig.ScalingType.LOGARITHMIC);
        assertEquals(EnhancedSpawnConfig.ScalingType.LOGARITHMIC, config.getScalingType());
        
        config.setScalingType(EnhancedSpawnConfig.ScalingType.LINEAR);
        assertEquals(EnhancedSpawnConfig.ScalingType.LINEAR, config.getScalingType());
    }
    
    @Test
    void testGameStateConfigurationSetters() {
        config.setPreserveScalingOnPause(false);
        assertFalse(config.isPreserveScalingOnPause());
        
        config.setResetOnGameOver(false);
        assertFalse(config.isResetOnGameOver());
        
        config.setResetOnNewGame(false);
        assertFalse(config.isResetOnNewGame());
    }
    
    @Test
    void testBasicGettersAndSetters() {
        config.setBaseSpawnRate(0.3f);
        assertEquals(0.3f, config.getBaseSpawnRate(), 0.001f);
        
        config.setMaxSpawnRate(1.5f);
        assertEquals(1.5f, config.getMaxSpawnRate(), 0.001f);
        
        config.setMaxEnemies(75);
        assertEquals(75, config.getMaxEnemies());
        
        config.setScalingFactor(2.0f);
        assertEquals(2.0f, config.getScalingFactor(), 0.001f);
        
        config.setTargetDuration(90.0f);
        assertEquals(90.0f, config.getTargetDuration(), 0.001f);
        
        config.setMinFrameRate(45.0f);
        assertEquals(45.0f, config.getMinFrameRate(), 0.001f);
        
        config.setPerformanceReductionFactor(0.7f);
        assertEquals(0.7f, config.getPerformanceReductionFactor(), 0.001f);
    }
    
    /**
     * Property 5: Configuration Loading Completeness
     * 
     * **Validates: Requirements 3.1, 3.2, 3.3**
     * 
     * For any valid configuration parameters (base spawn rate, maximum spawn rate, 
     * and scaling factor), all values should be correctly applied to the spawning system
     * when set through the configuration interface.
     * 
     * This property test validates that the configuration system correctly handles
     * and preserves all required spawn configuration parameters across the valid
     * input ranges, ensuring completeness of configuration loading functionality.
     */
    @Property(tries = 100)
    @Label("Property 5: Configuration Loading Completeness")
    void configurationLoadingCompleteness(
            @ForAll @FloatRange(min = 0.1f, max = 2.0f) float baseSpawnRate,
            @ForAll @FloatRange(min = 1.1f, max = 5.0f) float maxSpawnRate, // Avoid default 1.0f
            @ForAll @FloatRange(min = 0.1f, max = 3.0f) float scalingFactor,
            @ForAll @IntRange(min = 10, max = 100) int maxEnemies,
            @ForAll @FloatRange(min = 30.0f, max = 300.0f) float targetDuration) {
        
        // Ensure maxSpawnRate is greater than baseSpawnRate for valid configuration
        Assume.that(maxSpawnRate > baseSpawnRate);
        // Avoid testing with default values to ensure we can verify they changed
        Assume.that(Math.abs(baseSpawnRate - 0.2f) > 0.001f); // Default is 0.2f
        Assume.that(Math.abs(scalingFactor - 1.0f) > 0.001f); // Default is 1.0f
        Assume.that(maxEnemies != 50); // Default is 50
        Assume.that(Math.abs(targetDuration - 60.0f) > 0.001f); // Default is 60.0f
        
        // Initialize configuration for this property test
        EnhancedSpawnConfig testConfig = new EnhancedSpawnConfig();
        
        // Apply configuration parameters using the configuration interface
        testConfig.applyDefaults(); // Start with known defaults
        testConfig.setBaseSpawnRate(baseSpawnRate);
        testConfig.setMaxSpawnRate(maxSpawnRate);
        testConfig.setScalingFactor(scalingFactor);
        testConfig.setMaxEnemies(maxEnemies);
        testConfig.setTargetDuration(targetDuration);
        
        // Verify all configuration parameters are correctly loaded and applied
        // Requirement 3.1: Base spawn rate is correctly loaded
        assertEquals(baseSpawnRate, testConfig.getBaseSpawnRate(), 0.001f,
                "Base spawn rate should be correctly applied");
        
        // Requirement 3.2: Maximum spawn rate is correctly loaded  
        assertEquals(maxSpawnRate, testConfig.getMaxSpawnRate(), 0.001f,
                "Maximum spawn rate should be correctly applied");
        
        // Requirement 3.3: Scaling factor parameters are correctly loaded
        assertEquals(scalingFactor, testConfig.getScalingFactor(), 0.001f,
                "Scaling factor should be correctly applied");
        
        // Additional completeness checks for related configuration parameters
        assertEquals(maxEnemies, testConfig.getMaxEnemies(),
                "Max enemies should be correctly applied");
        assertEquals(targetDuration, testConfig.getTargetDuration(), 0.001f,
                "Target duration should be correctly applied");
        
        // Verify configuration remains valid after all parameters are set
        assertTrue(testConfig.isConfigurationValid(),
                "Configuration should remain valid with all valid parameters");
        
        // Verify configuration completeness - all required parameters should be non-default
        // when explicitly set (ensuring no parameter is lost or reset)
        assertNotEquals(0.2f, testConfig.getBaseSpawnRate(), 0.001f,
                "Base spawn rate should not revert to default");
        assertNotEquals(1.0f, testConfig.getMaxSpawnRate(), 0.001f,
                "Max spawn rate should not revert to default");
        assertNotEquals(1.0f, testConfig.getScalingFactor(), 0.001f,
                "Scaling factor should not revert to default");
        assertNotEquals(50, testConfig.getMaxEnemies(),
                "Max enemies should not revert to default");
        assertNotEquals(60.0f, testConfig.getTargetDuration(), 0.001f,
                "Target duration should not revert to default");
    }

    /**
     * Property 6: Configuration Error Handling
     *
     * **Validates: Requirements 3.4**
     *
     * For any invalid or missing configuration values, the system should fall back
     * to safe default values and continue operating without errors.
     *
     * This property test validates that the EnhancedSpawnConfig class properly handles
     * invalid configuration values by applying safe defaults and maintaining system
     * stability, ensuring the spawning system remains functional even with corrupted
     * or incomplete configuration data.
     */
    @Property(tries = 100)
    @Label("Property 6: Configuration Error Handling")
    void configurationErrorHandling(
            @ForAll("invalidSpawnRates") float invalidBaseSpawnRate,
            @ForAll("invalidSpawnRates") float invalidMaxSpawnRate,
            @ForAll("invalidEnemyCounts") int invalidMaxEnemies,
            @ForAll("invalidScalingFactors") float invalidScalingFactor,
            @ForAll("invalidDurations") float invalidTargetDuration,
            @ForAll("invalidFrameRates") float invalidMinFrameRate,
            @ForAll("invalidReductionFactors") float invalidPerformanceReductionFactor) {

        // Initialize configuration for this property test
        EnhancedSpawnConfig testConfig = new EnhancedSpawnConfig();

        // Apply invalid configuration values
        testConfig.setBaseSpawnRate(invalidBaseSpawnRate);
        testConfig.setMaxSpawnRate(invalidMaxSpawnRate);
        testConfig.setMaxEnemies(invalidMaxEnemies);
        testConfig.setScalingFactor(invalidScalingFactor);
        testConfig.setTargetDuration(invalidTargetDuration);
        testConfig.setMinFrameRate(invalidMinFrameRate);
        testConfig.setPerformanceReductionFactor(invalidPerformanceReductionFactor);

        // Trigger validation which should apply safe defaults for invalid values
        testConfig.validateConfiguration();

        // Requirement 3.4: System should use safe default values for invalid/missing configuration

        // Verify base spawn rate falls back to safe default (0.2f) for invalid values
        if (invalidBaseSpawnRate <= 0) {
            assertEquals(0.2f, testConfig.getBaseSpawnRate(), 0.001f,
                    "Invalid base spawn rate should fall back to safe default");
        }

        // Verify max spawn rate is corrected to be greater than base spawn rate
        assertTrue(testConfig.getMaxSpawnRate() > testConfig.getBaseSpawnRate(),
                "Max spawn rate should be greater than base spawn rate after validation");

        // Verify max enemies falls back to safe default (50) for invalid values
        if (invalidMaxEnemies <= 0) {
            assertEquals(50, testConfig.getMaxEnemies(),
                    "Invalid max enemies should fall back to safe default");
        }

        // Verify scaling factor falls back to safe default (1.0f) for invalid values
        if (invalidScalingFactor < 0) {
            assertEquals(1.0f, testConfig.getScalingFactor(), 0.001f,
                    "Invalid scaling factor should fall back to safe default");
        }

        // Verify target duration falls back to safe default (60.0f) for invalid values
        if (invalidTargetDuration <= 0) {
            assertEquals(60.0f, testConfig.getTargetDuration(), 0.001f,
                    "Invalid target duration should fall back to safe default");
        }

        // Verify min frame rate falls back to safe default (30.0f) for invalid values
        if (invalidMinFrameRate <= 0) {
            assertEquals(30.0f, testConfig.getMinFrameRate(), 0.001f,
                    "Invalid min frame rate should fall back to safe default");
        }

        // Verify performance reduction factor falls back to safe default (0.8f) for invalid values
        if (invalidPerformanceReductionFactor <= 0 || invalidPerformanceReductionFactor > 1.0f) {
            assertEquals(0.8f, testConfig.getPerformanceReductionFactor(), 0.001f,
                    "Invalid performance reduction factor should fall back to safe default");
        }

        // Verify all configuration values are within safe operational ranges after validation
        assertTrue(testConfig.getBaseSpawnRate() > 0,
                "Base spawn rate should be positive after validation");
        assertTrue(testConfig.getMaxSpawnRate() > testConfig.getBaseSpawnRate(),
                "Max spawn rate should be greater than base spawn rate after validation");
        assertTrue(testConfig.getMaxEnemies() > 0,
                "Max enemies should be positive after validation");
        assertTrue(testConfig.getScalingFactor() >= 0,
                "Scaling factor should be non-negative after validation");
        assertTrue(testConfig.getTargetDuration() > 0,
                "Target duration should be positive after validation");
        assertTrue(testConfig.getMinFrameRate() > 0,
                "Min frame rate should be positive after validation");
        assertTrue(testConfig.getPerformanceReductionFactor() > 0 &&
                  testConfig.getPerformanceReductionFactor() <= 1.0f,
                "Performance reduction factor should be between 0 and 1 after validation");

        // Verify system continues operating without errors (no exceptions thrown)
        // The fact that we reach this point means no exceptions were thrown during validation
        assertDoesNotThrow(() -> {
            testConfig.getBaseSpawnRate();
            testConfig.getMaxSpawnRate();
            testConfig.getMaxEnemies();
            testConfig.getScalingFactor();
            testConfig.getTargetDuration();
            testConfig.getMinFrameRate();
            testConfig.getPerformanceReductionFactor();
        }, "Configuration should remain accessible without errors after validation");
    }

    // Generators for invalid configuration values

    @Provide
    Arbitrary<Float> invalidSpawnRates() {
        return Arbitraries.oneOf(
                Arbitraries.floats().between(-10.0f, 0.0f), // Negative and zero values
                Arbitraries.just(Float.NaN),                // NaN values
                Arbitraries.just(Float.NEGATIVE_INFINITY),  // Negative infinity
                Arbitraries.just(Float.POSITIVE_INFINITY)   // Positive infinity
        );
    }

    @Provide
    Arbitrary<Integer> invalidEnemyCounts() {
        return Arbitraries.oneOf(
                Arbitraries.integers().between(-100, 0),    // Negative and zero values
                Arbitraries.just(Integer.MIN_VALUE),        // Extreme negative value
                Arbitraries.just(-1)                        // Common invalid value
        );
    }

    @Provide
    Arbitrary<Float> invalidScalingFactors() {
        return Arbitraries.oneOf(
                Arbitraries.floats().between(-10.0f, -0.1f), // Negative values
                Arbitraries.just(Float.NaN),                 // NaN values
                Arbitraries.just(Float.NEGATIVE_INFINITY)    // Negative infinity
        );
    }

    @Provide
    Arbitrary<Float> invalidDurations() {
        return Arbitraries.oneOf(
                Arbitraries.floats().between(-100.0f, 0.0f), // Negative and zero values
                Arbitraries.just(Float.NaN),                 // NaN values
                Arbitraries.just(Float.NEGATIVE_INFINITY)    // Negative infinity
        );
    }

    @Provide
    Arbitrary<Float> invalidFrameRates() {
        return Arbitraries.oneOf(
                Arbitraries.floats().between(-60.0f, 0.0f), // Negative and zero values
                Arbitraries.just(Float.NaN),                // NaN values
                Arbitraries.just(Float.NEGATIVE_INFINITY)   // Negative infinity
        );
    }

    @Provide
    Arbitrary<Float> invalidReductionFactors() {
        return Arbitraries.oneOf(
                Arbitraries.floats().between(-1.0f, 0.0f),  // Negative and zero values
                Arbitraries.floats().between(1.1f, 10.0f),  // Values greater than 1.0
                Arbitraries.just(Float.NaN),                // NaN values
                Arbitraries.just(Float.NEGATIVE_INFINITY),  // Negative infinity
                Arbitraries.just(Float.POSITIVE_INFINITY)   // Positive infinity
        );
    }
}