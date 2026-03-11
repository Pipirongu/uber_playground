package com.plv.uberplayground.enemies;

import net.jqwik.api.*;
import net.jqwik.api.constraints.FloatRange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ScalingCalculator class.
 * Tests mathematical scaling functions and bounds enforcement.
 */
public class ScalingCalculatorTest {
    
    private EnhancedSpawnConfig config;
    private ScalingCalculator calculator;
    
    @BeforeEach
    void setUp() {
        config = new EnhancedSpawnConfig();
        config.applyDefaults(); // baseSpawnRate=0.2, maxSpawnRate=1.0, scalingFactor=1.0, targetDuration=60.0
        calculator = new ScalingCalculator(config);
    }
    
    @Test
    void testLinearScalingAtGameStart() {
        config.setScalingType(EnhancedSpawnConfig.ScalingType.LINEAR);
        
        float spawnRate = calculator.calculateSpawnRate(0.0f);
        assertEquals(0.2f, spawnRate, 0.001f, "Spawn rate should equal base rate at game start");
    }
    
    @Test
    void testLinearScalingAt60Seconds() {
        config.setScalingType(EnhancedSpawnConfig.ScalingType.LINEAR);
        
        // At 60 seconds with linear scaling, should double the base rate
        // Formula: 0.2 * (1 + 1.0 * (60/60)) = 0.2 * (1 + 1.0) = 0.4
        float spawnRate = calculator.calculateSpawnRate(60.0f);
        assertEquals(0.4f, spawnRate, 0.001f, "Spawn rate should double at target duration");
    }
    
    @Test
    void testExponentialScaling() {
        config.setScalingType(EnhancedSpawnConfig.ScalingType.EXPONENTIAL);
        
        float startRate = calculator.calculateSpawnRate(0.0f);
        float midRate = calculator.calculateSpawnRate(30.0f);
        float endRate = calculator.calculateSpawnRate(60.0f);
        
        assertEquals(0.2f, startRate, 0.001f, "Exponential should start at base rate");
        assertTrue(midRate > startRate, "Exponential should increase over time");
        assertTrue(endRate > midRate, "Exponential should accelerate");
    }
    
    @Test
    void testLogarithmicScaling() {
        config.setScalingType(EnhancedSpawnConfig.ScalingType.LOGARITHMIC);
        
        float startRate = calculator.calculateSpawnRate(0.0f);
        float midRate = calculator.calculateSpawnRate(30.0f);
        float endRate = calculator.calculateSpawnRate(60.0f);
        
        assertEquals(0.2f, startRate, 0.001f, "Logarithmic should start at base rate");
        assertTrue(midRate > startRate, "Logarithmic should increase over time");
        assertTrue(endRate > midRate, "Logarithmic should continue increasing");
        
        // Logarithmic should have diminishing returns
        float earlyIncrease = midRate - startRate;
        float lateIncrease = endRate - midRate;
        assertTrue(earlyIncrease > lateIncrease, "Logarithmic should have diminishing returns");
    }
    
    @Test
    void testMaxSpawnRateBounds() {
        config.setMaxSpawnRate(0.3f); // Set a low max rate
        config.setScalingType(EnhancedSpawnConfig.ScalingType.LINEAR);
        
        // At 120 seconds, linear scaling would give 0.2 * (1 + 1.0 * (120/60)) = 0.6
        // But should be capped at 0.3
        float spawnRate = calculator.calculateSpawnRate(120.0f);
        assertEquals(0.3f, spawnRate, 0.001f, "Spawn rate should be capped at maximum");
    }
    
    @Test
    void testNegativeGameDuration() {
        float spawnRate = calculator.calculateSpawnRate(-10.0f);
        assertEquals(0.2f, spawnRate, 0.001f, "Negative duration should return base rate");
    }
    
    @Test
    void testScalingFactorCalculation() {
        config.setScalingType(EnhancedSpawnConfig.ScalingType.LINEAR);
        
        float factor0 = calculator.calculateScalingFactor(0.0f);
        float factor30 = calculator.calculateScalingFactor(30.0f);
        float factor60 = calculator.calculateScalingFactor(60.0f);
        
        assertEquals(0.0f, factor0, 0.001f, "Scaling factor should be 0 at start");
        assertEquals(0.5f, factor30, 0.001f, "Scaling factor should be 0.5 at half duration");
        assertEquals(1.0f, factor60, 0.001f, "Scaling factor should be 1.0 at target duration");
    }
    
    @Test
    void testPerformanceLimitsValidation() {
        assertTrue(calculator.isWithinPerformanceLimits(0.5f), "Valid rate should pass limits check");
        assertTrue(calculator.isWithinPerformanceLimits(1.0f), "Max rate should pass limits check");
        assertFalse(calculator.isWithinPerformanceLimits(1.5f), "Rate above max should fail limits check");
        assertFalse(calculator.isWithinPerformanceLimits(-0.1f), "Negative rate should fail limits check");
    }
    
    @Test
    void testScalingTypeChange() {
        calculator.setScalingType(EnhancedSpawnConfig.ScalingType.EXPONENTIAL);
        assertEquals(EnhancedSpawnConfig.ScalingType.EXPONENTIAL, calculator.getScalingType());
        
        calculator.setScalingType(EnhancedSpawnConfig.ScalingType.LOGARITHMIC);
        assertEquals(EnhancedSpawnConfig.ScalingType.LOGARITHMIC, calculator.getScalingType());
    }
    
    @Test
    void testConfigurationAccess() {
        assertSame(config, calculator.getConfig(), "Should return the same config instance");
    }
    
    @Test
    void testMonotonicityForLinearScaling() {
        config.setScalingType(EnhancedSpawnConfig.ScalingType.LINEAR);
        
        float rate1 = calculator.calculateSpawnRate(10.0f);
        float rate2 = calculator.calculateSpawnRate(20.0f);
        float rate3 = calculator.calculateSpawnRate(30.0f);
        
        assertTrue(rate2 >= rate1, "Linear scaling should be monotonic");
        assertTrue(rate3 >= rate2, "Linear scaling should be monotonic");
    }
    
    @Test
    void testDeterministicCalculation() {
        config.setScalingType(EnhancedSpawnConfig.ScalingType.EXPONENTIAL);
        
        float rate1 = calculator.calculateSpawnRate(45.0f);
        float rate2 = calculator.calculateSpawnRate(45.0f);
        
        assertEquals(rate1, rate2, 0.0001f, "Same input should produce identical output");
    }
    
    /**
     * Property 1: Scaling Function Monotonicity and Determinism
     * 
     * **Validates: Requirements 1.2, 1.3**
     * 
     * This property test validates that for any two game durations where duration1 < duration2,
     * the calculated spawn rate at duration2 should be greater than or equal to the spawn rate
     * at duration1, and calling the scaling calculation multiple times with the same duration
     * should always produce identical results.
     * 
     * The monotonicity property ensures that spawn rates never decrease as game time progresses,
     * which is essential for maintaining progressive difficulty scaling. The determinism property
     * ensures consistent behavior across multiple calculations with the same input.
     */
    @Property(tries = 100)
    @Label("Property 1: Scaling Function Monotonicity and Determinism")
    void scalingFunctionIsMonotonicAndDeterministic(
            @ForAll @FloatRange(min = 0.0f, max = 300.0f) float duration1,
            @ForAll @FloatRange(min = 0.0f, max = 300.0f) float duration2,
            @ForAll("scalingTypes") EnhancedSpawnConfig.ScalingType scalingType) {
        
        // Create fresh configuration for this property test
        EnhancedSpawnConfig testConfig = new EnhancedSpawnConfig();
        testConfig.applyDefaults();
        testConfig.setScalingType(scalingType);
        
        ScalingCalculator testCalculator = new ScalingCalculator(testConfig);
        
        // Test monotonicity: if duration1 < duration2, then rate1 <= rate2
        if (duration1 < duration2) {
            float rate1 = testCalculator.calculateSpawnRate(duration1);
            float rate2 = testCalculator.calculateSpawnRate(duration2);
            
            assertTrue(rate2 >= rate1, 
                String.format("Monotonicity violated: rate at %.2fs (%.4f) > rate at %.2fs (%.4f) for %s scaling",
                    duration1, rate1, duration2, rate2, scalingType));
        }
        
        // Test determinism: same input should always produce same output
        float rate1 = testCalculator.calculateSpawnRate(duration1);
        float rate2 = testCalculator.calculateSpawnRate(duration1);
        
        assertEquals(rate1, rate2, 0.0001f,
            String.format("Determinism violated: same duration %.2fs produced different rates %.6f vs %.6f for %s scaling",
                duration1, rate1, rate2, scalingType));
        
        // Additional determinism test for duration2
        float rate3 = testCalculator.calculateSpawnRate(duration2);
        float rate4 = testCalculator.calculateSpawnRate(duration2);
        
        assertEquals(rate3, rate4, 0.0001f,
            String.format("Determinism violated: same duration %.2fs produced different rates %.6f vs %.6f for %s scaling",
                duration2, rate3, rate4, scalingType));
    }
    
    /**
     * Provider for scaling types used in property tests.
     */
    @Provide
    Arbitrary<EnhancedSpawnConfig.ScalingType> scalingTypes() {
        return Arbitraries.of(
            EnhancedSpawnConfig.ScalingType.LINEAR,
            EnhancedSpawnConfig.ScalingType.EXPONENTIAL,
            EnhancedSpawnConfig.ScalingType.LOGARITHMIC
        );
    }

    /**
     * Property 2: Spawn Rate Bounds Enforcement
     *
     * **Validates: Requirements 1.5**
     *
     * This property test validates that calculated spawn rates never exceed the configured
     * maximum threshold, regardless of game duration, scaling type, or configuration parameters.
     *
     * The bounds enforcement property ensures that the spawning system maintains performance
     * and stability by preventing spawn rates from exceeding safe operational limits, even
     * during extended gameplay sessions or with aggressive scaling configurations.
     */
    @Property(tries = 100)
    @Label("Property 2: Spawn Rate Bounds Enforcement")
    void spawnRateBoundsEnforcement(
            @ForAll @FloatRange(min = 0.0f, max = 600.0f) float gameDuration,
            @ForAll @FloatRange(min = 0.1f, max = 0.5f) float baseSpawnRate,
            @ForAll @FloatRange(min = 0.5f, max = 2.0f) float maxSpawnRate,
            @ForAll @FloatRange(min = 0.5f, max = 3.0f) float scalingFactor,
            @ForAll @FloatRange(min = 30.0f, max = 120.0f) float targetDuration,
            @ForAll("scalingTypes") EnhancedSpawnConfig.ScalingType scalingType) {

        // Ensure maxSpawnRate is greater than baseSpawnRate for valid configuration
        Assume.that(maxSpawnRate > baseSpawnRate);

        // Create fresh configuration for this property test
        EnhancedSpawnConfig testConfig = new EnhancedSpawnConfig();
        testConfig.setBaseSpawnRate(baseSpawnRate);
        testConfig.setMaxSpawnRate(maxSpawnRate);
        testConfig.setScalingFactor(scalingFactor);
        testConfig.setTargetDuration(targetDuration);
        testConfig.setScalingType(scalingType);

        ScalingCalculator testCalculator = new ScalingCalculator(testConfig);

        // Calculate spawn rate for the given game duration
        float calculatedSpawnRate = testCalculator.calculateSpawnRate(gameDuration);

        // Verify that the calculated spawn rate never exceeds the maximum threshold
        assertTrue(calculatedSpawnRate <= maxSpawnRate,
            String.format("Spawn rate bounds violated: calculated rate %.6f exceeds maximum %.6f " +
                         "for duration %.2fs, base %.3f, scaling %.3f, target %.1fs, type %s",
                calculatedSpawnRate, maxSpawnRate, gameDuration, baseSpawnRate,
                scalingFactor, targetDuration, scalingType));

        // Verify that the calculated spawn rate is non-negative
        assertTrue(calculatedSpawnRate >= 0.0f,
            String.format("Spawn rate should be non-negative: got %.6f for duration %.2fs, " +
                         "base %.3f, max %.3f, scaling %.3f, target %.1fs, type %s",
                calculatedSpawnRate, gameDuration, baseSpawnRate, maxSpawnRate,
                scalingFactor, targetDuration, scalingType));

        // Verify that the calculated spawn rate is at least the base rate for positive durations
        if (gameDuration >= 0.0f) {
            assertTrue(calculatedSpawnRate >= baseSpawnRate,
                String.format("Spawn rate should be at least base rate: got %.6f, expected >= %.6f " +
                             "for duration %.2fs, scaling %.3f, target %.1fs, type %s",
                    calculatedSpawnRate, baseSpawnRate, gameDuration,
                    scalingFactor, targetDuration, scalingType));
        }

        // Additional verification: test isWithinPerformanceLimits method consistency
        assertTrue(testCalculator.isWithinPerformanceLimits(calculatedSpawnRate),
            String.format("Performance limits check failed for calculated rate %.6f " +
                         "with max %.6f for duration %.2fs, type %s",
                calculatedSpawnRate, maxSpawnRate, gameDuration, scalingType));
    }
}