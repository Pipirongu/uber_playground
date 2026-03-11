package com.plv.uberplayground.scoring;

/**
 * Represents a high score entry with score value and metadata.
 * Provides a structured way to handle high score data beyond just integer values.
 */
public class HighScore implements Comparable<HighScore> {
    
    private final int score;
    private final long timestamp;
    
    /**
     * Creates a new high score entry.
     * 
     * @param score The score value
     */
    public HighScore(int score) {
        this.score = score;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Creates a high score entry with a specific timestamp.
     * Used when loading scores from file.
     * 
     * @param score The score value
     * @param timestamp The timestamp when this score was achieved
     */
    public HighScore(int score, long timestamp) {
        this.score = score;
        this.timestamp = timestamp;
    }
    
    /**
     * Gets the score value.
     * 
     * @return The score value
     */
    public int getScore() {
        return score;
    }
    
    /**
     * Gets the timestamp when this score was achieved.
     * 
     * @return The timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Compares high scores for sorting (highest score first).
     * 
     * @param other The other high score to compare to
     * @return Negative if this score is higher, positive if lower, 0 if equal
     */
    @Override
    public int compareTo(HighScore other) {
        // Sort in descending order (highest scores first)
        return Integer.compare(other.score, this.score);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        HighScore highScore = (HighScore) obj;
        return score == highScore.score && timestamp == highScore.timestamp;
    }
    
    @Override
    public int hashCode() {
        return 31 * score + Long.hashCode(timestamp);
    }
    
    @Override
    public String toString() {
        return "HighScore{score=" + score + ", timestamp=" + timestamp + "}";
    }
}