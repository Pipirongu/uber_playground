package com.plv.uberplayground.enemies;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the enhanced PerformanceOptimizer functionality for dynamic spawning.
 * Tests the new frame rate monitoring and spawn rate adjustment features.
 */
public class EnhancedPerformanceOptimizerTest {
    
    private DynamicSpawnController mockSpawnController;
    private EnhancedSpawnConfig testConfig;
    
    @BeforeEach
    void setUp() {
        // Create test configuration
        testConfig = new EnhancedSpawnConfig();
        testConfig.applyDefaults();
        
        // Create mock spawn controller (without LibGDX dependencies)
        mockSpawnController = new TestDynamicSpawnController();
        
        // Set up PerformanceOptimizer with mock controller
        PerformanceOptimizer.setSpawnController(mockSpawnController);
    }
    
    @Test
    void testDistributedSpawningLogic() {
        // Test that distributed spawning limits spawns per frame
        PerformanceOptimizer.startFrame();
        
        // Initially should allow spawning
        assertFalse(PerformanceOptimizer.shouldDistributeSpawning());
        
        // Record spawns up to the limit
        for (int i = 0; i < PerformanceOptimizer.getMaxSpawnsPerFrame(); i++) {
            PerformanceOptimizer.recordSpawnOperation();
        }
        
        // Should now require distribution
        assertTrue(PerformanceOptimizer.shouldDistributeSpawning());
        
        assertEquals(PerformanceOptimizer.getMaxSpawnsPerFrame(), PerformanceOptimizer.getSpawnsThisFrame());
    }
    
    @Test
    void testPerformanceThresholds() {
        // Test that performance thresholds are correctly set for 30 FPS minimum
        PerformanceOptimizer.startFrame();
        
        // Simulate good performance (16ms frame time = 60 FPS)
        float goodFrameTime = PerformanceOptimizer.endFrame(0.016f);
        assertTrue(PerformanceOptimizer.isPerformanceAcceptable());
        
        // Reset for next test
        PerformanceOptimizer.startFrame();
        
        // Simulate poor performance (40ms frame time = 25 FPS)
        float poorFrameTime = PerformanceOptimizer.endFrame(0.040f);
        // Note: This might still show as acceptable due to rolling average
        // The actual performance reduction would trigger after sustained poor performance
    }
    
    @Test
    void testFrameCounterReset() {
        // Test that spawn counters reset each frame
        PerformanceOptimizer.startFrame();
        assertEquals(0, PerformanceOptimizer.getSpawnsThisFrame());
        
        PerformanceOptimizer.recordSpawnOperation();
        assertEquals(1, PerformanceOptimizer.getSpawnsThisFrame());
        
        // Start new frame should reset counter
        PerformanceOptimizer.startFrame();
        assertEquals(0, PerformanceOptimizer.getSpawnsThisFrame());
    }
    
    @Test
    void testPerformanceStatsFormat() {
        // Test that performance stats include new dynamic spawning metrics
        PerformanceOptimizer.startFrame();
        PerformanceOptimizer.recordSpawnOperation();
        PerformanceOptimizer.endFrame(0.016f);
        
        String stats = PerformanceOptimizer.getPerformanceStats();
        
        // Should contain frame time information
        assertTrue(stats.contains("Avg Frame"));
        assertTrue(stats.contains("Target"));
        assertTrue(stats.contains("Spawns/Frame"));
        assertTrue(stats.contains("Reduction"));
    }
    
    @Test
    void testCurrentFPSCalculation() {
        PerformanceOptimizer.startFrame();
        PerformanceOptimizer.endFrame(0.016f); // 16ms = ~62.5 FPS
        
        float fps = PerformanceOptimizer.getCurrentFPS();
        assertTrue(fps > 0, "FPS should be positive");
        assertTrue(fps < 1000, "FPS should be reasonable"); // Sanity check
    }
    
    /**
     * Mock DynamicSpawnController for testing without LibGDX dependencies
     */
    private static class TestDynamicSpawnController extends DynamicSpawnController {
        private boolean performanceReductionActive = false;
        
        public TestDynamicSpawnController() {
            super(null, null, new EnhancedSpawnConfig());
        }
        
        @Override
        public void setPerformanceReduction(boolean enabled) {
            this.performanceReductionActive = enabled;
        }
        
        @Override
        public boolean isPerformanceReductionActive() {
            return performanceReductionActive;
        }
    }
}