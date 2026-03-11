package com.plv.uberplayground.enemies;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.plv.uberplayground.actors.PlayerActor;
import com.plv.uberplayground.actors.Projectile;

/**
 * Centralized health management system for enemies.
 * Handles enemy health registration, damage processing from projectile collisions,
 * and enemy destruction when health reaches zero.
 */
public class HealthSystem {
    
    // Map of enemies to their health components
    private final ObjectMap<PlayerActor, HealthComponent> enemyHealthMap;
    
    // List of enemies marked for removal
    private final Array<PlayerActor> enemiesToRemove;
    
    // Damage configuration
    private static final int PROJECTILE_DAMAGE = SpawnConfiguration.PROJECTILE_DAMAGE;
    private static final int DEFAULT_ENEMY_HEALTH = SpawnConfiguration.ENEMY_HEALTH;
    
    /**
     * Creates a new HealthSystem instance.
     */
    public HealthSystem() {
        this.enemyHealthMap = new ObjectMap<>();
        this.enemiesToRemove = new Array<>();
    }
    
    /**
     * Initializes health for a new enemy with the default health value.
     * @param enemy The enemy to initialize health for
     */
    public void initializeHealth(PlayerActor enemy) {
        initializeHealth(enemy, DEFAULT_ENEMY_HEALTH);
    }
    
    /**
     * Initializes health for a new enemy with the specified maximum health.
     * @param enemy The enemy to initialize health for
     * @param maxHealth The maximum health points for this enemy
     */
    public void initializeHealth(PlayerActor enemy, int maxHealth) {
        if (enemy == null) {
            return;
        }
        
        HealthComponent healthComponent = new HealthComponent(maxHealth);
        enemyHealthMap.put(enemy, healthComponent);
    }
    
    /**
     * Processes damage to an enemy from a projectile collision.
     * @param enemy The enemy that took damage
     * @param projectile The projectile that caused the damage (can be null)
     * @param currentTime The current game time for tracking damage timing
     * @return true if the enemy is still alive after damage, false if destroyed
     */
    public boolean takeDamage(PlayerActor enemy, Projectile projectile, float currentTime) {
        if (enemy == null) {
            return false;
        }
        
        HealthComponent healthComponent = enemyHealthMap.get(enemy);
        if (healthComponent == null) {
            // Enemy not registered, initialize with default health and apply damage
            initializeHealth(enemy);
            healthComponent = enemyHealthMap.get(enemy);
        }
        
        // Apply projectile damage
        healthComponent.takeDamage(PROJECTILE_DAMAGE, currentTime);
        
        // Check if enemy should be destroyed
        if (!healthComponent.isAlive()) {
            markForRemoval(enemy);
            return false;
        }
        
        return true;
    }
    
    /**
     * Processes damage to an enemy with a custom damage amount.
     * @param enemy The enemy that took damage
     * @param damage The amount of damage to apply
     * @param currentTime The current game time for tracking damage timing
     * @return true if the enemy is still alive after damage, false if destroyed
     */
    public boolean takeDamage(PlayerActor enemy, int damage, float currentTime) {
        if (enemy == null) {
            return false;
        }
        
        HealthComponent healthComponent = enemyHealthMap.get(enemy);
        if (healthComponent == null) {
            // Enemy not registered, initialize with default health and apply damage
            initializeHealth(enemy);
            healthComponent = enemyHealthMap.get(enemy);
        }
        
        // Apply custom damage
        healthComponent.takeDamage(damage, currentTime);
        
        // Check if enemy should be destroyed
        if (!healthComponent.isAlive()) {
            markForRemoval(enemy);
            return false;
        }
        
        return true;
    }
    
    /**
     * Checks if an enemy is still alive.
     * @param enemy The enemy to check
     * @return true if the enemy is alive, false otherwise
     */
    public boolean isAlive(PlayerActor enemy) {
        if (enemy == null) {
            return false;
        }
        
        HealthComponent healthComponent = enemyHealthMap.get(enemy);
        if (healthComponent == null) {
            return false; // Unregistered enemies are considered dead
        }
        
        return healthComponent.isAlive();
    }
    
    /**
     * Gets the health percentage for an enemy.
     * @param enemy The enemy to get health percentage for
     * @return Health percentage as a float between 0.0 and 1.0, or 0.0 if enemy not found
     */
    public float getHealthPercentage(PlayerActor enemy) {
        if (enemy == null) {
            return 0.0f;
        }
        
        HealthComponent healthComponent = enemyHealthMap.get(enemy);
        if (healthComponent == null) {
            return 0.0f;
        }
        
        return healthComponent.getHealthPercentage();
    }
    
    /**
     * Gets the current health points for an enemy.
     * @param enemy The enemy to get health for
     * @return Current health points, or 0 if enemy not found
     */
    public int getCurrentHealth(PlayerActor enemy) {
        if (enemy == null) {
            return 0;
        }
        
        HealthComponent healthComponent = enemyHealthMap.get(enemy);
        if (healthComponent == null) {
            return 0;
        }
        
        return healthComponent.getCurrentHealth();
    }
    
    /**
     * Gets the maximum health points for an enemy.
     * @param enemy The enemy to get max health for
     * @return Maximum health points, or 0 if enemy not found
     */
    public int getMaxHealth(PlayerActor enemy) {
        if (enemy == null) {
            return 0;
        }
        
        HealthComponent healthComponent = enemyHealthMap.get(enemy);
        if (healthComponent == null) {
            return 0;
        }
        
        return healthComponent.getMaxHealth();
    }
    
    /**
     * Marks an enemy for removal from the health system.
     * This should be called when an enemy is destroyed.
     * @param enemy The enemy to mark for removal
     */
    public void markForRemoval(PlayerActor enemy) {
        if (enemy != null && !enemiesToRemove.contains(enemy, true)) {
            enemiesToRemove.add(enemy);
        }
    }
    
    /**
     * Removes an enemy from the health system immediately.
     * @param enemy The enemy to remove
     */
    public void removeEnemy(PlayerActor enemy) {
        if (enemy == null) {
            return;
        }
        
        enemyHealthMap.remove(enemy);
        enemiesToRemove.removeValue(enemy, true);
    }
    
    /**
     * Processes all enemies marked for removal and returns them.
     * This should be called once per frame to get enemies that need to be destroyed.
     * @return Array of enemies that should be removed from the game
     */
    public Array<PlayerActor> processRemovals() {
        Array<PlayerActor> toRemove = new Array<>(enemiesToRemove);
        
        // Remove from health system
        for (PlayerActor enemy : enemiesToRemove) {
            enemyHealthMap.remove(enemy);
        }
        
        // Clear the removal list
        enemiesToRemove.clear();
        
        return toRemove;
    }
    
    /**
     * Gets the number of enemies currently registered in the health system.
     * @return Number of registered enemies
     */
    public int getRegisteredEnemyCount() {
        return enemyHealthMap.size;
    }
    
    /**
     * Clears all enemies from the health system.
     * This should be called when resetting the game state.
     */
    public void clear() {
        enemyHealthMap.clear();
        enemiesToRemove.clear();
    }
    
    /**
     * Gets all currently registered enemies.
     * @return Array of all enemies registered in the health system
     */
    public Array<PlayerActor> getRegisteredEnemies() {
        Array<PlayerActor> enemies = new Array<>();
        for (PlayerActor enemy : enemyHealthMap.keys()) {
            enemies.add(enemy);
        }
        return enemies;
    }
    
    /**
     * Resets the health of an enemy to full health.
     * @param enemy The enemy to reset health for
     */
    public void resetHealth(PlayerActor enemy) {
        if (enemy == null) {
            return;
        }
        
        HealthComponent healthComponent = enemyHealthMap.get(enemy);
        if (healthComponent != null) {
            healthComponent.reset();
            // Remove from removal list if it was marked
            enemiesToRemove.removeValue(enemy, true);
        }
    }
}