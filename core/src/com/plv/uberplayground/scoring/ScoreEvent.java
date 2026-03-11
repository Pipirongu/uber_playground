package com.plv.uberplayground.scoring;

/**
 * Represents different types of scoring events that can occur during gameplay.
 * Used to categorize and handle different score-affecting actions.
 */
public enum ScoreEvent {
    
    /** Event triggered when an enemy is destroyed by the player */
    ENEMY_KILL(ScoreConstants.POINTS_PER_ENEMY_KILL),
    
    /** Event triggered when a new game session starts (resets score) */
    GAME_START(0),
    
    /** Event triggered when the game session ends */
    GAME_OVER(0);
    
    private final int pointValue;
    
    ScoreEvent(int pointValue) {
        this.pointValue = pointValue;
    }
    
    /**
     * Gets the point value associated with this score event.
     * 
     * @return The number of points this event is worth
     */
    public int getPointValue() {
        return pointValue;
    }
}