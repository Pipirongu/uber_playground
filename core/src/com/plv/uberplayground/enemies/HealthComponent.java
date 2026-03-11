package com.plv.uberplayground.enemies;

/**
 * Component for managing enemy health state and damage processing.
 * Tracks current health, maximum health, and provides health percentage calculations.
 */
public class HealthComponent {
    
    private int currentHealth;
    private int maxHealth;
    private boolean isAlive;
    private float lastDamageTime;
    
    /**
     * Creates a new health component with the specified maximum health.
     * @param maxHealth The maximum health points for this component
     */
    public HealthComponent(int maxHealth) {
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
        this.isAlive = true;
        this.lastDamageTime = 0.0f;
    }
    
    /**
     * Applies damage to this health component.
     * @param damage The amount of damage to apply (must be positive)
     * @param currentTime The current game time for tracking damage timing
     */
    public void takeDamage(int damage, float currentTime) {
        if (damage < 0) {
            return; // Reject negative damage
        }
        
        this.currentHealth = Math.max(0, this.currentHealth - damage);
        this.lastDamageTime = currentTime;
        
        if (this.currentHealth <= 0) {
            this.isAlive = false;
        }
    }
    
    /**
     * Returns the current health points.
     * @return Current health value
     */
    public int getCurrentHealth() {
        return currentHealth;
    }
    
    /**
     * Returns the maximum health points.
     * @return Maximum health value
     */
    public int getMaxHealth() {
        return maxHealth;
    }
    
    /**
     * Returns whether this entity is still alive.
     * @return true if health > 0, false otherwise
     */
    public boolean isAlive() {
        return isAlive;
    }
    
    /**
     * Returns the health as a percentage (0.0 to 1.0).
     * @return Health percentage as a float between 0.0 and 1.0
     */
    public float getHealthPercentage() {
        if (maxHealth <= 0) {
            return 0.0f;
        }
        return (float) currentHealth / maxHealth;
    }
    
    /**
     * Returns the time when damage was last applied.
     * @return Last damage time in seconds
     */
    public float getLastDamageTime() {
        return lastDamageTime;
    }
    
    /**
     * Resets the health component to full health.
     */
    public void reset() {
        this.currentHealth = this.maxHealth;
        this.isAlive = true;
        this.lastDamageTime = 0.0f;
    }
}