package com.plv.uberplayground.game;

import net.jqwik.api.*;
import net.jqwik.api.constraints.FloatRange;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import com.plv.uberplayground.enemies.DynamicSpawnController;
import com.plv.uberplayground.enemies.EnhancedSpawnConfig;

/**
 * Property-based tests for GameStateManager and DynamicSpawnController integration.
 * Tests state preservation during pause/resume cycles and game state transitions.
 */
public class GameStateManagerPropertyTest {
    
    private GameStateManager gameStateManager;
    private EnhancedSpawnConfig spawnConfig;
    private DynamicSpawnController dynamicSpawnController;
    
    @BeforeEach
    void setUp() {
        gameStateManager = new GameStateManager();
        spawnConfig = new EnhancedSpawnConfig();
        spawnConfig.applyDefaults();
        spawnConfig.setPreserveScalingOnPause(true); // Enable state preservation
        
        // Create DynamicSpawnController without EnemySpawner for logic testing
        dynamicSpawnController = new DynamicSpawnController(null, gameStateManager, spawnConfig);
    }
    
    /**
     * **Property 11: Pause/Resume State Preservation**
     * **Validates: Requirements 5.1, 5.2, 5.5**
     * 
     * This property test validates that for any pause and resume cycle during active gameplay,
     * the scaling state (including game duration and current spawn rate) should be preserved
     * and continue from where it left off.
     * 
     * The test simulates various gameplay durations before pause, different pause durations,
     * and verifies that:
     * 1. Game duration tracking continues correctly after resume
     * 2. Spawn rate calculation remains consistent with pre-pause state
     * 3. Scaling state is properly preserved across pause/resume cycles
     * 4. Multiple pause/resume cycles maintain state integrity
     * 
     * This ensures that players can pause and resume the game without losing their
     * progression in the dynamic difficulty scaling system.
     */
    @Property(tries = 100)
    @Label("Property 11: Pause/Resume State Preservation")
    void pauseResumeStatePreservation(
            @ForAll @FloatRange(min = 1.0f, max = 120.0f) float gameplayBeforePause,
            @ForAll @FloatRange(min = 0.1f, max = 30.0f) float pauseDuration,
            @ForAll @FloatRange(min = 0.1f, max = 60.0f) float gameplayAfterResume) {
        
        // Initialize configuration for this property test
        EnhancedSpawnConfig testConfig = new EnhancedSpawnConfig();
        testConfig.applyDefaults();
        testConfig.setPreserveScalingOnPause(true);
        
        GameStateManager testGameStateManager = new GameStateManager();
        DynamicSpawnController testController = new DynamicSpawnController(null, testGameStateManager, testConfig);
        
        // Start new game
        testGameStateManager.startNewGame();
        assertTrue(testGameStateManager.isGameplayActive(), "Game should be in active state");
        assertTrue(testController.isActive(), "Dynamic spawn controller should be active");
        
        // Simulate gameplay before pause
        testGameStateManager.update(gameplayBeforePause);
        testController.update(gameplayBeforePause);
        
        // Capture state before pause
        float durationBeforePause = testGameStateManager.getSessionData().getActiveDuration();
        float spawnRateBeforePause = testController.getCurrentSpawnRate();
        float scalingFactorBeforePause = testController.getCurrentScalingFactor();
        boolean wasActiveBeforePause = testController.isActive();
        
        // Verify pre-pause state
        assertEquals(gameplayBeforePause, durationBeforePause, 0.001f, 
            "Active duration should match gameplay time before pause");
        assertTrue(wasActiveBeforePause, "Controller should be active before pause");
        assertTrue(spawnRateBeforePause >= testConfig.getBaseSpawnRate(), 
            "Spawn rate should be at least base rate");
        
        // Pause the game
        testGameStateManager.pauseGame();
        assertFalse(testGameStateManager.isGameplayActive(), "Game should be paused");
        assertFalse(testController.isActive(), "Controller should be inactive during pause");
        
        // Simulate pause duration (only total duration should increase, not active duration)
        testGameStateManager.update(pauseDuration);
        testController.update(pauseDuration);
        
        // Verify pause state
        float totalDurationDuringPause = testGameStateManager.getSessionData().getTotalGameDuration();
        float activeDurationDuringPause = testGameStateManager.getSessionData().getActiveDuration();
        float pausedDurationDuringPause = testGameStateManager.getSessionData().getPausedDuration();
        
        assertEquals(gameplayBeforePause + pauseDuration, totalDurationDuringPause, 0.001f,
            "Total duration should include pause time");
        assertEquals(gameplayBeforePause, activeDurationDuringPause, 0.001f,
            "Active duration should not increase during pause");
        assertEquals(pauseDuration, pausedDurationDuringPause, 0.001f,
            "Paused duration should track pause time");
        
        // Resume the game
        testGameStateManager.resumeGame();
        assertTrue(testGameStateManager.isGameplayActive(), "Game should be active after resume");
        assertTrue(testController.isActive(), "Controller should be active after resume");
        
        // Verify state preservation immediately after resume
        float spawnRateAfterResume = testController.getCurrentSpawnRate();
        float scalingFactorAfterResume = testController.getCurrentScalingFactor();
        
        assertEquals(spawnRateBeforePause, spawnRateAfterResume, 0.001f,
            "Spawn rate should be preserved across pause/resume");
        assertEquals(scalingFactorBeforePause, scalingFactorAfterResume, 0.001f,
            "Scaling factor should be preserved across pause/resume");
        
        // Continue gameplay after resume
        testGameStateManager.update(gameplayAfterResume);
        testController.update(gameplayAfterResume);
        
        // Verify final state
        float finalActiveDuration = testGameStateManager.getSessionData().getActiveDuration();
        float finalTotalDuration = testGameStateManager.getSessionData().getTotalGameDuration();
        float finalSpawnRate = testController.getCurrentSpawnRate();
        
        float expectedActiveDuration = gameplayBeforePause + gameplayAfterResume;
        float expectedTotalDuration = gameplayBeforePause + pauseDuration + gameplayAfterResume;
        
        assertEquals(expectedActiveDuration, finalActiveDuration, 0.001f,
            "Final active duration should equal total gameplay time (excluding pause)");
        assertEquals(expectedTotalDuration, finalTotalDuration, 0.001f,
            "Final total duration should include all time (including pause)");
        
        // Verify spawn rate progression continues correctly
        // Calculate what the spawn rate should be based on total active duration
        float expectedFinalSpawnRate = testController.getScalingCalculator().calculateSpawnRate(expectedActiveDuration);
        assertEquals(expectedFinalSpawnRate, finalSpawnRate, 0.001f,
            "Final spawn rate should match expected rate for total active duration");
        
        // Verify scaling progression: spawn rate should increase with more active time
        if (gameplayAfterResume > 0.1f) {
            assertTrue(finalSpawnRate >= spawnRateBeforePause,
                "Spawn rate should continue increasing after resume");
        }
        
        // Verify bounds are still enforced
        assertTrue(finalSpawnRate <= testConfig.getMaxSpawnRate(),
            "Final spawn rate should not exceed maximum");
        assertTrue(finalSpawnRate >= testConfig.getBaseSpawnRate(),
            "Final spawn rate should not be below base rate");
    }
    
    /**
     * Provider for multiple pause/resume cycles to test complex scenarios.
     */
    @Provide
    Arbitrary<Integer> pauseResumeCycles() {
        return Arbitraries.integers().between(1, 5);
    }
    
    /**
     * **Property 11.1: Multiple Pause/Resume Cycles State Preservation**
     * **Validates: Requirements 5.1, 5.2, 5.5**
     * 
     * This property test validates that multiple pause/resume cycles maintain
     * state integrity throughout the entire sequence.
     */
    @Property(tries = 50)
    @Label("Property 11.1: Multiple Pause/Resume Cycles State Preservation")
    void multiplePauseResumeStatePreservation(
            @ForAll("pauseResumeCycles") int numCycles,
            @ForAll @FloatRange(min = 1.0f, max = 20.0f) float gameplayPerCycle,
            @ForAll @FloatRange(min = 0.1f, max = 5.0f) float pausePerCycle) {
        
        // Initialize configuration for this property test
        EnhancedSpawnConfig testConfig = new EnhancedSpawnConfig();
        testConfig.applyDefaults();
        testConfig.setPreserveScalingOnPause(true);
        
        GameStateManager testGameStateManager = new GameStateManager();
        DynamicSpawnController testController = new DynamicSpawnController(null, testGameStateManager, testConfig);
        
        // Start new game
        testGameStateManager.startNewGame();
        
        float totalActiveTime = 0.0f;
        float totalPauseTime = 0.0f;
        float lastSpawnRate = testController.getCurrentSpawnRate();
        
        // Perform multiple pause/resume cycles
        for (int cycle = 0; cycle < numCycles; cycle++) {
            // Gameplay phase
            testGameStateManager.update(gameplayPerCycle);
            testController.update(gameplayPerCycle);
            totalActiveTime += gameplayPerCycle;
            
            // Verify spawn rate progression
            float currentSpawnRate = testController.getCurrentSpawnRate();
            assertTrue(currentSpawnRate >= lastSpawnRate,
                "Spawn rate should not decrease during gameplay in cycle " + cycle);
            lastSpawnRate = currentSpawnRate;
            
            // Pause phase
            testGameStateManager.pauseGame();
            testGameStateManager.update(pausePerCycle);
            testController.update(pausePerCycle);
            totalPauseTime += pausePerCycle;
            
            // Verify pause state
            assertFalse(testController.isActive(), "Controller should be inactive during pause in cycle " + cycle);
            
            // Resume phase
            testGameStateManager.resumeGame();
            
            // Verify state preservation after resume
            float spawnRateAfterResume = testController.getCurrentSpawnRate();
            assertEquals(lastSpawnRate, spawnRateAfterResume, 0.001f,
                "Spawn rate should be preserved after resume in cycle " + cycle);
        }
        
        // Verify final state after all cycles
        float finalActiveDuration = testGameStateManager.getSessionData().getActiveDuration();
        float finalTotalDuration = testGameStateManager.getSessionData().getTotalGameDuration();
        float finalPausedDuration = testGameStateManager.getSessionData().getPausedDuration();
        
        assertEquals(totalActiveTime, finalActiveDuration, 0.001f,
            "Final active duration should equal sum of all gameplay periods");
        assertEquals(totalActiveTime + totalPauseTime, finalTotalDuration, 0.001f,
            "Final total duration should include all time");
        assertEquals(totalPauseTime, finalPausedDuration, 0.001f,
            "Final paused duration should equal sum of all pause periods");
        
        // Verify spawn rate matches expected value for total active time
        float expectedSpawnRate = testController.getScalingCalculator().calculateSpawnRate(totalActiveTime);
        float actualSpawnRate = testController.getCurrentSpawnRate();
        assertEquals(expectedSpawnRate, actualSpawnRate, 0.001f,
            "Final spawn rate should match expected rate for total active time");
    }
    
    /**
     * **Property 12: Game Over Scaling Reset**
     * **Validates: Requirements 5.3**
     * 
     * This property test validates that for any game over event, the scaling system
     * should reset to base spawn rate and zero game duration for the next game session.
     * 
     * The test simulates various gameplay scenarios before game over, verifies that
     * scaling has progressed beyond base values, triggers game over, and then validates
     * that:
     * 1. Game duration is reset to zero
     * 2. Spawn rate is reset to base spawn rate
     * 3. Scaling factor is reset to zero
     * 4. Controller state is properly reset for new game
     * 5. New game can start fresh with proper scaling progression
     * 
     * This ensures that each game session starts with consistent initial conditions
     * regardless of how far the previous session had progressed in difficulty scaling.
     */
    @Property(tries = 100)
    @Label("Property 12: Game Over Scaling Reset")
    void gameOverScalingReset(
            @ForAll @FloatRange(min = 5.0f, max = 180.0f) float gameplayBeforeGameOver,
            @ForAll @FloatRange(min = 1.0f, max = 60.0f) float newGameplayAfterReset) {
        
        // Initialize configuration for this property test
        EnhancedSpawnConfig testConfig = new EnhancedSpawnConfig();
        testConfig.applyDefaults();
        testConfig.setResetOnGameOver(true); // Ensure game over reset is enabled
        
        GameStateManager testGameStateManager = new GameStateManager();
        DynamicSpawnController testController = new DynamicSpawnController(null, testGameStateManager, testConfig);
        
        // Start new game
        testGameStateManager.startNewGame();
        assertTrue(testGameStateManager.isGameplayActive(), "Game should be in active state");
        assertTrue(testController.isActive(), "Dynamic spawn controller should be active");
        
        // Verify initial state
        float initialDuration = testGameStateManager.getSessionData().getActiveDuration();
        float initialSpawnRate = testController.getCurrentSpawnRate();
        float initialScalingFactor = testController.getCurrentScalingFactor();
        
        assertEquals(0.0f, initialDuration, 0.001f, "Initial game duration should be zero");
        assertEquals(testConfig.getBaseSpawnRate(), initialSpawnRate, 0.001f, 
            "Initial spawn rate should be base spawn rate");
        assertEquals(0.0f, initialScalingFactor, 0.001f, "Initial scaling factor should be zero");
        
        // Simulate gameplay to build up scaling
        testGameStateManager.update(gameplayBeforeGameOver);
        testController.update(gameplayBeforeGameOver);
        
        // Capture state before game over (should show progression)
        float durationBeforeGameOver = testGameStateManager.getSessionData().getActiveDuration();
        float spawnRateBeforeGameOver = testController.getCurrentSpawnRate();
        float scalingFactorBeforeGameOver = testController.getCurrentScalingFactor();
        
        // Verify that scaling has progressed beyond initial values
        assertEquals(gameplayBeforeGameOver, durationBeforeGameOver, 0.001f,
            "Game duration should match gameplay time");
        assertTrue(spawnRateBeforeGameOver >= testConfig.getBaseSpawnRate(),
            "Spawn rate should be at least base rate before game over");
        
        // For gameplay longer than a few seconds, scaling should have increased
        if (gameplayBeforeGameOver > 5.0f) {
            assertTrue(spawnRateBeforeGameOver > testConfig.getBaseSpawnRate(),
                "Spawn rate should have increased above base rate after " + gameplayBeforeGameOver + " seconds");
            assertTrue(scalingFactorBeforeGameOver > 0.0f,
                "Scaling factor should be positive after " + gameplayBeforeGameOver + " seconds");
        }
        
        // Trigger game over
        testGameStateManager.endGame();
        assertEquals(GameStateManager.GameState.GAME_OVER, testGameStateManager.getCurrentState(),
            "Game state should be GAME_OVER");
        assertTrue(testGameStateManager.isGameOver(), "Game should be in game over state");
        assertFalse(testGameStateManager.isGameplayActive(), "Game should not be active during game over");
        
        // Verify game over state - controller should be inactive but not reset yet
        assertFalse(testController.isActive(), "Controller should be inactive during game over");
        
        // Start a new game (this should trigger the reset)
        testGameStateManager.startNewGame();
        
        // Verify reset state immediately after new game start
        float durationAfterReset = testGameStateManager.getSessionData().getActiveDuration();
        float spawnRateAfterReset = testController.getCurrentSpawnRate();
        float scalingFactorAfterReset = testController.getCurrentScalingFactor();
        
        assertEquals(0.0f, durationAfterReset, 0.001f,
            "Game duration should be reset to zero after new game");
        assertEquals(testConfig.getBaseSpawnRate(), spawnRateAfterReset, 0.001f,
            "Spawn rate should be reset to base spawn rate after new game");
        assertEquals(0.0f, scalingFactorAfterReset, 0.001f,
            "Scaling factor should be reset to zero after new game");
        
        // Verify controller is active again
        assertTrue(testGameStateManager.isGameplayActive(), "Game should be active after new game start");
        assertTrue(testController.isActive(), "Controller should be active after new game start");
        
        // Simulate some gameplay in the new session to verify scaling works correctly
        testGameStateManager.update(newGameplayAfterReset);
        testController.update(newGameplayAfterReset);
        
        // Verify new session scaling progression
        float finalDuration = testGameStateManager.getSessionData().getActiveDuration();
        float finalSpawnRate = testController.getCurrentSpawnRate();
        float finalScalingFactor = testController.getCurrentScalingFactor();
        
        assertEquals(newGameplayAfterReset, finalDuration, 0.001f,
            "New session duration should match new gameplay time");
        
        // Calculate expected values for the new session
        float expectedFinalSpawnRate = testController.getScalingCalculator().calculateSpawnRate(newGameplayAfterReset);
        float expectedFinalScalingFactor = testController.getScalingCalculator().calculateScalingFactor(newGameplayAfterReset);
        
        assertEquals(expectedFinalSpawnRate, finalSpawnRate, 0.001f,
            "New session spawn rate should match expected rate for new gameplay duration");
        assertEquals(expectedFinalScalingFactor, finalScalingFactor, 0.001f,
            "New session scaling factor should match expected factor for new gameplay duration");
        
        // Verify scaling progression in new session
        assertTrue(finalSpawnRate >= testConfig.getBaseSpawnRate(),
            "New session spawn rate should be at least base rate");
        assertTrue(finalSpawnRate <= testConfig.getMaxSpawnRate(),
            "New session spawn rate should not exceed maximum");
        
        // For longer new gameplay, verify scaling is progressing
        if (newGameplayAfterReset > 2.0f) {
            assertTrue(finalScalingFactor >= 0.0f,
                "New session scaling factor should be non-negative");
        }
        
        // Verify independence: new session scaling should not be affected by previous session
        // The spawn rate should be based only on new gameplay duration, not previous session
        float independentExpectedRate = testConfig.getBaseSpawnRate() * 
            (1 + testConfig.getScalingFactor() * 
             testController.getScalingCalculator().calculateScalingFactor(newGameplayAfterReset));
        independentExpectedRate = Math.min(independentExpectedRate, testConfig.getMaxSpawnRate());
        
        assertEquals(independentExpectedRate, finalSpawnRate, 0.001f,
            "New session spawn rate should be independent of previous session progression");
        
        // Verify session data is properly reset
        assertEquals(newGameplayAfterReset, testGameStateManager.getSessionData().getTotalGameDuration(), 0.001f,
            "Total duration should only include new session time");
        assertEquals(0.0f, testGameStateManager.getSessionData().getPausedDuration(), 0.001f,
            "Paused duration should be zero in new session");
    }
}