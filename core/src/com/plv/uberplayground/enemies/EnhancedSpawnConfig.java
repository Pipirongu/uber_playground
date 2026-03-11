package com.plv.uberplayground.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/**
 * Enhanced configuration system for dynamic enemy spawning.
 * Supports JSON configuration loading, validation, runtime updates, and change notifications.
 */
public class EnhancedSpawnConfig {
    
    /**
     * Interface for listening to configuration changes.
     */
    public interface ConfigurationChangeListener {
        /**
         * Called when configuration values have been updated.
         * 
         * @param config The updated configuration instance
         */
        void onConfigurationChanged(EnhancedSpawnConfig config);
    }
    
    public enum ScalingType {
        LINEAR, EXPONENTIAL, LOGARITHMIC
    }
    
    // Base configuration
    private float baseSpawnRate = 0.2f;
    private float maxSpawnRate = 1.0f;
    private int maxEnemies = 50;
    
    // Scaling parameters
    private float scalingFactor = 1.0f;
    private float targetDuration = 60.0f;
    private ScalingType scalingType = ScalingType.LINEAR;
    
    // Performance thresholds
    private float minFrameRate = 30.0f;
    private float performanceReductionFactor = 0.8f;
    
    // Game state configuration
    private boolean preserveScalingOnPause = true;
    private boolean resetOnGameOver = true;
    private boolean resetOnNewGame = true;
    
    private boolean configurationValid = true;
    
    // Configuration change notification
    private final Array<ConfigurationChangeListener> listeners = new Array<>();
    private String currentConfigPath;
    
    /**
     * Load configuration from JSON file.
     * Falls back to safe defaults if file is missing or invalid.
     * 
     * @param configPath Path to the configuration file
     */
    public void loadFromFile(String configPath) {
        this.currentConfigPath = configPath;
        loadConfigurationInternal(configPath, true);
    }
    
    /**
     * Reload configuration from the current file path.
     * This method enables hot-reload functionality for runtime configuration updates.
     * 
     * @return true if configuration was successfully reloaded, false otherwise
     */
    public boolean reloadConfiguration() {
        if (currentConfigPath == null) {
            logError("Cannot reload configuration: no configuration file path set.");
            return false;
        }
        
        return loadConfigurationInternal(currentConfigPath, false);
    }
    
    /**
     * Internal method to load configuration with optional notification.
     * 
     * @param configPath Path to the configuration file
     * @param suppressNotification If true, configuration change notifications are not sent
     * @return true if configuration was successfully loaded, false otherwise
     */
    private boolean loadConfigurationInternal(String configPath, boolean suppressNotification) {
        try {
            FileHandle configFile = Gdx.files.internal(configPath);
            if (!configFile.exists()) {
                logInfo("Configuration file not found: " + configPath + ". Using defaults.");
                applyDefaults();
                if (!suppressNotification) {
                    notifyConfigurationChanged();
                }
                return false;
            }
            
            Json json = new Json();
            JsonValue root = json.fromJson(null, configFile);
            
            loadSpawnConfiguration(root);
            loadPerformanceConfiguration(root);
            loadGameStateConfiguration(root);
            
            validateConfiguration();
            
            logInfo("Configuration loaded successfully from: " + configPath);
            
            if (!suppressNotification) {
                notifyConfigurationChanged();
            }
            
            return true;
            
        } catch (Exception e) {
            logError("Failed to load configuration: " + e.getMessage());
            applyDefaults();
            if (!suppressNotification) {
                notifyConfigurationChanged();
            }
            return false;
        }
    }
    
    private void loadSpawnConfiguration(JsonValue root) {
        JsonValue spawnConfig = root.get("spawnConfiguration");
        if (spawnConfig != null) {
            baseSpawnRate = spawnConfig.getFloat("baseSpawnRate", baseSpawnRate);
            maxSpawnRate = spawnConfig.getFloat("maxSpawnRate", maxSpawnRate);
            maxEnemies = spawnConfig.getInt("maxEnemies", maxEnemies);
            scalingFactor = spawnConfig.getFloat("scalingFactor", scalingFactor);
            targetDuration = spawnConfig.getFloat("targetDuration", targetDuration);
            
            String scalingTypeStr = spawnConfig.getString("scalingType", "LINEAR");
            try {
                scalingType = ScalingType.valueOf(scalingTypeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                logError("Invalid scaling type: " + scalingTypeStr + ". Using LINEAR.");
                scalingType = ScalingType.LINEAR;
            }
        }
    }
    
    private void loadPerformanceConfiguration(JsonValue root) {
        JsonValue perfConfig = root.get("performanceConfiguration");
        if (perfConfig != null) {
            minFrameRate = perfConfig.getFloat("minFrameRate", minFrameRate);
            performanceReductionFactor = perfConfig.getFloat("performanceReductionFactor", performanceReductionFactor);
        }
    }
    
    private void loadGameStateConfiguration(JsonValue root) {
        JsonValue gameStateConfig = root.get("gameStateConfiguration");
        if (gameStateConfig != null) {
            preserveScalingOnPause = gameStateConfig.getBoolean("preserveScalingOnPause", preserveScalingOnPause);
            resetOnGameOver = gameStateConfig.getBoolean("resetOnGameOver", resetOnGameOver);
            resetOnNewGame = gameStateConfig.getBoolean("resetOnNewGame", resetOnNewGame);
        }
    }
    
    /**
     * Validate all configuration parameters and apply corrections if needed.
     */
    public void validateConfiguration() {
        configurationValid = true;
        
        // Validate spawn rates
        if (baseSpawnRate <= 0 || !Float.isFinite(baseSpawnRate)) {
            logError("Invalid baseSpawnRate: " + baseSpawnRate + ". Using default.");
            baseSpawnRate = 0.2f;
            configurationValid = false;
        }
        
        if (maxSpawnRate <= baseSpawnRate || !Float.isFinite(maxSpawnRate)) {
            logError("maxSpawnRate must be greater than baseSpawnRate. Adjusting.");
            maxSpawnRate = Math.max(baseSpawnRate * 2, 1.0f);
            configurationValid = false;
        }
        
        // Validate enemy limits
        if (maxEnemies <= 0) {
            logError("Invalid maxEnemies: " + maxEnemies + ". Using default.");
            maxEnemies = 50;
            configurationValid = false;
        }
        
        // Validate scaling parameters
        if (scalingFactor < 0 || !Float.isFinite(scalingFactor)) {
            logError("Invalid scalingFactor: " + scalingFactor + ". Using default.");
            scalingFactor = 1.0f;
            configurationValid = false;
        }
        
        if (targetDuration <= 0 || !Float.isFinite(targetDuration)) {
            logError("Invalid targetDuration: " + targetDuration + ". Using default.");
            targetDuration = 60.0f;
            configurationValid = false;
        }
        
        // Validate performance parameters
        if (minFrameRate <= 0 || !Float.isFinite(minFrameRate)) {
            logError("Invalid minFrameRate: " + minFrameRate + ". Using default.");
            minFrameRate = 30.0f;
            configurationValid = false;
        }
        
        if (performanceReductionFactor <= 0 || performanceReductionFactor > 1.0f || !Float.isFinite(performanceReductionFactor)) {
            logError("Invalid performanceReductionFactor: " + performanceReductionFactor + ". Using default.");
            performanceReductionFactor = 0.8f;
            configurationValid = false;
        }
    }
    
    private void logError(String message) {
        if (Gdx.app != null) {
            Gdx.app.error("EnhancedSpawnConfig", message);
        } else {
            System.err.println("EnhancedSpawnConfig: " + message);
        }
    }
    
    private void logInfo(String message) {
        if (Gdx.app != null) {
            Gdx.app.log("EnhancedSpawnConfig", message);
        } else {
            System.out.println("EnhancedSpawnConfig: " + message);
        }
    }
    
    /**
     * Apply safe default values for all configuration parameters.
     */
    public void applyDefaults() {
        baseSpawnRate = 0.2f;
        maxSpawnRate = 1.0f;
        maxEnemies = 50;
        scalingFactor = 1.0f;
        targetDuration = 60.0f;
        scalingType = ScalingType.LINEAR;
        minFrameRate = 30.0f;
        performanceReductionFactor = 0.8f;
        preserveScalingOnPause = true;
        resetOnGameOver = true;
        resetOnNewGame = true;
        configurationValid = true;
        
        logInfo("Applied default configuration values.");
    }
    
    /**
     * Add a listener for configuration changes.
     * 
     * @param listener The listener to add
     */
    public void addConfigurationChangeListener(ConfigurationChangeListener listener) {
        if (listener != null && !listeners.contains(listener, true)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove a configuration change listener.
     * 
     * @param listener The listener to remove
     */
    public void removeConfigurationChangeListener(ConfigurationChangeListener listener) {
        listeners.removeValue(listener, true);
    }
    
    /**
     * Notify all registered listeners that the configuration has changed.
     */
    private void notifyConfigurationChanged() {
        for (ConfigurationChangeListener listener : listeners) {
            try {
                listener.onConfigurationChanged(this);
            } catch (Exception e) {
                logError("Error notifying configuration change listener: " + e.getMessage());
            }
        }
    }
    
    /**
     * Update multiple configuration values at once and notify listeners once.
     * This is more efficient than calling individual setters when updating multiple values.
     * 
     * @param baseSpawnRate New base spawn rate (null to keep current)
     * @param maxSpawnRate New max spawn rate (null to keep current)
     * @param maxEnemies New max enemies (null to keep current)
     * @param scalingFactor New scaling factor (null to keep current)
     * @param targetDuration New target duration (null to keep current)
     * @param scalingType New scaling type (null to keep current)
     */
    public void updateConfiguration(Float baseSpawnRate, Float maxSpawnRate, Integer maxEnemies,
                                  Float scalingFactor, Float targetDuration, ScalingType scalingType) {
        boolean changed = false;
        
        if (baseSpawnRate != null && this.baseSpawnRate != baseSpawnRate) {
            this.baseSpawnRate = baseSpawnRate;
            changed = true;
        }
        
        if (maxSpawnRate != null && this.maxSpawnRate != maxSpawnRate) {
            this.maxSpawnRate = maxSpawnRate;
            changed = true;
        }
        
        if (maxEnemies != null && this.maxEnemies != maxEnemies) {
            this.maxEnemies = maxEnemies;
            changed = true;
        }
        
        if (scalingFactor != null && this.scalingFactor != scalingFactor) {
            this.scalingFactor = scalingFactor;
            changed = true;
        }
        
        if (targetDuration != null && this.targetDuration != targetDuration) {
            this.targetDuration = targetDuration;
            changed = true;
        }
        
        if (scalingType != null && this.scalingType != scalingType) {
            this.scalingType = scalingType;
            changed = true;
        }
        
        if (changed) {
            validateConfiguration();
            notifyConfigurationChanged();
        }
    }
    
    /**
     * Check if the current configuration is valid.
     * 
     * @return true if configuration is valid, false otherwise
     */
    public boolean isConfigurationValid() {
        return configurationValid;
    }
    
    // Getters for all configuration parameters
    
    public float getBaseSpawnRate() {
        return baseSpawnRate;
    }
    
    public float getMaxSpawnRate() {
        return maxSpawnRate;
    }
    
    public int getMaxEnemies() {
        return maxEnemies;
    }
    
    public float getScalingFactor() {
        return scalingFactor;
    }
    
    public float getTargetDuration() {
        return targetDuration;
    }
    
    public ScalingType getScalingType() {
        return scalingType;
    }
    
    public float getMinFrameRate() {
        return minFrameRate;
    }
    
    public float getPerformanceReductionFactor() {
        return performanceReductionFactor;
    }
    
    public boolean isPreserveScalingOnPause() {
        return preserveScalingOnPause;
    }
    
    public boolean isResetOnGameOver() {
        return resetOnGameOver;
    }
    
    public boolean isResetOnNewGame() {
        return resetOnNewGame;
    }
    
    // Setters for runtime configuration updates with validation and notification
    
    public void setBaseSpawnRate(float baseSpawnRate) {
        if (this.baseSpawnRate != baseSpawnRate) {
            float oldValue = this.baseSpawnRate;
            this.baseSpawnRate = baseSpawnRate;
            
            if (!validateSingleParameter("baseSpawnRate", baseSpawnRate)) {
                this.baseSpawnRate = oldValue; // Revert on validation failure
                return;
            }
            
            validateConfiguration();
            notifyConfigurationChanged();
        }
    }
    
    public void setMaxSpawnRate(float maxSpawnRate) {
        if (this.maxSpawnRate != maxSpawnRate) {
            float oldValue = this.maxSpawnRate;
            this.maxSpawnRate = maxSpawnRate;
            
            if (!validateSingleParameter("maxSpawnRate", maxSpawnRate)) {
                this.maxSpawnRate = oldValue; // Revert on validation failure
                return;
            }
            
            validateConfiguration();
            notifyConfigurationChanged();
        }
    }
    
    public void setMaxEnemies(int maxEnemies) {
        if (this.maxEnemies != maxEnemies) {
            int oldValue = this.maxEnemies;
            this.maxEnemies = maxEnemies;
            
            if (!validateSingleParameter("maxEnemies", maxEnemies)) {
                this.maxEnemies = oldValue; // Revert on validation failure
                return;
            }
            
            validateConfiguration();
            notifyConfigurationChanged();
        }
    }
    
    public void setScalingFactor(float scalingFactor) {
        if (this.scalingFactor != scalingFactor) {
            float oldValue = this.scalingFactor;
            this.scalingFactor = scalingFactor;
            
            if (!validateSingleParameter("scalingFactor", scalingFactor)) {
                this.scalingFactor = oldValue; // Revert on validation failure
                return;
            }
            
            validateConfiguration();
            notifyConfigurationChanged();
        }
    }
    
    public void setTargetDuration(float targetDuration) {
        if (this.targetDuration != targetDuration) {
            float oldValue = this.targetDuration;
            this.targetDuration = targetDuration;
            
            if (!validateSingleParameter("targetDuration", targetDuration)) {
                this.targetDuration = oldValue; // Revert on validation failure
                return;
            }
            
            validateConfiguration();
            notifyConfigurationChanged();
        }
    }
    
    public void setScalingType(ScalingType scalingType) {
        if (this.scalingType != scalingType) {
            this.scalingType = scalingType;
            notifyConfigurationChanged();
        }
    }
    
    public void setMinFrameRate(float minFrameRate) {
        if (this.minFrameRate != minFrameRate) {
            float oldValue = this.minFrameRate;
            this.minFrameRate = minFrameRate;
            
            if (!validateSingleParameter("minFrameRate", minFrameRate)) {
                this.minFrameRate = oldValue; // Revert on validation failure
                return;
            }
            
            validateConfiguration();
            notifyConfigurationChanged();
        }
    }
    
    public void setPerformanceReductionFactor(float performanceReductionFactor) {
        if (this.performanceReductionFactor != performanceReductionFactor) {
            float oldValue = this.performanceReductionFactor;
            this.performanceReductionFactor = performanceReductionFactor;
            
            if (!validateSingleParameter("performanceReductionFactor", performanceReductionFactor)) {
                this.performanceReductionFactor = oldValue; // Revert on validation failure
                return;
            }
            
            validateConfiguration();
            notifyConfigurationChanged();
        }
    }
    
    public void setPreserveScalingOnPause(boolean preserveScalingOnPause) {
        if (this.preserveScalingOnPause != preserveScalingOnPause) {
            this.preserveScalingOnPause = preserveScalingOnPause;
            notifyConfigurationChanged();
        }
    }
    
    public void setResetOnGameOver(boolean resetOnGameOver) {
        if (this.resetOnGameOver != resetOnGameOver) {
            this.resetOnGameOver = resetOnGameOver;
            notifyConfigurationChanged();
        }
    }
    
    public void setResetOnNewGame(boolean resetOnNewGame) {
        if (this.resetOnNewGame != resetOnNewGame) {
            this.resetOnNewGame = resetOnNewGame;
            notifyConfigurationChanged();
        }
    }
    
    /**
     * Validate a single parameter before applying it.
     * This provides immediate feedback for runtime configuration changes.
     * 
     * @param parameterName The name of the parameter being validated
     * @param value The value to validate
     * @return true if the parameter is valid, false otherwise
     */
    private boolean validateSingleParameter(String parameterName, Object value) {
        switch (parameterName) {
            case "baseSpawnRate":
                if ((Float) value <= 0 || !Float.isFinite((Float) value)) {
                    logError("Invalid " + parameterName + ": " + value + ". Must be greater than 0.");
                    return false;
                }
                break;
                
            case "maxSpawnRate":
                if ((Float) value <= baseSpawnRate || !Float.isFinite((Float) value)) {
                    logError("Invalid " + parameterName + ": " + value + ". Must be greater than baseSpawnRate (" + baseSpawnRate + ").");
                    return false;
                }
                break;
                
            case "maxEnemies":
                if ((Integer) value <= 0) {
                    logError("Invalid " + parameterName + ": " + value + ". Must be greater than 0.");
                    return false;
                }
                break;
                
            case "scalingFactor":
                if ((Float) value < 0 || !Float.isFinite((Float) value)) {
                    logError("Invalid " + parameterName + ": " + value + ". Must be non-negative.");
                    return false;
                }
                break;
                
            case "targetDuration":
                if ((Float) value <= 0 || !Float.isFinite((Float) value)) {
                    logError("Invalid " + parameterName + ": " + value + ". Must be greater than 0.");
                    return false;
                }
                break;
                
            case "minFrameRate":
                if ((Float) value <= 0 || !Float.isFinite((Float) value)) {
                    logError("Invalid " + parameterName + ": " + value + ". Must be greater than 0.");
                    return false;
                }
                break;
                
            case "performanceReductionFactor":
                if ((Float) value <= 0 || (Float) value > 1.0f || !Float.isFinite((Float) value)) {
                    logError("Invalid " + parameterName + ": " + value + ". Must be between 0 and 1.");
                    return false;
                }
                break;
        }
        
        return true;
    }
}