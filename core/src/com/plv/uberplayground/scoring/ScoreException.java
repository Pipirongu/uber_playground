package com.plv.uberplayground.scoring;

/**
 * Exception thrown when score-related operations fail.
 * Provides specific error handling for the score tracking system.
 */
public class ScoreException extends Exception {
    
    /**
     * Creates a new ScoreException with the specified message.
     * 
     * @param message The error message
     */
    public ScoreException(String message) {
        super(message);
    }
    
    /**
     * Creates a new ScoreException with the specified message and cause.
     * 
     * @param message The error message
     * @param cause The underlying cause of the exception
     */
    public ScoreException(String message, Throwable cause) {
        super(message, cause);
    }
}