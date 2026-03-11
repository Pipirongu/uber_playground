package com.plv.uberplayground.scoring;

/**
 * Constants and configuration values for the score tracking system.
 * Centralizes all scoring-related constants for easy maintenance.
 */
public final class ScoreConstants {
    
    // Score values
    /** Points awarded for each enemy kill */
    public static final int POINTS_PER_ENEMY_KILL = 1;
    
    /** Initial score value for new game sessions */
    public static final int INITIAL_SCORE = 0;
    
    /** Maximum number of high scores to maintain */
    public static final int MAX_HIGH_SCORES = 5;
    
    // File configuration
    /** Filename for persistent high score storage */
    public static final String HIGH_SCORE_FILENAME = "highscores.txt";
    
    // Display configuration
    /** Default font size for score display */
    public static final float SCORE_FONT_SIZE = 24f;
    
    /** Y position offset from top of screen for score display */
    public static final float SCORE_DISPLAY_Y_OFFSET = 50f;
    
    // Prevent instantiation
    private ScoreConstants() {
        throw new AssertionError("ScoreConstants should not be instantiated");
    }
}