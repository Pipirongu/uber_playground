package com.plv.uberplayground.scoring;

import com.badlogic.gdx.files.FileHandle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for file format compliance validation.
 * Validates that the high score file format implementation matches the specification exactly.
 * 
 * **Validates: Requirements 5.1, 5.2, 5.3, 5.4, 5.5**
 */
class FileFormatComplianceTest {
    
    @TempDir
    Path tempDir;
    
    private FileHandle testScoreFile;
    private HighScoreManager manager;
    
    @BeforeEach
    void setUp() {
        // Create a test file in temp directory
        File scoreFile = tempDir.resolve(ScoreConstants.HIGH_SCORE_FILENAME).toFile();
        testScoreFile = new FileHandle(scoreFile);
        manager = new HighScoreManager(testScoreFile);
    }
    
    /**
     * Test Requirement 5.1: Score file located in project directory with filename "highscores.txt"
     */
    @Test
    void testFileLocationAndNaming() {
        // Verify the filename constant matches specification
        assertEquals("highscores.txt", ScoreConstants.HIGH_SCORE_FILENAME, 
            "File name must be 'highscores.txt' as per Requirement 5.1");
        
        // Verify HighScoreManager uses the correct filename
        HighScoreManager defaultManager = new HighScoreManager();
        FileHandle defaultFile = defaultManager.getScoreFile();
        assertTrue(defaultFile.path().endsWith("highscores.txt"), 
            "HighScoreManager must use 'highscores.txt' filename as per Requirement 5.1");
        
        // Verify file is created in project directory (local file)
        assertTrue(defaultFile.path().contains("highscores.txt"), 
            "Score file must be located in project directory as per Requirement 5.1");
    }
    
    /**
     * Test Requirement 5.2: Store each high score as single integer on its own line
     */
    @Test
    void testSingleIntegerPerLine() {
        // Save multiple scores
        manager.saveScore(1250);
        manager.saveScore(1100);
        manager.saveScore(950);
        
        // Read raw file content
        String fileContent = testScoreFile.readString();
        String[] lines = fileContent.split("\n");
        
        // Verify each line contains only a single integer
        assertEquals(3, lines.length, "File should have exactly 3 lines for 3 scores");
        
        for (String line : lines) {
            String trimmedLine = line.trim();
            assertFalse(trimmedLine.isEmpty(), "No line should be empty");
            
            // Verify line contains only digits (single integer)
            assertTrue(trimmedLine.matches("\\d+"), 
                "Each line must contain only a single integer as per Requirement 5.2, found: '" + trimmedLine + "'");
            
            // Verify it's a valid integer
            assertDoesNotThrow(() -> Integer.parseInt(trimmedLine), 
                "Each line must be parseable as an integer as per Requirement 5.2");
        }
        
        // Verify specific format: integer + newline
        assertEquals("1250\n1100\n950\n", fileContent, 
            "File format must be: integer + newline for each score as per Requirement 5.2");
    }
    
    /**
     * Test Requirement 5.3: Overwrite entire file with updated top 5 scores
     */
    @Test
    void testFileOverwriteBehavior() {
        // Initial save - create file with 3 scores
        manager.saveScore(100);
        manager.saveScore(200);
        manager.saveScore(150);
        
        String initialContent = testScoreFile.readString();
        assertEquals("200\n150\n100\n", initialContent, "Initial file should contain 3 scores");
        
        // Add more scores to trigger overwrite
        manager.saveScore(300);
        manager.saveScore(250);
        manager.saveScore(175);
        
        // Verify file is completely overwritten (not appended)
        String finalContent = testScoreFile.readString();
        assertEquals("300\n250\n200\n175\n150\n", finalContent, 
            "File must be completely overwritten with updated top 5 scores as per Requirement 5.3");
        
        // Verify old scores below top 5 are removed
        assertFalse(finalContent.contains("100"), 
            "Scores not in top 5 must be removed during overwrite as per Requirement 5.3");
    }
    
    /**
     * Test Requirement 5.3: Enforce maximum 5 scores limit
     */
    @Test
    void testTop5ScoresLimit() {
        // Save more than 5 scores
        for (int i = 1; i <= 10; i++) {
            manager.saveScore(i * 100); // 100, 200, 300, ..., 1000
        }
        
        // Verify file contains exactly 5 scores (top 5)
        String fileContent = testScoreFile.readString();
        String[] lines = fileContent.split("\n");
        
        assertEquals(5, lines.length, 
            "File must contain exactly 5 scores maximum as per Requirement 5.3");
        
        // Verify it's the top 5 scores in descending order
        assertEquals("1000\n900\n800\n700\n600\n", fileContent, 
            "File must contain only top 5 scores in descending order as per Requirement 5.3");
    }
    
    /**
     * Test Requirement 5.4: Parse each line as integer score value
     */
    @Test
    void testIntegerParsing() {
        // Create file with valid integer lines
        testScoreFile.writeString("1250\n1100\n950\n800\n650\n", false);
        
        List<Integer> scores = manager.getTopScores();
        
        // Verify all lines are parsed as integers
        assertEquals(5, scores.size(), "All valid integer lines should be parsed");
        assertEquals(1250, scores.get(0), "First line should be parsed as integer 1250");
        assertEquals(1100, scores.get(1), "Second line should be parsed as integer 1100");
        assertEquals(950, scores.get(2), "Third line should be parsed as integer 950");
        assertEquals(800, scores.get(3), "Fourth line should be parsed as integer 800");
        assertEquals(650, scores.get(4), "Fifth line should be parsed as integer 650");
        
        // Verify parsing maintains integer values exactly
        for (Integer score : scores) {
            assertNotNull(score, "Parsed scores must not be null");
            assertTrue(score >= 0, "Parsed scores must be non-negative integers");
        }
    }
    
    /**
     * Test Requirement 5.5: Skip lines that cannot be parsed as integers
     */
    @Test
    void testInvalidLineParsing() {
        // Create file with mix of valid and invalid lines
        String mixedContent = "1250\n" +           // Valid
                             "invalid_text\n" +    // Invalid - text
                             "1100\n" +            // Valid
                             "12.5\n" +            // Invalid - decimal
                             "950\n" +             // Valid
                             "\n" +                // Invalid - empty
                             "800abc\n" +          // Invalid - mixed
                             "650\n" +             // Valid
                             "-100\n";             // Invalid - negative (implementation skips these)
        
        testScoreFile.writeString(mixedContent, false);
        
        List<Integer> scores = manager.getTopScores();
        
        // Verify only valid integer lines are parsed
        assertEquals(4, scores.size(), 
            "Only valid integer lines should be parsed as per Requirement 5.5");
        
        // Verify correct values are parsed (in descending order)
        assertEquals(1250, scores.get(0), "Valid integer 1250 should be parsed");
        assertEquals(1100, scores.get(1), "Valid integer 1100 should be parsed");
        assertEquals(950, scores.get(2), "Valid integer 950 should be parsed");
        assertEquals(650, scores.get(3), "Valid integer 650 should be parsed");
        
        // Verify invalid lines are skipped
        assertFalse(scores.contains(-100), "Negative numbers should be skipped");
        
        // Verify no exceptions are thrown during parsing
        assertDoesNotThrow(() -> manager.getTopScores(), 
            "Invalid lines should be skipped without throwing exceptions as per Requirement 5.5");
    }
    
    /**
     * Test edge case: Empty lines and whitespace handling
     */
    @Test
    void testWhitespaceHandling() {
        // Create file with whitespace variations
        String whitespaceContent = "  1250  \n" +    // Leading/trailing spaces
                                  "\n" +              // Empty line
                                  "1100\n" +          // Normal line
                                  "   \n" +           // Whitespace only
                                  "950\n";            // Normal line
        
        testScoreFile.writeString(whitespaceContent, false);
        
        List<Integer> scores = manager.getTopScores();
        
        // Verify whitespace is handled correctly
        assertEquals(3, scores.size(), "Whitespace should be trimmed and empty lines skipped");
        assertEquals(1250, scores.get(0), "Leading/trailing whitespace should be trimmed");
        assertEquals(1100, scores.get(1), "Normal lines should parse correctly");
        assertEquals(950, scores.get(2), "Normal lines should parse correctly");
    }
    
    /**
     * Test edge case: Large integer values
     */
    @Test
    void testLargeIntegerValues() {
        // Test with large but valid integer values
        int maxScore = Integer.MAX_VALUE - 1;
        int largeScore = 2000000000; // Large but valid
        
        manager.saveScore(maxScore);
        manager.saveScore(largeScore);
        manager.saveScore(1000000);
        
        List<Integer> scores = manager.getTopScores();
        
        assertEquals(3, scores.size(), "Large integer values should be handled correctly");
        assertEquals(maxScore, scores.get(0), "Maximum integer values should be parsed correctly");
        assertEquals(largeScore, scores.get(1), "Large integer values should be parsed correctly");
        assertEquals(1000000, scores.get(2), "Regular integer values should be parsed correctly");
        
        // Verify file format is maintained with large numbers
        String fileContent = testScoreFile.readString();
        assertTrue(fileContent.contains(String.valueOf(maxScore)), 
            "Large integers should be written to file correctly");
    }
    
    /**
     * Test complete file format specification compliance
     */
    @Test
    void testCompleteFormatSpecification() {
        // Save exactly 5 scores to test complete format
        manager.saveScore(1250);
        manager.saveScore(1100);
        manager.saveScore(950);
        manager.saveScore(800);
        manager.saveScore(650);
        
        String fileContent = testScoreFile.readString();
        
        // Verify exact format matches specification example
        String expectedFormat = "1250\n1100\n950\n800\n650\n";
        assertEquals(expectedFormat, fileContent, 
            "File format must exactly match specification: [score]\\n for each line");
        
        // Verify no extra characters or formatting
        assertFalse(fileContent.contains(" "), "File should not contain spaces");
        assertFalse(fileContent.contains("\t"), "File should not contain tabs");
        assertFalse(fileContent.contains("\r"), "File should not contain carriage returns");
        
        // Verify proper line termination
        assertTrue(fileContent.endsWith("\n"), "File should end with newline");
        
        // Verify each line is properly terminated
        String[] lines = fileContent.split("\n");
        assertEquals(5, lines.length, "Should have exactly 5 lines");
        
        for (String line : lines) {
            assertFalse(line.isEmpty(), "No line should be empty");
            assertTrue(line.matches("\\d+"), "Each line should contain only digits");
        }
    }
}