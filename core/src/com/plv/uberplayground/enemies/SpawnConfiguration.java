package com.plv.uberplayground.enemies;

/**
 * Configuration constants for the enemy spawning and management system.
 * Contains gameplay balancing parameters and system limits.
 */
public class SpawnConfiguration {
    
    /** Time interval between enemy spawn attempts in seconds */
    public static final float SPAWN_INTERVAL = 5.0f;
    
    /** Safe distance from player where enemies cannot spawn (in pixels) */
    public static final float SAFE_DISTANCE = 200.0f;
    
    /** Maximum number of spawn location attempts before skipping spawn cycle */
    public static final int MAX_SPAWN_ATTEMPTS = 10;
    
    /** Maximum number of active enemies allowed in the game */
    public static final int MAX_ENEMIES = 10;
    
    /** Initial health points for newly spawned enemies */
    public static final int ENEMY_HEALTH = 100;
    
    /** Damage dealt by projectiles to enemies */
    public static final int PROJECTILE_DAMAGE = 25;
    
    /** Minimum behavior switch interval in seconds */
    public static final float MIN_BEHAVIOR_SWITCH_INTERVAL = 2.0f;
    
    /** Maximum behavior switch interval in seconds */
    public static final float MAX_BEHAVIOR_SWITCH_INTERVAL = 10.0f;
    
    /** Default orbit radius for orbit behavior (in pixels) */
    public static final float DEFAULT_ORBIT_RADIUS = 150.0f;
    
    /** Health bar width in pixels */
    public static final float HEALTH_BAR_WIDTH = 40.0f;
    
    /** Health bar height in pixels */
    public static final float HEALTH_BAR_HEIGHT = 6.0f;
    
    /** Health bar offset above enemy in pixels */
    public static final float HEALTH_BAR_OFFSET = 30.0f;
    
    private SpawnConfiguration() {
        // Utility class - prevent instantiation
    }
}