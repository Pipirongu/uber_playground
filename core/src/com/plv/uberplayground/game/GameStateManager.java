package com.plv.uberplayground.game;

import com.badlogic.gdx.utils.Array;

/**
 * Manages game state transitions and provides notifications to listeners.
 * Supports game duration tracking with pause/resume functionality.
 * 
 * This class is part of the dynamic enemy spawning system and coordinates
 * with the spawn controller for proper state handling.
 */
public class GameStateManager {
    
    /**
     * Enumeration of possible game states
     */
    public enum GameState {
        MENU,       // Main menu or other UI screens
        PLAYING,    // Active gameplay
        PAUSED,     // Game paused by user
        GAME_OVER   // Game ended (player died)
    }
    
    /**
     * Interface for listening to game state changes
     */
    public interface GameStateListener {
        /**
         * Called when the game state changes
         * @param oldState The previous game state
         * @param newState The new game state
         */
        void onStateChanged(GameState oldState, GameState newState);
        
        /**
         * Called when game duration is updated (during PLAYING state)
         * @param totalDuration Total game duration in seconds
         * @param activeDuration Active gameplay duration (excluding paused time)
         */
        void onDurationUpdated(float totalDuration, float activeDuration);
    }
    
    /**
     * Data structure for tracking game session metrics
     */
    public static class GameSessionData {
        private float totalGameDuration;
        private float pausedDuration;
        private float activeDuration;
        private boolean scalingActive;
        
        public GameSessionData() {
            reset();
        }
        
        public void reset() {
            totalGameDuration = 0f;
            pausedDuration = 0f;
            activeDuration = 0f;
            scalingActive = false;
        }
        
        public float getTotalGameDuration() { return totalGameDuration; }
        public float getPausedDuration() { return pausedDuration; }
        public float getActiveDuration() { return activeDuration; }
        public boolean isScalingActive() { return scalingActive; }
        
        void setTotalGameDuration(float duration) { this.totalGameDuration = duration; }
        void setPausedDuration(float duration) { this.pausedDuration = duration; }
        void setActiveDuration(float duration) { this.activeDuration = duration; }
        void setScalingActive(boolean active) { this.scalingActive = active; }
    }
    
    // State management
    private GameState currentState;
    private GameState previousState;
    private final Array<GameStateListener> listeners;
    
    // Duration tracking
    private final GameSessionData sessionData;
    private float pauseStartTime;
    private boolean isPauseTimerActive;
    
    /**
     * Creates a new GameStateManager with initial MENU state
     */
    public GameStateManager() {
        this.currentState = GameState.MENU;
        this.previousState = GameState.MENU;
        this.listeners = new Array<>();
        this.sessionData = new GameSessionData();
        this.pauseStartTime = 0f;
        this.isPauseTimerActive = false;
    }
    
    /**
     * Updates the game duration tracking. Should be called every frame.
     * @param delta Time elapsed since last frame in seconds
     */
    public void update(float delta) {
        if (currentState == GameState.PLAYING) {
            // Update total and active duration during gameplay
            sessionData.setTotalGameDuration(sessionData.getTotalGameDuration() + delta);
            sessionData.setActiveDuration(sessionData.getActiveDuration() + delta);
            sessionData.setScalingActive(true);
            
            // Notify listeners of duration update
            notifyDurationUpdate();
        } else if (currentState == GameState.PAUSED) {
            // Update total duration but not active duration during pause
            sessionData.setTotalGameDuration(sessionData.getTotalGameDuration() + delta);
            
            // Track paused time
            if (isPauseTimerActive) {
                sessionData.setPausedDuration(sessionData.getPausedDuration() + delta);
            }
        }
    }
    
    /**
     * Sets the current game state and notifies listeners
     * @param newState The new game state to transition to
     */
    public void setState(GameState newState) {
        if (newState == null) {
            throw new IllegalArgumentException("Game state cannot be null");
        }
        
        if (currentState != newState) {
            GameState oldState = currentState;
            previousState = currentState;
            currentState = newState;
            
            // Handle state-specific logic
            handleStateTransition(oldState, newState);
            
            // Notify all listeners
            notifyStateChange(oldState, newState);
        }
    }
    
    /**
     * Gets the current game state
     * @return The current game state
     */
    public GameState getCurrentState() {
        return currentState;
    }
    
    /**
     * Gets the previous game state
     * @return The previous game state
     */
    public GameState getPreviousState() {
        return previousState;
    }
    
    /**
     * Checks if the game is currently in an active gameplay state
     * @return true if the game state is PLAYING, false otherwise
     */
    public boolean isGameplayActive() {
        return currentState == GameState.PLAYING;
    }
    
    /**
     * Checks if the game is currently paused
     * @return true if the game state is PAUSED, false otherwise
     */
    public boolean isPaused() {
        return currentState == GameState.PAUSED;
    }
    
    /**
     * Checks if the game is over
     * @return true if the game state is GAME_OVER, false otherwise
     */
    public boolean isGameOver() {
        return currentState == GameState.GAME_OVER;
    }
    
    /**
     * Adds a listener for game state changes
     * @param listener The listener to add
     */
    public void addListener(GameStateListener listener) {
        if (listener != null && !listeners.contains(listener, true)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Removes a listener for game state changes
     * @param listener The listener to remove
     */
    public void removeListener(GameStateListener listener) {
        listeners.removeValue(listener, true);
    }
    
    /**
     * Removes all listeners
     */
    public void clearListeners() {
        listeners.clear();
    }
    
    /**
     * Gets the current game session data
     * @return A copy of the current session data
     */
    public GameSessionData getSessionData() {
        // Return a copy to prevent external modification
        GameSessionData copy = new GameSessionData();
        copy.setTotalGameDuration(sessionData.getTotalGameDuration());
        copy.setPausedDuration(sessionData.getPausedDuration());
        copy.setActiveDuration(sessionData.getActiveDuration());
        copy.setScalingActive(sessionData.isScalingActive());
        return copy;
    }
    
    /**
     * Resets the game session data for a new game
     */
    public void resetSession() {
        sessionData.reset();
        pauseStartTime = 0f;
        isPauseTimerActive = false;
    }
    
    /**
     * Convenience method to start a new game
     */
    public void startNewGame() {
        resetSession();
        setState(GameState.PLAYING);
    }
    
    /**
     * Convenience method to pause the game
     */
    public void pauseGame() {
        if (currentState == GameState.PLAYING) {
            setState(GameState.PAUSED);
        }
    }
    
    /**
     * Convenience method to resume the game
     */
    public void resumeGame() {
        if (currentState == GameState.PAUSED) {
            setState(GameState.PLAYING);
        }
    }
    
    /**
     * Convenience method to end the game
     */
    public void endGame() {
        setState(GameState.GAME_OVER);
    }
    
    /**
     * Convenience method to return to menu
     */
    public void returnToMenu() {
        setState(GameState.MENU);
    }
    
    /**
     * Handles state-specific logic during transitions
     */
    private void handleStateTransition(GameState oldState, GameState newState) {
        switch (newState) {
            case PLAYING:
                // Resume active duration tracking
                isPauseTimerActive = false;
                if (oldState == GameState.MENU || oldState == GameState.GAME_OVER) {
                    // Starting new game - reset session data
                    resetSession();
                }
                break;
                
            case PAUSED:
                // Start tracking pause time
                pauseStartTime = sessionData.getTotalGameDuration();
                isPauseTimerActive = true;
                sessionData.setScalingActive(false);
                break;
                
            case GAME_OVER:
                // Stop all tracking
                isPauseTimerActive = false;
                sessionData.setScalingActive(false);
                break;
                
            case MENU:
                // Reset everything when returning to menu
                resetSession();
                break;
        }
    }
    
    /**
     * Notifies all listeners of state changes
     */
    private void notifyStateChange(GameState oldState, GameState newState) {
        for (GameStateListener listener : listeners) {
            try {
                listener.onStateChanged(oldState, newState);
            } catch (Exception e) {
                // Log error but continue notifying other listeners
                System.err.println("Error notifying game state listener: " + e.getMessage());
            }
        }
    }
    
    /**
     * Notifies all listeners of duration updates
     */
    private void notifyDurationUpdate() {
        for (GameStateListener listener : listeners) {
            try {
                listener.onDurationUpdated(sessionData.getTotalGameDuration(), sessionData.getActiveDuration());
            } catch (Exception e) {
                // Log error but continue notifying other listeners
                System.err.println("Error notifying duration update listener: " + e.getMessage());
            }
        }
    }
}