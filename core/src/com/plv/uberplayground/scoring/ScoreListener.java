package com.plv.uberplayground.scoring;

/**
 * Interface for components that need to be notified of score changes.
 * Implements the observer pattern for real-time score updates.
 * 
 * This interface allows components like ScoreDisplay to receive immediate
 * notifications when the player's score changes during gameplay.
 */
public interface ScoreListener {
    
    /**
     * Called when the player's score changes.
     * 
     * @param newScore The updated score value
     */
    void onScoreChanged(int newScore);
}