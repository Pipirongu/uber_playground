package com.plv.uberplayground.scoring;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for HighScoreManager class.
 * Tests file I/O operations, caching, and error handling.
 */
class HighScoreManagerTest {
    
    @TempDir
    Path tempDir;
    
    private FileHandle testScoreFile;
    
    @BeforeEach
    void setUp() {
        // Create a test file in temp directory
        File scoreFile = tempDir.resolve(ScoreConstants.HIGH_SCORE_FILENAME).toFile();
        testScoreFile = new FileHandle(scoreFile);
    }
    
    @Test
    void testSaveScore_NewFile() {
        // Test saving a score when no file exists
        HighScoreManager manager = createTestManager();
        manager.saveScore(100);
        
        assertTrue(testScoreFile.exists());
        assertEquals("100\n", testScoreFile.readString());
    }
    
    @Test
    void testSaveScore_MultipleScores() {
        // Test saving multiple scores
        HighScoreManager manager = createTestManager();
        manager.saveScore(100);
        manager.saveScore(200);
        manager.saveScore(150);
        
        List<Integer> scores = manager.getTopScores();
        assertEquals(3, scores.size());
        assertEquals(200, scores.get(0)); // Highest first
        assertEquals(150, scores.get(1));
        assertEquals(100, scores.get(2)); // Lowest last
    }
    
    @Test
    void testSaveScore_EnforcesTop5Limit() {
        // Test that only top 5 scores are kept
        HighScoreManager manager = createTestManager();
        for (int i = 1; i <= 10; i++) {
            manager.saveScore(i * 10);
        }
        
        List<Integer> scores = manager.getTopScores();
        assertEquals(ScoreConstants.MAX_HIGH_SCORES, scores.size());
        assertEquals(100, scores.get(0)); // Highest
        assertEquals(60, scores.get(4));  // 5th highest
    }
    
    @Test
    void testGetTopScores_EmptyFile() {
        // Test getting scores when file doesn't exist
        HighScoreManager manager = createTestManager();
        List<Integer> scores = manager.getTopScores();
        
        assertNotNull(scores);
        assertTrue(scores.isEmpty());
    }
    
    @Test
    void testGetTopScores_WithExistingScores() {
        // Create a file with existing scores
        testScoreFile.writeString("300\n200\n100\n", false);
        
        HighScoreManager manager = createTestManager();
        List<Integer> scores = manager.getTopScores();
        assertEquals(3, scores.size());
        assertEquals(300, scores.get(0));
        assertEquals(200, scores.get(1));
        assertEquals(100, scores.get(2));
    }
    
    @Test
    void testIsHighScore_EmptyList() {
        // Any score should be a high score when list is empty
        HighScoreManager manager = createTestManager();
        assertTrue(manager.isHighScore(1));
        assertTrue(manager.isHighScore(100));
    }
    
    @Test
    void testIsHighScore_PartialList() {
        // Test with less than 5 scores
        HighScoreManager manager = createTestManager();
        manager.saveScore(100);
        manager.saveScore(200);
        
        assertTrue(manager.isHighScore(50));  // Any score qualifies
        assertTrue(manager.isHighScore(300)); // Higher score
    }
    
    @Test
    void testIsHighScore_FullList() {
        // Test with 5 scores (full list)
        HighScoreManager manager = createTestManager();
        for (int i = 1; i <= 5; i++) {
            manager.saveScore(i * 100); // 100, 200, 300, 400, 500
        }
        
        assertFalse(manager.isHighScore(50));  // Too low
        assertFalse(manager.isHighScore(100)); // Equal to lowest
        assertTrue(manager.isHighScore(150));  // Higher than lowest
        assertTrue(manager.isHighScore(600));  // Higher than highest
    }
    
    @Test
    void testInvalidDataParsing() {
        // Test handling of corrupted file data
        testScoreFile.writeString("300\ninvalid\n200\n\n100\n", false);
        
        HighScoreManager manager = createTestManager();
        List<Integer> scores = manager.getTopScores();
        assertEquals(3, scores.size()); // Should skip invalid line and empty line
        assertEquals(300, scores.get(0));
        assertEquals(200, scores.get(1));
        assertEquals(100, scores.get(2));
    }
    
    @Test
    void testInvalidateCache() {
        // Test cache invalidation
        testScoreFile.writeString("100\n", false);
        
        HighScoreManager manager = createTestManager();
        // Load into cache
        manager.getTopScores();
        
        // Modify file
        testScoreFile.writeString("200\n100\n", false);
        
        // Invalidate cache
        manager.invalidateCache();
        
        // Should read fresh data from file
        List<Integer> scores = manager.getTopScores();
        assertEquals(2, scores.size());
        assertEquals(200, scores.get(0));
    }
    
    @Test
    void testErrorHandling_FileOperations() {
        // Test that file operations don't crash the game when they fail
        HighScoreManager manager = createTestManager();
        
        // Test saving to a valid file first
        assertDoesNotThrow(() -> {
            manager.saveScore(100);
        });
        
        // Verify the score was saved
        List<Integer> scores = manager.getTopScores();
        assertEquals(1, scores.size());
        assertEquals(100, scores.get(0));
        
        // Test that additional saves don't crash
        assertDoesNotThrow(() -> {
            manager.saveScore(200);
            manager.saveScore(150);
        });
        
        // Verify scores are properly sorted
        scores = manager.getTopScores();
        assertEquals(3, scores.size());
        assertEquals(200, scores.get(0)); // Highest first
    }
    
    @Test
    void testErrorHandling_CorruptedFile() {
        // Test error handling when file is completely corrupted
        testScoreFile.writeString("completely invalid file content that cannot be parsed", false);
        
        HighScoreManager manager = createTestManager();
        
        // Should not crash and should return empty list
        assertDoesNotThrow(() -> {
            List<Integer> scores = manager.getTopScores();
            assertTrue(scores.isEmpty());
        });
        
        // Should still be able to save new scores
        assertDoesNotThrow(() -> {
            manager.saveScore(100);
        });
    }
    
    @Test
    void testErrorHandling_NegativeScores() {
        // Test handling of negative scores in file
        testScoreFile.writeString("300\n-100\n200\n", false);
        
        HighScoreManager manager = createTestManager();
        List<Integer> scores = manager.getTopScores();
        
        // Should skip negative score
        assertEquals(2, scores.size());
        assertEquals(300, scores.get(0));
        assertEquals(200, scores.get(1));
    }

    /**
     * Creates a test HighScoreManager that uses our test file.
     */
    private HighScoreManager createTestManager() {
        return new HighScoreManager(testScoreFile);
    }
}