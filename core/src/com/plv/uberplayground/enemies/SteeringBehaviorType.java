package com.plv.uberplayground.enemies;

/**
 * Enumeration of available steering behaviors for enemy AI.
 * Each behavior type represents a different movement pattern and decision-making strategy.
 */
public enum SteeringBehaviorType {
    
    /** Random exploration movement - existing behavior */
    WANDER,
    
    /** Direct pursuit of the player - existing behavior */
    SEEK,
    
    /** Evasive movement away from the player */
    FLEE,
    
    /** Waypoint-based movement following predefined paths */
    PATROL,
    
    /** Circular movement around the player at a fixed distance */
    ORBIT,
    
    /** Predictive movement to intercept the player's future position */
    INTERCEPT,
    
    /** Group coordination behavior for multiple enemies (advanced) */
    FLOCK;
    
    /**
     * Returns a random steering behavior type from all available options.
     * @return A randomly selected SteeringBehaviorType
     */
    public static SteeringBehaviorType getRandomBehavior() {
        SteeringBehaviorType[] values = values();
        return values[(int) (Math.random() * values.length)];
    }
    
    /**
     * Returns a random steering behavior type different from the current one.
     * @param current The current behavior to exclude from selection
     * @return A randomly selected SteeringBehaviorType different from current
     */
    public static SteeringBehaviorType getRandomBehaviorExcluding(SteeringBehaviorType current) {
        SteeringBehaviorType[] values = values();
        if (values.length <= 1) {
            return current; // No alternatives available
        }
        
        SteeringBehaviorType newBehavior;
        do {
            newBehavior = values[(int) (Math.random() * values.length)];
        } while (newBehavior == current);
        
        return newBehavior;
    }
}