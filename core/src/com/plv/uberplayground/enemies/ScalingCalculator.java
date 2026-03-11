package com.plv.uberplayground.enemies;

/**
 * Mathematical engine for calculating dynamic spawn rates based on game duration.
 * Implements linear, exponential, and logarithmic scaling algorithms with bounds enforcement.
 */
public class ScalingCalculator {
    
    private final EnhancedSpawnConfig config;
    
    /**
     * Create a new ScalingCalculator with the provided configuration.
     * 
     * @param config The enhanced spawn configuration containing scaling parameters
     */
    public ScalingCalculator(EnhancedSpawnConfig config) {
        this.config = config;
    }
    
    /**
     * Calculate the current spawn rate based on game duration and scaling configuration.
     * Uses the formula: currentSpawnRate = baseSpawnRate * (1 + scalingFactor * progressionFunction(gameDuration))
     * 
     * @param gameDuration The elapsed game time in seconds
     * @return The calculated spawn rate, bounded by maxSpawnRate
     */
    public float calculateSpawnRate(float gameDuration) {
        if (gameDuration < 0) {
            return config.getBaseSpawnRate();
        }
        
        float scalingFactor = calculateScalingFactor(gameDuration);
        float currentSpawnRate = config.getBaseSpawnRate() * (1 + config.getScalingFactor() * scalingFactor);
        
        // Enforce maximum spawn rate bounds
        return Math.min(currentSpawnRate, config.getMaxSpawnRate());
    }
    
    /**
     * Calculate the scaling factor based on game duration using the configured progression function.
     * 
     * @param gameDuration The elapsed game time in seconds
     * @return The scaling factor (0.0 to 1.0 for most functions)
     */
    public float calculateScalingFactor(float gameDuration) {
        if (gameDuration <= 0) {
            return 0.0f;
        }
        
        float targetDuration = config.getTargetDuration();
        float normalizedDuration = gameDuration / targetDuration;
        
        switch (config.getScalingType()) {
            case LINEAR:
                return calculateLinearProgression(normalizedDuration);
                
            case EXPONENTIAL:
                return calculateExponentialProgression(normalizedDuration);
                
            case LOGARITHMIC:
                return calculateLogarithmicProgression(normalizedDuration);
                
            default:
                return calculateLinearProgression(normalizedDuration);
        }
    }
    
    /**
     * Linear progression function: gameDuration / targetDuration
     * 
     * @param normalizedDuration Game duration normalized by target duration
     * @return Linear scaling factor
     */
    private float calculateLinearProgression(float normalizedDuration) {
        return normalizedDuration;
    }
    
    /**
     * Exponential progression function: (e^(gameDuration/targetDuration) - 1) / (e - 1)
     * This provides faster scaling that accelerates over time.
     * 
     * @param normalizedDuration Game duration normalized by target duration
     * @return Exponential scaling factor
     */
    private float calculateExponentialProgression(float normalizedDuration) {
        float e = (float) Math.E;
        return (float) ((Math.exp(normalizedDuration) - 1) / (e - 1));
    }
    
    /**
     * Logarithmic progression function: log(1 + gameDuration) / log(1 + targetDuration)
     * This provides slower scaling that decelerates over time.
     * 
     * @param normalizedDuration Game duration normalized by target duration
     * @return Logarithmic scaling factor
     */
    private float calculateLogarithmicProgression(float normalizedDuration) {
        float targetDuration = config.getTargetDuration();
        return (float) (Math.log(1 + normalizedDuration * targetDuration) / Math.log(1 + targetDuration));
    }
    
    /**
     * Check if a proposed spawn rate is within performance limits.
     * 
     * @param proposedRate The spawn rate to validate
     * @return true if the rate is within acceptable performance bounds
     */
    public boolean isWithinPerformanceLimits(float proposedRate) {
        return proposedRate <= config.getMaxSpawnRate() && proposedRate >= 0;
    }
    
    /**
     * Set the scaling type for the calculator.
     * This updates the configuration and affects future calculations.
     * 
     * @param type The new scaling type to use
     */
    public void setScalingType(EnhancedSpawnConfig.ScalingType type) {
        config.setScalingType(type);
    }
    
    /**
     * Get the current scaling type being used.
     * 
     * @return The current scaling type
     */
    public EnhancedSpawnConfig.ScalingType getScalingType() {
        return config.getScalingType();
    }
    
    /**
     * Get the configuration object used by this calculator.
     * 
     * @return The enhanced spawn configuration
     */
    public EnhancedSpawnConfig getConfig() {
        return config;
    }
}