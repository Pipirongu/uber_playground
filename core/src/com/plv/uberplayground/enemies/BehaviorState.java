package com.plv.uberplayground.enemies;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages the behavior state for an individual enemy, including behavior switching
 * timers and behavior-specific data storage.
 */
public class BehaviorState {
    
    private SteeringBehaviorType currentType;
    private float switchTimer;
    private float switchInterval;
    private Map<String, Object> behaviorData;
    
    /**
     * Creates a new behavior state with a random initial behavior and timer.
     */
    public BehaviorState() {
        this.currentType = SteeringBehaviorType.getRandomBehavior();
        this.switchInterval = generateRandomInterval();
        this.switchTimer = this.switchInterval;
        this.behaviorData = new HashMap<>();
        initializeBehaviorData();
    }
    
    /**
     * Creates a new behavior state with the specified initial behavior.
     * @param initialBehavior The initial steering behavior type
     */
    public BehaviorState(SteeringBehaviorType initialBehavior) {
        this.currentType = initialBehavior;
        this.switchInterval = generateRandomInterval();
        this.switchTimer = this.switchInterval;
        this.behaviorData = new HashMap<>();
        initializeBehaviorData();
    }
    
    /**
     * Updates the behavior state timer and handles behavior switching.
     * @param delta Time elapsed since last update in seconds
     */
    public void update(float delta) {
        switchTimer -= delta;
        
        if (switchTimer <= 0) {
            switchBehavior();
        }
    }
    
    /**
     * Switches to a new random behavior and resets the timer.
     */
    private void switchBehavior() {
        SteeringBehaviorType newBehavior = SteeringBehaviorType.getRandomBehaviorExcluding(currentType);
        this.currentType = newBehavior;
        this.switchInterval = generateRandomInterval();
        this.switchTimer = this.switchInterval;
        initializeBehaviorData();
    }
    
    /**
     * Generates a random interval between min and max behavior switch times.
     * @return Random interval in seconds
     */
    private float generateRandomInterval() {
        float min = SpawnConfiguration.MIN_BEHAVIOR_SWITCH_INTERVAL;
        float max = SpawnConfiguration.MAX_BEHAVIOR_SWITCH_INTERVAL;
        return min + (float) Math.random() * (max - min);
    }
    
    /**
     * Initializes behavior-specific data based on current behavior type.
     */
    private void initializeBehaviorData() {
        behaviorData.clear();
        
        switch (currentType) {
            case PATROL:
                // Initialize patrol waypoints
                Array<Vector2> waypoints = new Array<>();
                behaviorData.put("waypoints", waypoints);
                behaviorData.put("currentWaypointIndex", 0);
                break;
                
            case ORBIT:
                // Initialize orbit angle
                behaviorData.put("orbitAngle", 0.0f);
                behaviorData.put("orbitRadius", SpawnConfiguration.DEFAULT_ORBIT_RADIUS);
                break;
                
            case INTERCEPT:
                // Initialize intercept prediction data
                behaviorData.put("lastPlayerPosition", new Vector2());
                behaviorData.put("predictedPosition", new Vector2());
                break;
                
            case FLOCK:
                // Initialize flocking data
                behaviorData.put("separationRadius", 50.0f);
                behaviorData.put("alignmentRadius", 100.0f);
                behaviorData.put("cohesionRadius", 150.0f);
                break;
                
            default:
                // No special initialization needed for WANDER, SEEK, FLEE
                break;
        }
    }
    
    /**
     * Returns the current steering behavior type.
     * @return Current behavior type
     */
    public SteeringBehaviorType getCurrentType() {
        return currentType;
    }
    
    /**
     * Returns the remaining time until next behavior switch.
     * @return Time remaining in seconds
     */
    public float getTimeUntilSwitch() {
        return switchTimer;
    }
    
    /**
     * Returns the current switch interval.
     * @return Switch interval in seconds
     */
    public float getSwitchInterval() {
        return switchInterval;
    }
    
    /**
     * Gets behavior-specific data by key.
     * @param key The data key
     * @return The stored data object, or null if not found
     */
    public Object getBehaviorData(String key) {
        return behaviorData.get(key);
    }
    
    /**
     * Sets behavior-specific data by key.
     * @param key The data key
     * @param value The data value to store
     */
    public void setBehaviorData(String key, Object value) {
        behaviorData.put(key, value);
    }
    
    /**
     * Forces an immediate behavior switch to the specified type.
     * @param newBehavior The behavior to switch to
     */
    public void forceBehaviorSwitch(SteeringBehaviorType newBehavior) {
        this.currentType = newBehavior;
        this.switchInterval = generateRandomInterval();
        this.switchTimer = this.switchInterval;
        initializeBehaviorData();
    }
}