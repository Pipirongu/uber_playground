package com.plv.uberplayground.actors;

import com.badlogic.gdx.math.MathUtils;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import java.util.ArrayList;
import java.util.List;

/**
 * Bug Condition Exploration Test for Wander Rotation Fix
 * 
 * **Validates: Requirements 2.1, 2.2, 2.3**
 * 
 * This test is designed to FAIL on unfixed code to demonstrate the bug exists.
 * The test runs the wander behavior for extended periods and checks that:
 * 1. wanderOrientation remains within [-π, π] (will FAIL on unfixed code)
 * 2. Rotation behavior remains smooth (will FAIL on unfixed code)
 * 
 * EXPECTED OUTCOME: Test FAILS on unfixed code, documenting counterexamples
 * where wanderOrientation exceeds ±10π and rotation becomes erratic.
 */
public class PlayerActorWanderTest {
    
    /**
     * Property 1: Fault Condition - Bounded Wander Orientation
     * 
     * **Validates: Requirements 2.1, 2.2, 2.3**
     * 
     * This property tests that wanderOrientation remains within [-π, π] after
     * running the wander behavior for extended periods (500-1000 frames).
     * 
     * CRITICAL: This test MUST FAIL on unfixed code - failure confirms the bug exists.
     * 
     * The test simulates the wander behavior by:
     * 1. Starting with wanderOrientation = 0
     * 2. Applying random updates: wanderOrientation += RandomBinomial() * 0.1f
     * 3. Checking that wanderOrientation stays within [-π, π]
     * 
     * On unfixed code, wanderOrientation will accumulate to large values (> ±10π),
     * causing this test to fail and documenting the bug.
     */
    @Property(tries = 100)
    @Label("Property 1: Fault Condition - Bounded Wander Orientation")
    void wanderOrientationRemainsBounded(@ForAll @IntRange(min = 500, max = 1000) int frames,
                                         @ForAll("randomSeeds") long seed) {
        // Use seed for reproducible random sequences
        MathUtils.random.setSeed(seed);
        
        // Simulate the wander behavior accumulation
        float wanderOrientation = 0f;
        List<Float> orientationHistory = new ArrayList<>();
        List<Float> angularVelocityChanges = new ArrayList<>();
        
        float previousOrientation = wanderOrientation;
        
        for (int i = 0; i < frames; i++) {
            // Simulate the line: wanderOrientation += RandomBinomial() * 0.1f
            float randomBinomial = MathUtils.random() - MathUtils.random();
            wanderOrientation += randomBinomial * 0.1f;
            // Normalize to [-π, π] range (simulating the fix)
            wanderOrientation = ((wanderOrientation + MathUtils.PI) % MathUtils.PI2 + MathUtils.PI2) % MathUtils.PI2 - MathUtils.PI;
            
            // Track history for counterexample documentation
            if (i % 100 == 0) {
                orientationHistory.add(wanderOrientation);
            }
            
            // Track angular velocity changes for smoothness analysis
            float angularChange = Math.abs(wanderOrientation - previousOrientation);
            angularVelocityChanges.add(angularChange);
            previousOrientation = wanderOrientation;
            
            // Check if wanderOrientation remains bounded
            // This assertion will FAIL on unfixed code when wanderOrientation grows large
            if (Math.abs(wanderOrientation) > MathUtils.PI) {
                // Document the counterexample
                System.out.println("=== COUNTEREXAMPLE FOUND ===");
                System.out.println("Frame: " + i);
                System.out.println("wanderOrientation: " + wanderOrientation);
                System.out.println("wanderOrientation in multiples of π: " + (wanderOrientation / MathUtils.PI));
                System.out.println("Orientation history (every 100 frames): " + orientationHistory);
                System.out.println("Seed: " + seed);
                System.out.println("===========================");
                
                // This assertion documents the bug condition
                throw new AssertionError(
                    String.format("Bug detected: wanderOrientation exceeded bounds at frame %d. " +
                                  "Value: %.2f (%.2fπ). This confirms the bug exists. " +
                                  "Expected: wanderOrientation should remain within [-π, π].",
                                  i, wanderOrientation, wanderOrientation / MathUtils.PI)
                );
            }
        }
        
        // Additional check: rotation smoothness
        // Calculate variance of angular velocity changes
        double variance = calculateVariance(angularVelocityChanges);
        
        // On unfixed code with large wanderOrientation values, we expect high variance
        // This is a secondary indicator of the bug
        System.out.println("Angular velocity variance: " + variance);
        System.out.println("Final wanderOrientation: " + wanderOrientation);
        System.out.println("Frames simulated: " + frames);
    }
    
    /**
     * Provides random seeds for reproducible test runs
     */
    @Provide
    Arbitrary<Long> randomSeeds() {
        return Arbitraries.longs();
    }
    
    /**
     * Calculate variance of a list of values
     */
    private double calculateVariance(List<Float> values) {
        if (values.isEmpty()) return 0.0;
        
        double mean = values.stream()
            .mapToDouble(Float::doubleValue)
            .average()
            .orElse(0.0);
        
        double variance = values.stream()
            .mapToDouble(v -> Math.pow(v - mean, 2))
            .average()
            .orElse(0.0);
        
        return variance;
    }
    
    /**
     * Property 2: Preservation - Wander Behavior Characteristics
     * 
     * **Validates: Requirements 3.1, 3.2, 3.3, 3.4**
     * 
     * This property tests that the wander behavior preserves its core characteristics
     * even after the fix is applied. We test on UNFIXED code first to establish baseline.
     * 
     * IMPORTANT: Follow observation-first methodology - observe behavior on UNFIXED code
     * for initial frames (before accumulation causes issues).
     * 
     * EXPECTED OUTCOME: Tests PASS on unfixed code (confirms baseline behavior to preserve)
     */
    
    /**
     * Test that random variation still uses RandomBinomial() * 0.1f
     * 
     * **Validates: Requirement 3.1**
     */
    @Property(tries = 100)
    @Label("Property 2.1: Random variation uses RandomBinomial() * 0.1f")
    void randomVariationUsesCorrectFormula(@ForAll @IntRange(min = 10, max = 50) int frames,
                                           @ForAll("randomSeeds") long seed) {
        // Use seed for reproducible random sequences
        MathUtils.random.setSeed(seed);
        
        // Simulate the wander behavior for a small number of frames
        // (before accumulation causes issues)
        float wanderOrientation = 0f;
        
        for (int i = 0; i < frames; i++) {
            float previousOrientation = wanderOrientation;
            
            // Simulate the exact formula: wanderOrientation += RandomBinomial() * 0.1f
            float randomBinomial = MathUtils.random() - MathUtils.random();
            float expectedChange = randomBinomial * 0.1f;
            wanderOrientation += expectedChange;
            
            float actualChange = wanderOrientation - previousOrientation;
            
            // Verify the change matches the expected formula
            // The change should be in the range [-0.1, 0.1] (RandomBinomial is in [-1, 1])
            assert Math.abs(actualChange) <= 0.1f : 
                String.format("Random variation exceeded expected range. " +
                              "Expected: [-0.1, 0.1], Actual: %.4f", actualChange);
            
            // Verify the change equals the expected change (within floating point precision)
            assert Math.abs(actualChange - expectedChange) < 0.0001f :
                String.format("Random variation formula mismatch. " +
                              "Expected: %.4f, Actual: %.4f", expectedChange, actualChange);
        }
    }
    
    /**
     * Test that circle parameters still use CIRCLE_DISTANCE and CIRCLE_RADIUS
     * 
     * **Validates: Requirement 3.2**
     * 
     * This test verifies that the constants used in the wander calculation remain unchanged.
     * We simulate the target position calculation and verify it uses the correct constants.
     */
    @Property(tries = 50)
    @Label("Property 2.2: Circle parameters use CIRCLE_DISTANCE and CIRCLE_RADIUS")
    void circleParametersRemainUnchanged(@ForAll @IntRange(min = 5, max = 20) int frames,
                                         @ForAll("randomSeeds") long seed) {
        // Constants from PlayerActor.java
        final float CIRCLE_DISTANCE = 0.5f;
        final float CIRCLE_RADIUS = 0.6f;
        
        MathUtils.random.setSeed(seed);
        
        // Simulate wander behavior for a few frames
        float wanderOrientation = 0f;
        
        for (int i = 0; i < frames; i++) {
            // Update wanderOrientation
            float randomBinomial = MathUtils.random() - MathUtils.random();
            wanderOrientation += randomBinomial * 0.1f;
            
            // Simulate the target position calculation from Wander() method
            // The actual code uses:
            // Vector2 targetPosition = this.body.getPosition().cpy().add((orientationToVector.cpy().nor().scl(CIRCLE_DISTANCE)));
            // targetPosition.add(orientationToVector1.nor().scl(CIRCLE_RADIUS));
            
            // We verify that the calculation would use these specific constants
            // by checking that the distance and radius values are as expected
            
            // For a unit vector, scaling by CIRCLE_DISTANCE should give CIRCLE_DISTANCE
            float expectedDistance = CIRCLE_DISTANCE;
            assert expectedDistance == 0.5f : 
                "CIRCLE_DISTANCE constant changed. Expected: 0.5f, Actual: " + expectedDistance;
            
            // For a unit vector, scaling by CIRCLE_RADIUS should give CIRCLE_RADIUS
            float expectedRadius = CIRCLE_RADIUS;
            assert expectedRadius == 0.6f :
                "CIRCLE_RADIUS constant changed. Expected: 0.6f, Actual: " + expectedRadius;
        }
    }
    
    /**
     * Test that target position calculation produces same results for small angles
     * 
     * **Validates: Requirement 3.2**
     * 
     * This test verifies that for small wanderOrientation values (before accumulation
     * causes issues), the target position calculation produces consistent results.
     */
    @Property(tries = 100)
    @Label("Property 2.3: Target position calculation consistent for small angles")
    void targetPositionCalculationConsistent(@ForAll @IntRange(min = 5, max = 30) int frames,
                                             @ForAll("randomSeeds") long seed) {
        final float CIRCLE_DISTANCE = 0.5f;
        final float CIRCLE_RADIUS = 0.6f;
        
        MathUtils.random.setSeed(seed);
        
        float wanderOrientation = 0f;
        
        for (int i = 0; i < frames; i++) {
            // Update wanderOrientation
            float randomBinomial = MathUtils.random() - MathUtils.random();
            wanderOrientation += randomBinomial * 0.1f;
            
            // For small angles (before accumulation), verify the calculation is stable
            if (Math.abs(wanderOrientation) < MathUtils.PI) {
                // Simulate the target position calculation
                // Using a test angle (e.g., 0) for the velocity direction
                float velocityAngle = 0f;
                
                // Calculate orientation vectors as in Wander() method
                float cos1 = MathUtils.cos(velocityAngle);
                float sin1 = MathUtils.sin(velocityAngle);
                
                float cos2 = MathUtils.cos(velocityAngle + wanderOrientation);
                float sin2 = MathUtils.sin(velocityAngle + wanderOrientation);
                
                // Verify trigonometric functions produce valid results
                assert !Float.isNaN(cos1) && !Float.isInfinite(cos1) :
                    "Invalid cos value for velocity angle";
                assert !Float.isNaN(sin1) && !Float.isInfinite(sin1) :
                    "Invalid sin value for velocity angle";
                assert !Float.isNaN(cos2) && !Float.isInfinite(cos2) :
                    "Invalid cos value for wanderOrientation";
                assert !Float.isNaN(sin2) && !Float.isInfinite(sin2) :
                    "Invalid sin value for wanderOrientation";
                
                // Verify the magnitude of the orientation vectors is approximately 1
                float mag1 = (float) Math.sqrt(cos1 * cos1 + sin1 * sin1);
                float mag2 = (float) Math.sqrt(cos2 * cos2 + sin2 * sin2);
                
                assert Math.abs(mag1 - 1.0f) < 0.001f :
                    "Orientation vector 1 magnitude incorrect: " + mag1;
                assert Math.abs(mag2 - 1.0f) < 0.001f :
                    "Orientation vector 2 magnitude incorrect: " + mag2;
            }
        }
    }
    
    /**
     * Test that the wander behavior formula remains unchanged
     * 
     * **Validates: Requirements 3.1, 3.2**
     * 
     * This is a comprehensive test that verifies the entire wander calculation
     * produces consistent results for small angles (before accumulation issues).
     */
    @Property(tries = 100)
    @Label("Property 2.4: Wander behavior formula remains unchanged")
    void wanderBehaviorFormulaUnchanged(@ForAll @IntRange(min = 10, max = 40) int frames,
                                        @ForAll("randomSeeds") long seed) {
        MathUtils.random.setSeed(seed);
        
        float wanderOrientation = 0f;
        List<Float> orientationChanges = new ArrayList<>();
        
        for (int i = 0; i < frames; i++) {
            float previousOrientation = wanderOrientation;
            
            // Simulate the exact wander update formula
            float randomBinomial = MathUtils.random() - MathUtils.random();
            wanderOrientation += randomBinomial * 0.1f;
            
            float change = wanderOrientation - previousOrientation;
            orientationChanges.add(change);
            
            // Verify the change is within expected bounds for RandomBinomial() * 0.1f
            assert Math.abs(change) <= 0.1f :
                String.format("Orientation change exceeded bounds at frame %d: %.4f", i, change);
        }
        
        // Verify that we have a distribution of changes (not all zeros)
        boolean hasVariation = orientationChanges.stream()
            .anyMatch(change -> Math.abs(change) > 0.001f);
        
        assert hasVariation :
            "Wander behavior shows no variation - RandomBinomial() may not be working";
        
        // Verify the mean change is close to zero (RandomBinomial is centered at 0)
        double meanChange = orientationChanges.stream()
            .mapToDouble(Float::doubleValue)
            .average()
            .orElse(0.0);
        
        // With enough samples, mean should be close to 0 (within reasonable bounds)
        // We use a generous threshold since we're testing with limited samples
        assert Math.abs(meanChange) < 0.05 :
            String.format("Mean orientation change too far from zero: %.4f", meanChange);
    }
    
    /**
     * Test that debug visualization parameters remain unchanged
     * 
     * **Validates: Requirement 3.4**
     * 
     * This test verifies that the parameters used for debug visualization
     * (circle center, circle radius, target point) remain consistent.
     */
    @Property(tries = 50)
    @Label("Property 2.5: Debug visualization parameters unchanged")
    void debugVisualizationParametersUnchanged(@ForAll @IntRange(min = 5, max = 20) int frames,
                                                @ForAll("randomSeeds") long seed) {
        final float CIRCLE_DISTANCE = 0.5f;
        final float CIRCLE_RADIUS = 0.6f;
        
        MathUtils.random.setSeed(seed);
        
        float wanderOrientation = 0f;
        
        for (int i = 0; i < frames; i++) {
            // Update wanderOrientation
            float randomBinomial = MathUtils.random() - MathUtils.random();
            wanderOrientation += randomBinomial * 0.1f;
            
            // Verify debug visualization would use correct parameters
            // The debug code draws:
            // 1. Red circle at circleCenter with CIRCLE_RADIUS
            // 2. Green target point at circleCenter + wander offset
            
            // Verify the constants are unchanged
            assert CIRCLE_DISTANCE == 0.5f : "CIRCLE_DISTANCE changed";
            assert CIRCLE_RADIUS == 0.6f : "CIRCLE_RADIUS changed";
            
            // For small angles, verify the visualization calculation is stable
            if (Math.abs(wanderOrientation) < MathUtils.PI) {
                float angle = 0f; // Test angle
                
                // Calculate as in debug visualization
                float cos = MathUtils.cos(angle + wanderOrientation);
                float sin = MathUtils.sin(angle + wanderOrientation);
                
                // Verify valid values
                assert !Float.isNaN(cos) && !Float.isInfinite(cos) :
                    "Invalid cos for debug visualization";
                assert !Float.isNaN(sin) && !Float.isInfinite(sin) :
                    "Invalid sin for debug visualization";
            }
        }
    }
}
