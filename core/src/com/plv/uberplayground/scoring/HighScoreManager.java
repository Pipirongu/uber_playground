package com.plv.uberplayground.scoring;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages high score persistence and retrieval operations.
 * 
 * This class handles reading and writing high score data to persistent storage,
 * maintains the top 5 scores in sorted order, and provides caching for performance.
 * All file I/O operations include error handling to ensure game stability.
 */
public class HighScoreManager {
    
    private final FileHandle scoreFile;
    private List<Integer> cachedScores;
    private boolean cacheValid;
    
    /**
     * Creates a new HighScoreManager instance.
     * Initializes the score file handle and cache.
     */
    public HighScoreManager() {
        this.scoreFile = getScoreFile();
        this.cachedScores = new CopyOnWriteArrayList<>();
        this.cacheValid = false;
    }
    
    /**
     * Constructor for testing with custom file handle.
     * 
     * @param scoreFile The file handle to use for score storage
     */
    HighScoreManager(FileHandle scoreFile) {
        this.scoreFile = scoreFile;
        this.cachedScores = new CopyOnWriteArrayList<>();
        this.cacheValid = false;
    }
    
    /**
     * Saves a score to the high score list if it qualifies.
     * Maintains only the top 5 scores in descending order.
     * 
     * @param score The score to save
     */
    public void saveScore(int score) {
        try {
            List<Integer> scores = getTopScores();
            scores.add(score);
            
            // Sort in descending order (highest first)
            scores.sort(Collections.reverseOrder());
            
            // Keep only top 5 scores
            if (scores.size() > ScoreConstants.MAX_HIGH_SCORES) {
                scores = scores.subList(0, ScoreConstants.MAX_HIGH_SCORES);
            }
            
            writeScoresToFile(scores);
            
            // Update cache
            cachedScores = new CopyOnWriteArrayList<>(scores);
            cacheValid = true;
            
        } catch (Exception e) {
            if (Gdx.app != null) {
                Gdx.app.error("HighScoreManager", "Failed to save score: " + score, e);
            }
            // Continue operation without saving - game should not crash
        }
    }
    
    /**
     * Gets the top high scores, using cache when available.
     * 
     * @return List of top scores in descending order (highest first)
     */
    public List<Integer> getTopScores() {
        if (cacheValid && cachedScores != null) {
            return new ArrayList<>(cachedScores);
        }
        
        try {
            List<Integer> scores = readScoresFromFile();
            
            // Update cache
            cachedScores = new CopyOnWriteArrayList<>(scores);
            cacheValid = true;
            
            return new ArrayList<>(scores);
            
        } catch (Exception e) {
            if (Gdx.app != null) {
                Gdx.app.error("HighScoreManager", "Failed to read high scores", e);
            }
            // Return empty list on error - don't crash the game
            return new ArrayList<>();
        }
    }
    
    /**
     * Checks if a score qualifies as a high score.
     * 
     * @param score The score to check
     * @return true if the score would be in the top 5, false otherwise
     */
    public boolean isHighScore(int score) {
        try {
            List<Integer> scores = getTopScores();
            
            // If we have less than max scores, any score qualifies
            if (scores.size() < ScoreConstants.MAX_HIGH_SCORES) {
                return true;
            }
            
            // Check if score is higher than the lowest high score
            int lowestHighScore = scores.get(scores.size() - 1);
            return score > lowestHighScore;
            
        } catch (Exception e) {
            if (Gdx.app != null) {
                Gdx.app.error("HighScoreManager", "Failed to check if score is high score: " + score, e);
            }
            // Assume it's a high score on error - better to save than lose
            return true;
        }
    }
    
    /**
     * Writes the scores to the file system with comprehensive error handling.
     * Creates the file if it doesn't exist and handles various I/O error conditions.
     * Logs errors and continues gracefully without crashing the game.
     * 
     * @param scores List of scores to write (should be sorted in descending order)
     */
    private void writeScoresToFile(List<Integer> scores) {
        if (scores == null) {
            if (Gdx.app != null) {
                Gdx.app.error("HighScoreManager", "Cannot write null scores list to file");
            }
            throw new IllegalArgumentException("Scores list cannot be null");
        }
        
        try {
            // Ensure parent directory exists with error handling
            if (scoreFile.parent() != null && !scoreFile.parent().exists()) {
                try {
                    if (Gdx.app != null) {
                        Gdx.app.debug("HighScoreManager", "Creating parent directory for score file: " + scoreFile.parent().path());
                    }
                    scoreFile.parent().mkdirs();
                } catch (SecurityException se) {
                    if (Gdx.app != null) {
                        Gdx.app.error("HighScoreManager", "Permission denied creating parent directory: " + scoreFile.parent().path(), se);
                    }
                    throw se; // Re-throw to be caught by outer catch block
                } catch (Exception dirException) {
                    if (Gdx.app != null) {
                        Gdx.app.error("HighScoreManager", "Failed to create parent directory: " + scoreFile.parent().path(), dirException);
                    }
                    throw dirException; // Re-throw to be caught by outer catch block
                }
            }
            
            // Build content with validation
            StringBuilder content = new StringBuilder();
            for (Integer score : scores) {
                if (score == null) {
                    if (Gdx.app != null) {
                        Gdx.app.log("HighScoreManager", "Skipping null score value during write");
                    }
                    continue;
                }
                if (score < 0) {
                    if (Gdx.app != null) {
                        Gdx.app.log("HighScoreManager", "Skipping negative score value: " + score);
                    }
                    continue;
                }
                content.append(score).append("\n");
            }
            
            // Write to file with overwrite (false parameter)
            scoreFile.writeString(content.toString(), false);
            
            if (Gdx.app != null) {
                Gdx.app.debug("HighScoreManager", "Successfully wrote " + scores.size() + " scores to file");
            }
            
            // Invalidate cache since we just wrote new data
            cacheValid = false;
            
        } catch (OutOfMemoryError oom) {
            if (Gdx.app != null) {
                Gdx.app.error("HighScoreManager", "Out of memory error during file write - system resources exhausted", oom);
                Gdx.app.error("HighScoreManager", "Failed to save " + scores.size() + " scores due to memory issue. Game will continue without score persistence.");
            }
            // Do NOT throw exception - log error and continue without saving (Requirements 6.1, 6.3)
            
        } catch (Exception e) {
            if (Gdx.app != null) {
                Gdx.app.error("HighScoreManager", "Failed to write scores to file: " + scoreFile.path(), e);
                
                // Provide detailed error information for debugging and user feedback
                if (e instanceof SecurityException) {
                    Gdx.app.error("HighScoreManager", "Permission denied - check file permissions for: " + scoreFile.path());
                } else if (e instanceof java.io.IOException) {
                    Gdx.app.error("HighScoreManager", "I/O error - check disk space and file system for: " + scoreFile.path());
                    // Additional I/O error details
                    String ioMessage = e.getMessage();
                    if (ioMessage != null) {
                        if (ioMessage.toLowerCase().contains("space")) {
                            Gdx.app.error("HighScoreManager", "Possible disk space issue detected");
                        } else if (ioMessage.toLowerCase().contains("access") || ioMessage.toLowerCase().contains("denied")) {
                            Gdx.app.error("HighScoreManager", "File access denied - check permissions and file locks");
                        } else if (ioMessage.toLowerCase().contains("network")) {
                            Gdx.app.error("HighScoreManager", "Network drive issue detected - check network connectivity");
                        }
                    }
                } else {
                    Gdx.app.error("HighScoreManager", "Unexpected error during file write: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                }
                
                // Log the number of scores that failed to save for debugging
                Gdx.app.error("HighScoreManager", "Failed to save " + scores.size() + " scores. Game will continue without score persistence.");
            }
            
            // Do NOT throw exception - log error and continue without saving (Requirements 6.1, 6.3)
            // Game should continue normal operation without crashing
        }
    }
    
    /**
     * Reads scores from the file system with comprehensive error handling and validation.
     * Creates the file if it doesn't exist and handles various parsing errors gracefully.
     * 
     * @return List of scores in descending order, empty list if file doesn't exist or is corrupted
     */
    private List<Integer> readScoresFromFile() {
        List<Integer> scores = new ArrayList<>();
        
        try {
            // Handle file creation when it doesn't exist (Requirement 2.4)
            if (!scoreFile.exists()) {
                if (Gdx.app != null) {
                    Gdx.app.debug("HighScoreManager", "Score file does not exist, creating empty file: " + scoreFile.path());
                }
                
                // Create empty file to satisfy requirement 2.4
                try {
                    // Ensure parent directory exists
                    if (scoreFile.parent() != null && !scoreFile.parent().exists()) {
                        scoreFile.parent().mkdirs();
                    }
                    scoreFile.writeString("", false);
                    if (Gdx.app != null) {
                        Gdx.app.debug("HighScoreManager", "Successfully created empty score file");
                    }
                } catch (SecurityException se) {
                    if (Gdx.app != null) {
                        Gdx.app.error("HighScoreManager", "Permission denied creating score file: " + scoreFile.path(), se);
                    }
                } catch (Exception createException) {
                    if (Gdx.app != null) {
                        Gdx.app.error("HighScoreManager", "Error creating score file: " + scoreFile.path() + " - " + createException.getClass().getSimpleName(), createException);
                    }
                }
                
                return scores; // Return empty list for new file
            }
            
            String content = scoreFile.readString();
            if (content == null || content.trim().isEmpty()) {
                if (Gdx.app != null) {
                    Gdx.app.debug("HighScoreManager", "Score file is empty, returning empty list");
                }
                return scores;
            }
            
            String[] lines = content.split("\n");
            int validScoresCount = 0;
            int skippedLinesCount = 0;
            
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue; // Skip empty lines
                }
                
                try {
                    int score = Integer.parseInt(line);
                    
                    // Validate score value
                    if (score < 0) {
                        if (Gdx.app != null) {
                            Gdx.app.log("HighScoreManager", "Skipping negative score value: " + score);
                        }
                        skippedLinesCount++;
                        continue;
                    }
                    
                    scores.add(score);
                    validScoresCount++;
                    
                } catch (NumberFormatException e) {
                    if (Gdx.app != null) {
                        Gdx.app.log("HighScoreManager", "Skipping invalid score line: '" + line + "' - " + e.getMessage());
                    }
                    skippedLinesCount++;
                    // Continue processing other lines (Requirement 5.5)
                }
            }
            
            if (Gdx.app != null && skippedLinesCount > 0) {
                Gdx.app.log("HighScoreManager", "Parsed " + validScoresCount + " valid scores, skipped " + skippedLinesCount + " invalid lines");
            }
            
            // Sort in descending order to ensure consistency (Requirement 2.3)
            scores.sort(Collections.reverseOrder());
            
            // Ensure we don't have more than max scores (Requirement 2.2)
            if (scores.size() > ScoreConstants.MAX_HIGH_SCORES) {
                if (Gdx.app != null) {
                    Gdx.app.log("HighScoreManager", "Truncating scores list from " + scores.size() + " to " + ScoreConstants.MAX_HIGH_SCORES);
                }
                scores = scores.subList(0, ScoreConstants.MAX_HIGH_SCORES);
            }
            
        } catch (OutOfMemoryError oom) {
            if (Gdx.app != null) {
                Gdx.app.error("HighScoreManager", "Out of memory error during file read - file may be too large", oom);
                Gdx.app.error("HighScoreManager", "Returning empty score list due to memory issue. Game will continue with no high scores displayed.");
            }
            // Return empty list on error (Requirement 6.2)
            scores.clear();
            
        } catch (Exception e) {
            if (Gdx.app != null) {
                Gdx.app.error("HighScoreManager", "Failed to read scores from file: " + scoreFile.path(), e);
                
                // Provide detailed error information for debugging
                if (e instanceof SecurityException) {
                    Gdx.app.error("HighScoreManager", "Permission denied reading score file - check file permissions for: " + scoreFile.path());
                } else if (e instanceof java.io.IOException) {
                    Gdx.app.error("HighScoreManager", "I/O error reading score file - check file system for: " + scoreFile.path());
                    // Additional I/O error details
                    String ioMessage = e.getMessage();
                    if (ioMessage != null) {
                        if (ioMessage.toLowerCase().contains("network")) {
                            Gdx.app.error("HighScoreManager", "Network drive issue detected - check network connectivity");
                        } else if (ioMessage.toLowerCase().contains("corrupt")) {
                            Gdx.app.error("HighScoreManager", "File corruption detected - will return empty score list");
                        }
                    }
                } else {
                    Gdx.app.error("HighScoreManager", "Unexpected error during file read: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                }
                
                Gdx.app.error("HighScoreManager", "Returning empty score list due to read failure. Game will continue with no high scores displayed.");
            }
            
            // Return empty list on error (Requirement 6.2)
            // Game continues normal operation without crashing
            scores.clear();
        }
        
        return scores;
    }
    
    /**
     * Invalidates the cache, forcing a reload from file on next access.
     * Useful for testing or when external changes to the file are expected.
     */
    public void invalidateCache() {
        cacheValid = false;
        cachedScores.clear();
    }
    
    /**
     * Gets the file handle for the high score file.
     * Can be overridden for testing purposes.
     * 
     * @return The FileHandle for the high score file
     */
    FileHandle getScoreFile() {
        if (Gdx.files != null) {
            return Gdx.files.local(ScoreConstants.HIGH_SCORE_FILENAME);
        }
        // Fallback for testing when Gdx is not initialized
        return new FileHandle(ScoreConstants.HIGH_SCORE_FILENAME);
    }
}