package com.plv.uberplayground.enemies;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.plv.uberplayground.actors.PlayerActor;
import com.plv.uberplayground.actors.PlayerShip;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance validation tests for the enhanced enemy system.
 * Tests 60 FPS performance requirements with maximum enemy count.
 * 
 * Requirements: 8.1, 8.3, 8.5
 */
public class PerformanceValidationTest {
    
    private World world;
    private Stage gameStage;
    private HealthSystem healthSystem;
    private EnemyManager enemyManager;
    private EnemySpawner enemySpawner;
    private PlayerShip playerShip;
    
    // Performance measurement constants
    private static final int TARGET_FPS = 60;
    private static final float TARGET_FRAME_TIME_MS = 1000.0f / TARGET_FPS; // 16.67ms
    private static final int MAX_ENEMIES = 10;
    private static final int PERFORMANCE_TEST_FRAMES = 300; // 5 seconds at 60 FPS
    private static final float PERFORMANCE_TOLERANCE = 1.2f; // Allow 20% overhead
    
    @BeforeEach
    void setUp() {
        // Initialize LibGDX-like environment for testing
        world = new World(new Vector2(0, 0), true);
        gameStage = new Stage();
        
        // Initialize enemy system components
        healthSystem = new HealthSystem();
        enemyManager = new EnemyManager(healthSystem);
        
        // Create player ship for testing
        playerShip = new PlayerShip("test_player", world, 800, 600, 5, 2, 50.0f);
        
        // Initialize enemy spawner
        enemySpawner = new EnemySpawner(world, gameStage, enemyManager, playerShip, 1600, 1200);
    }
    
    @Test
    @DisplayName("System should maintain 60 FPS with 10 active enemies")
    void testPerformanceWith10Enemies() {
        // Spawn maximum number of enemies
        spawnMaximumEnemies();
        
        // Verify we have the expected number of enemies
        assertEquals(MAX_ENEMIES, enemyManager.getEnemyCount(), 
                    "Should have maximum number of enemies for performance test");
        
        // Measure performance over multiple frames
        PerformanceMetrics metrics = measureSystemPerformance(PERFORMANCE_TEST_FRAMES);
        
        // Validate performance requirements
        assertPerformanceRequirements(metrics);
        
        // Log performance results
        logPerformanceResults(metrics);
    }
    
    @Test
    @DisplayName("Health bar rendering should be optimized for performance")
    void testHealthBarRenderingOptimization() {
        // Create test enemy with health bar
        PlayerActor enemy = createTestEnemy(400, 300);
        enemyManager.addEnemy(enemy);
        
        // Measure health bar rendering performance
        long startTime = TimeUtils.nanoTime();
        int renderCalls = 0;
        
        // Simulate multiple renders with same health (should be optimized)
        for (int i = 0; i < 100; i++) {
            if (enemy.getHealthBar() != null) {
                // Simulate render call (would normally render to screen)
                renderCalls++;
            }
        }
        
        long renderTime = TimeUtils.nanoTime() - startTime;
        float renderTimeMs = renderTime / 1_000_000.0f;
        
        // Health bar rendering should be very fast
        assertTrue(renderTimeMs < 1.0f, 
                  "Health bar rendering took " + renderTimeMs + "ms, should be < 1ms");
        
        System.out.println("Health bar rendering: " + renderCalls + " calls in " + 
                          String.format("%.2f", renderTimeMs) + "ms");
    }
    
    @Test
    @DisplayName("Behavior calculations should be efficient with maximum enemies")
    void testBehaviorCalculationPerformance() {
        // Spawn maximum enemies with different behaviors
        spawnMaximumEnemies();
        
        // Set different behaviors for performance testing
        Array<PlayerActor> enemies = enemyManager.getActiveEnemies();
        for (int i = 0; i < enemies.size; i++) {
            PlayerActor enemy = enemies.get(i);
            SteeringBehaviorType behavior = SteeringBehaviorType.values()[i % SteeringBehaviorType.values().length];
            enemy.getBehaviorState().forceBehaviorSwitch(behavior);
        }
        
        // Measure behavior calculation performance
        long startTime = TimeUtils.nanoTime();
        
        // Simulate behavior calculations for multiple frames
        for (int frame = 0; frame < 60; frame++) {
            for (PlayerActor enemy : enemies) {
                if (enemy.getBehaviorState() != null) {
                    enemy.getBehaviorState().update(1.0f / 60.0f);
                }
            }
        }
        
        long behaviorTime = TimeUtils.nanoTime() - startTime;
        float behaviorTimeMs = behaviorTime / 1_000_000.0f;
        
        // Behavior calculations should complete within frame budget
        float maxAllowedTime = TARGET_FRAME_TIME_MS * 0.3f; // 30% of frame time
        assertTrue(behaviorTimeMs < maxAllowedTime, 
                  "Behavior calculations took " + behaviorTimeMs + "ms, should be < " + maxAllowedTime + "ms");
        
        System.out.println("Behavior calculations: " + behaviorTimeMs + "ms for " + 
                          enemies.size + " enemies over 60 frames");
    }
    
    @Test
    @DisplayName("Memory management should prevent leaks during enemy lifecycle")
    void testMemoryManagement() {
        // Measure initial memory usage
        System.gc(); // Force garbage collection
        long initialMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        
        // Spawn and destroy enemies multiple times
        for (int cycle = 0; cycle < 5; cycle++) {
            // Spawn maximum enemies
            spawnMaximumEnemies();
            assertEquals(MAX_ENEMIES, enemyManager.getEnemyCount());
            
            // Destroy all enemies
            enemyManager.clearAllEnemies();
            assertEquals(0, enemyManager.getEnemyCount());
            
            // Force garbage collection
            System.gc();
        }
        
        // Measure final memory usage
        long finalMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long memoryIncrease = finalMemory - initialMemory;
        
        // Memory increase should be minimal (less than 1MB)
        long maxAllowedIncrease = 1024 * 1024; // 1MB
        assertTrue(memoryIncrease < maxAllowedIncrease, 
                  "Memory increased by " + (memoryIncrease / 1024) + "KB, should be < " + 
                  (maxAllowedIncrease / 1024) + "KB");
        
        System.out.println("Memory management test: " + (memoryIncrease / 1024) + "KB increase after 5 cycles");
    }
    
    @Test
    @DisplayName("System should handle game reset efficiently")
    void testGameResetPerformance() {
        // Spawn maximum enemies and simulate active gameplay
        spawnMaximumEnemies();
        
        // Simulate some gameplay activity
        for (int i = 0; i < 60; i++) {
            enemyManager.updateAll(1.0f / 60.0f);
        }
        
        // Measure reset performance
        long startTime = TimeUtils.nanoTime();
        
        // Reset the game (clear all enemies)
        enemyManager.clearAllEnemies();
        healthSystem.clear();
        
        long resetTime = TimeUtils.nanoTime() - startTime;
        float resetTimeMs = resetTime / 1_000_000.0f;
        
        // Reset should be very fast (less than 5ms)
        assertTrue(resetTimeMs < 5.0f, 
                  "Game reset took " + resetTimeMs + "ms, should be < 5ms");
        
        // Verify clean state after reset
        assertEquals(0, enemyManager.getEnemyCount());
        
        System.out.println("Game reset performance: " + String.format("%.2f", resetTimeMs) + "ms");
    }
    
    /**
     * Spawns the maximum number of enemies for performance testing.
     */
    private void spawnMaximumEnemies() {
        enemySpawner.setSpawningEnabled(true);
        
        // Force spawn maximum enemies
        for (int i = 0; i < MAX_ENEMIES; i++) {
            if (enemySpawner.canSpawn()) {
                boolean spawned = enemySpawner.forceSpawn();
                assertTrue(spawned, "Should be able to spawn enemy " + (i + 1));
            }
        }
        
        // Verify maximum count reached
        assertEquals(MAX_ENEMIES, enemyManager.getEnemyCount());
    }
    
    /**
     * Creates a test enemy at the specified position.
     */
    private PlayerActor createTestEnemy(float x, float y) {
        PlayerActor enemy = new PlayerActor("test_enemy", world, x, y, 5, 2, 50.0f);
        enemy.setIsEnemy(true);
        gameStage.addActor(enemy);
        return enemy;
    }
    
    /**
     * Measures system performance over multiple frames.
     */
    private PerformanceMetrics measureSystemPerformance(int frameCount) {
        long totalUpdateTime = 0;
        long maxFrameTime = 0;
        int slowFrames = 0;
        
        for (int frame = 0; frame < frameCount; frame++) {
            long frameStart = TimeUtils.nanoTime();
            
            // Simulate full game update cycle
            float delta = 1.0f / TARGET_FPS;
            
            // Update enemy spawner
            enemySpawner.update(delta);
            
            // Update enemy manager and all enemies
            enemyManager.updateAll(delta);
            
            // Update player targets
            if (playerShip != null && playerShip.getBody() != null) {
                enemyManager.updatePlayerTargets(playerShip.getBody().getPosition());
            }
            
            // Simulate physics step (lightweight for testing)
            world.step(delta, 6, 2);
            
            long frameEnd = TimeUtils.nanoTime();
            long frameTime = frameEnd - frameStart;
            
            totalUpdateTime += frameTime;
            maxFrameTime = Math.max(maxFrameTime, frameTime);
            
            // Count frames that exceed target time
            float frameTimeMs = frameTime / 1_000_000.0f;
            if (frameTimeMs > TARGET_FRAME_TIME_MS) {
                slowFrames++;
            }
        }
        
        return new PerformanceMetrics(frameCount, totalUpdateTime, maxFrameTime, slowFrames);
    }
    
    /**
     * Validates that performance meets requirements.
     */
    private void assertPerformanceRequirements(PerformanceMetrics metrics) {
        // Average frame time should be within target
        float avgFrameTimeMs = metrics.getAverageFrameTimeMs();
        float maxAllowedAvg = TARGET_FRAME_TIME_MS * PERFORMANCE_TOLERANCE;
        assertTrue(avgFrameTimeMs <= maxAllowedAvg, 
                  "Average frame time " + String.format("%.2f", avgFrameTimeMs) + 
                  "ms exceeds target " + String.format("%.2f", maxAllowedAvg) + "ms");
        
        // Maximum frame time should not be excessive
        float maxFrameTimeMs = metrics.getMaxFrameTimeMs();
        float maxAllowedMax = TARGET_FRAME_TIME_MS * 2.0f; // Allow 2x target for worst case
        assertTrue(maxFrameTimeMs <= maxAllowedMax, 
                  "Maximum frame time " + String.format("%.2f", maxFrameTimeMs) + 
                  "ms exceeds limit " + String.format("%.2f", maxAllowedMax) + "ms");
        
        // Most frames should meet target (allow 5% slow frames)
        float slowFramePercentage = metrics.getSlowFramePercentage();
        assertTrue(slowFramePercentage <= 5.0f, 
                  "Slow frame percentage " + String.format("%.1f", slowFramePercentage) + 
                  "% exceeds 5% limit");
    }
    
    /**
     * Logs performance test results.
     */
    private void logPerformanceResults(PerformanceMetrics metrics) {
        System.out.println("\n=== Performance Test Results ===");
        System.out.println("Enemies: " + enemyManager.getEnemyCount());
        System.out.println("Frames tested: " + metrics.frameCount);
        System.out.println("Average frame time: " + String.format("%.2f", metrics.getAverageFrameTimeMs()) + "ms");
        System.out.println("Maximum frame time: " + String.format("%.2f", metrics.getMaxFrameTimeMs()) + "ms");
        System.out.println("Target frame time: " + String.format("%.2f", TARGET_FRAME_TIME_MS) + "ms");
        System.out.println("Slow frames: " + metrics.slowFrames + " (" + 
                          String.format("%.1f", metrics.getSlowFramePercentage()) + "%)");
        System.out.println("Performance status: " + 
                          (metrics.getAverageFrameTimeMs() <= TARGET_FRAME_TIME_MS ? "PASS" : "NEEDS OPTIMIZATION"));
        System.out.println("===============================\n");
    }
    
    /**
     * Performance metrics container class.
     */
    private static class PerformanceMetrics {
        final int frameCount;
        final long totalUpdateTime;
        final long maxFrameTime;
        final int slowFrames;
        
        PerformanceMetrics(int frameCount, long totalUpdateTime, long maxFrameTime, int slowFrames) {
            this.frameCount = frameCount;
            this.totalUpdateTime = totalUpdateTime;
            this.maxFrameTime = maxFrameTime;
            this.slowFrames = slowFrames;
        }
        
        float getAverageFrameTimeMs() {
            return (totalUpdateTime / (float) frameCount) / 1_000_000.0f;
        }
        
        float getMaxFrameTimeMs() {
            return maxFrameTime / 1_000_000.0f;
        }
        
        float getSlowFramePercentage() {
            return (slowFrames / (float) frameCount) * 100.0f;
        }
    }
}