/**
 * Score tracking system for the UberPlayground LibGDX game.
 * 
 * This package provides a comprehensive scoring mechanism that includes:
 * - Real-time score tracking during gameplay
 * - Persistent high score storage and management
 * - Observer pattern for score change notifications
 * - Error-resilient file I/O operations
 * 
 * Key Components:
 * - {@link com.plv.uberplayground.scoring.ScoreTracker} - Tracks current game score
 * - {@link com.plv.uberplayground.scoring.ScoreDisplay} - Renders score on screen
 * - {@link com.plv.uberplayground.scoring.HighScoreManager} - Manages persistent high scores
 * - {@link com.plv.uberplayground.scoring.HighScoreScreen} - Displays high score list
 * 
 * The system follows the observer pattern where score changes are immediately
 * propagated to registered listeners, ensuring real-time display updates.
 * 
 * @author UberPlayground Score System
 * @version 1.0
 */
package com.plv.uberplayground.scoring;