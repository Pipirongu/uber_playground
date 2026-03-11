package com.plv.uberplayground.scoring;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

/**
 * Thread-safe score tracking component for the UberPlayground game.
 * 
 * This class manages the current game session score using atomic operations
 * to ensure thread safety in LibGDX's multi-threaded environment. It implements
 * the observer pattern to notify registered listeners of score changes.
 * 
 * Key features:
 * - Thread-safe score operations using AtomicInteger
 * - Real-time listener notifications on score changes
 * - Support for score increments, resets, and current value queries
 * - Memory-safe listener management with CopyOnWriteArrayList
 */
public class ScoreTracker {
    
    /** Thread-safe score counter */
    private final AtomicInteger currentScore;
    
    /** Thread-safe list of score change listeners */
    private final List<ScoreListener> listeners;
    
    /**
     * Creates a new ScoreTracker with initial score of zero.
     */
    public ScoreTracker() {
        this.currentScore = new AtomicInteger(ScoreConstants.INITIAL_SCORE);
        this.listeners = new CopyOnWriteArrayList<>();
    }
    
    /**
     * Increments the current score by the standard enemy kill point value.
     * This method is thread-safe and will notify all registered listeners
     * of the score change.
     * 
     * Validates: Requirements 1.1 - Score increments on enemy kill events
     */
    public void incrementScore() {
        int newScore = currentScore.addAndGet(ScoreConstants.POINTS_PER_ENEMY_KILL);
        notifyListeners(newScore);
    }
    
    /**
     * Gets the current score value.
     * This method is thread-safe and returns the most up-to-date score.
     * 
     * @return The current score value
     */
    public int getCurrentScore() {
        return currentScore.get();
    }
    
    /**
     * Resets the score to zero for a new game session.
     * This method is thread-safe and will notify all registered listeners
     * of the score reset.
     * 
     * Validates: Requirements 4.3 - Score resets to zero when starting new game sessions
     */
    public void resetScore() {
        currentScore.set(ScoreConstants.INITIAL_SCORE);
        notifyListeners(ScoreConstants.INITIAL_SCORE);
    }
    
    /**
     * Registers a listener to receive score change notifications.
     * The listener will be notified immediately when the score changes.
     * 
     * @param listener The listener to register for score updates
     * @throws IllegalArgumentException if listener is null
     */
    public void addScoreListener(ScoreListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Score listener cannot be null");
        }
        listeners.add(listener);
    }
    
    /**
     * Removes a previously registered score listener.
     * 
     * @param listener The listener to remove
     * @return true if the listener was found and removed, false otherwise
     */
    public boolean removeScoreListener(ScoreListener listener) {
        return listeners.remove(listener);
    }
    
    /**
     * Gets the number of currently registered listeners.
     * Useful for testing and debugging.
     * 
     * @return The number of registered listeners
     */
    public int getListenerCount() {
        return listeners.size();
    }
    
    /**
     * Notifies all registered listeners of a score change.
     * This method is called internally whenever the score is modified.
     * 
     * @param newScore The new score value to broadcast
     */
    private void notifyListeners(int newScore) {
        for (ScoreListener listener : listeners) {
            try {
                listener.onScoreChanged(newScore);
            } catch (Exception e) {
                // Log error but continue notifying other listeners
                // This prevents one faulty listener from breaking the entire system
                System.err.println("Error notifying score listener: " + e.getMessage());
            }
        }
    }
}