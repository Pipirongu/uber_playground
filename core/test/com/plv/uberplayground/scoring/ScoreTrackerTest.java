package com.plv.uberplayground.scoring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for the ScoreTracker class.
 * Tests basic functionality, thread safety, and listener notifications.
 */
class ScoreTrackerTest {
    
    private ScoreTracker scoreTracker;
    
    @BeforeEach
    void setUp() {
        scoreTracker = new ScoreTracker();
    }
    
    @Test
    void testInitialScore() {
        assertEquals(ScoreConstants.INITIAL_SCORE, scoreTracker.getCurrentScore());
    }
    
    @Test
    void testIncrementScore() {
        scoreTracker.incrementScore();
        assertEquals(ScoreConstants.POINTS_PER_ENEMY_KILL, scoreTracker.getCurrentScore());
        
        scoreTracker.incrementScore();
        assertEquals(2 * ScoreConstants.POINTS_PER_ENEMY_KILL, scoreTracker.getCurrentScore());
    }
    
    @Test
    void testResetScore() {
        scoreTracker.incrementScore();
        scoreTracker.incrementScore();
        assertEquals(2 * ScoreConstants.POINTS_PER_ENEMY_KILL, scoreTracker.getCurrentScore());
        
        scoreTracker.resetScore();
        assertEquals(ScoreConstants.INITIAL_SCORE, scoreTracker.getCurrentScore());
    }
    
    @Test
    void testListenerNotification() {
        TestScoreListener listener = new TestScoreListener();
        scoreTracker.addScoreListener(listener);
        
        scoreTracker.incrementScore();
        assertEquals(ScoreConstants.POINTS_PER_ENEMY_KILL, listener.getLastScore());
        
        scoreTracker.resetScore();
        assertEquals(ScoreConstants.INITIAL_SCORE, listener.getLastScore());
    }
    
    @Test
    void testMultipleListeners() {
        TestScoreListener listener1 = new TestScoreListener();
        TestScoreListener listener2 = new TestScoreListener();
        
        scoreTracker.addScoreListener(listener1);
        scoreTracker.addScoreListener(listener2);
        
        scoreTracker.incrementScore();
        
        assertEquals(ScoreConstants.POINTS_PER_ENEMY_KILL, listener1.getLastScore());
        assertEquals(ScoreConstants.POINTS_PER_ENEMY_KILL, listener2.getLastScore());
    }
    
    @Test
    void testRemoveListener() {
        TestScoreListener listener = new TestScoreListener();
        scoreTracker.addScoreListener(listener);
        assertEquals(1, scoreTracker.getListenerCount());
        
        boolean removed = scoreTracker.removeScoreListener(listener);
        assertTrue(removed);
        assertEquals(0, scoreTracker.getListenerCount());
        
        // Listener should not be notified after removal
        scoreTracker.incrementScore();
        assertEquals(-1, listener.getLastScore()); // Initial value, not updated
    }
    
    @Test
    void testNullListenerThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            scoreTracker.addScoreListener(null);
        });
    }
    
    @Test
    void testThreadSafety() throws InterruptedException {
        final int numThreads = 10;
        final int incrementsPerThread = 100;
        final ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        final CountDownLatch latch = new CountDownLatch(numThreads);
        final AtomicInteger notificationCount = new AtomicInteger(0);
        
        // Add a listener to count notifications
        scoreTracker.addScoreListener(newScore -> notificationCount.incrementAndGet());
        
        // Submit increment tasks
        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < incrementsPerThread; j++) {
                        scoreTracker.incrementScore();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // Wait for all threads to complete
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        executor.shutdown();
        
        // Verify final score is correct
        int expectedScore = numThreads * incrementsPerThread * ScoreConstants.POINTS_PER_ENEMY_KILL;
        assertEquals(expectedScore, scoreTracker.getCurrentScore());
        
        // Verify all notifications were received
        assertEquals(numThreads * incrementsPerThread, notificationCount.get());
    }
    
    /**
     * Test implementation of ScoreListener for testing purposes.
     */
    private static class TestScoreListener implements ScoreListener {
        private int lastScore = -1; // Use -1 to indicate no score received yet
        
        @Override
        public void onScoreChanged(int newScore) {
            this.lastScore = newScore;
        }
        
        public int getLastScore() {
            return lastScore;
        }
    }
}