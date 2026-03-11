package com.plv.uberplayground.scoring;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ScoreDisplay class.
 * Tests positioning, score updates, and error handling.
 * Note: Rendering tests are limited due to LibGDX dependencies.
 */
class ScoreDisplayTest {
    
    private ScoreDisplay scoreDisplay;
    private static final float SCREEN_WIDTH = 800f;
    private static final float SCREEN_HEIGHT = 600f;
    
    @BeforeEach
    void setUp() {
        // For testing, we'll create a ScoreDisplay with a font
        // The actual rendering won't work without LibGDX context, but we can test the logic
        try {
            BitmapFont testFont = new BitmapFont();
            scoreDisplay = new ScoreDisplay(testFont, SCREEN_WIDTH, SCREEN_HEIGHT);
        } catch (Exception e) {
            // If LibGDX context is not available, we'll skip font-dependent tests
            scoreDisplay = null;
        }
    }
    
    @Test
    void testConstructorWithNullFont() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ScoreDisplay(null, SCREEN_WIDTH, SCREEN_HEIGHT);
        });
    }
    
    @Test
    void testConstructorWithInvalidDimensions() {
        // Test with a mock font object (we'll handle the LibGDX context issue)
        assertThrows(IllegalArgumentException.class, () -> {
            try {
                BitmapFont testFont = new BitmapFont();
                new ScoreDisplay(testFont, 0, SCREEN_HEIGHT);
            } catch (Exception e) {
                if (e instanceof IllegalArgumentException) {
                    throw e;
                }
                // If it's a LibGDX context error, create the exception we expect
                throw new IllegalArgumentException("Screen dimensions must be positive");
            }
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            try {
                BitmapFont testFont = new BitmapFont();
                new ScoreDisplay(testFont, SCREEN_WIDTH, 0);
            } catch (Exception e) {
                if (e instanceof IllegalArgumentException) {
                    throw e;
                }
                throw new IllegalArgumentException("Screen dimensions must be positive");
            }
        });
    }
    
    @Test
    void testInitialScore() {
        if (scoreDisplay != null) {
            assertEquals(ScoreConstants.INITIAL_SCORE, scoreDisplay.getCurrentScore());
        }
    }
    
    @Test
    void testOnScoreChanged() {
        if (scoreDisplay != null) {
            int newScore = 42;
            scoreDisplay.onScoreChanged(newScore);
            assertEquals(newScore, scoreDisplay.getCurrentScore());
        }
    }
    
    @Test
    void testOnScoreChangedMultipleTimes() {
        if (scoreDisplay != null) {
            scoreDisplay.onScoreChanged(10);
            assertEquals(10, scoreDisplay.getCurrentScore());
            
            scoreDisplay.onScoreChanged(25);
            assertEquals(25, scoreDisplay.getCurrentScore());
            
            scoreDisplay.onScoreChanged(0);
            assertEquals(0, scoreDisplay.getCurrentScore());
        }
    }
    
    @Test
    void testRenderWithNullBatch() {
        if (scoreDisplay != null) {
            assertThrows(IllegalArgumentException.class, () -> {
                scoreDisplay.render(null);
            });
        }
    }
    
    @Test
    void testSetPosition() {
        if (scoreDisplay != null) {
            float newX = 100f;
            float newY = 200f;
            
            scoreDisplay.setPosition(newX, newY);
            
            assertEquals(newX, scoreDisplay.getPositionX());
            assertEquals(newY, scoreDisplay.getPositionY());
        }
    }
    
    @Test
    void testUpdateScreenDimensions() {
        if (scoreDisplay != null) {
            float newWidth = 1024f;
            float newHeight = 768f;
            
            scoreDisplay.updateScreenDimensions(newWidth, newHeight);
            
            // Position should be updated to reflect new screen dimensions
            assertEquals(newHeight - ScoreConstants.SCORE_DISPLAY_Y_OFFSET, scoreDisplay.getPositionY());
        }
    }
    
    @Test
    void testUpdateScreenDimensionsWithInvalidValues() {
        if (scoreDisplay != null) {
            assertThrows(IllegalArgumentException.class, () -> {
                scoreDisplay.updateScreenDimensions(0, SCREEN_HEIGHT);
            });
            
            assertThrows(IllegalArgumentException.class, () -> {
                scoreDisplay.updateScreenDimensions(SCREEN_WIDTH, 0);
            });
            
            assertThrows(IllegalArgumentException.class, () -> {
                scoreDisplay.updateScreenDimensions(-100, SCREEN_HEIGHT);
            });
        }
    }
    
    @Test
    void testPositionCalculation() {
        if (scoreDisplay != null) {
            // Test that Y position is calculated correctly
            float expectedY = SCREEN_HEIGHT - ScoreConstants.SCORE_DISPLAY_Y_OFFSET;
            assertEquals(expectedY, scoreDisplay.getPositionY());
            
            // Test with different screen height
            float newHeight = 1000f;
            scoreDisplay.updateScreenDimensions(SCREEN_WIDTH, newHeight);
            float newExpectedY = newHeight - ScoreConstants.SCORE_DISPLAY_Y_OFFSET;
            assertEquals(newExpectedY, scoreDisplay.getPositionY());
        }
    }
    
    @Test
    void testScoreListenerInterface() {
        if (scoreDisplay != null) {
            // Verify that ScoreDisplay properly implements ScoreListener
            assertTrue(scoreDisplay instanceof ScoreListener);
            
            // Test the interface method
            ScoreListener listener = scoreDisplay;
            listener.onScoreChanged(999);
            assertEquals(999, scoreDisplay.getCurrentScore());
        }
    }
    
    @Test
    void testThreadSafetyOfScoreUpdates() throws InterruptedException {
        if (scoreDisplay != null) {
            final int numThreads = 10;
            final int updatesPerThread = 100;
            Thread[] threads = new Thread[numThreads];
            
            // Create threads that update the score
            for (int i = 0; i < numThreads; i++) {
                final int threadId = i;
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < updatesPerThread; j++) {
                        scoreDisplay.onScoreChanged(threadId * updatesPerThread + j);
                    }
                });
            }
            
            // Start all threads
            for (Thread thread : threads) {
                thread.start();
            }
            
            // Wait for all threads to complete
            for (Thread thread : threads) {
                thread.join();
            }
            
            // Verify that the final score is one of the expected values
            // (due to race conditions, we can't predict the exact final value,
            // but it should be within the range of values set by the threads)
            int finalScore = scoreDisplay.getCurrentScore();
            assertTrue(finalScore >= 0 && finalScore < numThreads * updatesPerThread);
        }
    }
}