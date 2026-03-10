package com.plv.uberplayground.actors;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.World;
import com.plv.uberplayground.config.Configuration;

public class PlayerShip extends AnimatedPhysicsActor {
    // Constants
    public static final float ROTATION_SPEED = 5.0f;       // Radians per second for constant rotation
    public static final float THRUST_FORCE = 15000.0f;     // Much higher for rapid acceleration
    public static final float MAX_LINEAR_VELOCITY = 300.0f; // Much higher for fast movement
    public static final float FIRE_RATE_COOLDOWN = 0.2f;   // 0.2 seconds between shots

    // State
    private boolean isAlive;
    private float lastFireTime;

    public PlayerShip(String animationName, World world, float x, float y, int frameCols, int frameRows) {
        this(animationName, world, x, y, frameCols, frameRows, 50.0f);
    }

    public PlayerShip(String animationName, World world, float x, float y, int frameCols, int frameRows, float scale) {
        super(animationName, world, x, y, frameCols, frameRows, scale);

        // Set initial state
        this.isAlive = true;
        this.lastFireTime = 0.0f;

        // Configure collision filtering
        Filter filter = new Filter();
        filter.categoryBits = Configuration.EntityCategory.PLAYER_SHIP.getValue();
        filter.maskBits = (short) (Configuration.EntityCategory.BOUNDARY.getValue() |
                                   Configuration.EntityCategory.PROJECTILE.getValue() |
                                   Configuration.EntityCategory.ENEMY_SHIP.getValue());

        this.body.getFixtureList().get(0).setFilterData(filter);

        // Set damping for responsive movement (reduce linear damping, zero angular damping)
        this.body.setLinearDamping(0.05f);
        this.body.setAngularDamping(0.0f);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (isAlive) {
            // Update fire cooldown timer
            lastFireTime += delta;

            // Clamp linear velocity to prevent excessive speed
            Vector2 velocity = body.getLinearVelocity();
            if (velocity.len() > MAX_LINEAR_VELOCITY) {
                velocity.nor().scl(MAX_LINEAR_VELOCITY);
                body.setLinearVelocity(velocity);
            }
        }
    }

    public boolean isAlive() {
        return this.isAlive;
    }

    /**
     * Apply rotational force to the ship.
     * @param direction -1 for counter-clockwise, +1 for clockwise
     */
    /**
     * Apply constant rotation to the ship.
     * @param direction -1 for counter-clockwise, +1 for clockwise
     */
    public void applyRotation(float direction) {
        if (!isAlive) return;

        // Set constant angular velocity (no momentum-based rotation)
        body.setAngularVelocity(ROTATION_SPEED * direction);
    }

    /**
     * Apply thrust force to the ship.
     * @param direction 1 for forward thrust, -1 for reverse thrust
     */
    /**
     * Stop rotation when no rotation input is active.
     */
    public void stopRotation() {
        if (!isAlive) return;

        // Immediately stop rotation (zero angular velocity)
        body.setAngularVelocity(0.0f);
    }
    /**
     * Apply thrust force to the ship with rapid acceleration.
     * @param direction 1 for forward thrust, -1 for reverse thrust
     */
    public void applyThrust(float direction) {
        if (!isAlive) return;

        com.badlogic.gdx.math.Vector2 facingDir = getFacingDirection();
        float forceX = facingDir.x * THRUST_FORCE * direction;
        float forceY = facingDir.y * THRUST_FORCE * direction;
        body.applyForceToCenter(forceX, forceY, true);
    }


    /**
     * Get the direction the ship is currently facing.
     * @return Vector2 representing the facing direction (unit vector)
     */
    public com.badlogic.gdx.math.Vector2 getFacingDirection() {
        float angle = body.getAngle();
        // Ship sprite faces up (positive Y) by default, so add 90 degrees (PI/2) to the angle
        // This makes the ship's visual front correspond to the forward direction
        return new com.badlogic.gdx.math.Vector2(
            (float) Math.cos(angle + Math.PI/2),
            (float) Math.sin(angle + Math.PI/2)
        );
    }

    /**
     * Destroy the ship (set to not alive).
     */
    public void destroy() {
        this.isAlive = false;
    }

    /**
     * Shoot a projectile from the ship's current position and facing direction.
     * Implements fire rate limiting with 0.2 second cooldown.
     * @param shootCallback Callback to handle projectile spawning
     */
    /**
     * Shoot a projectile from the ship's current position and facing direction.
     * Implements fire rate limiting with 0.2 second cooldown.
     * @param shootCallback Callback to handle projectile spawning
     */
    public void shoot(ShootCallback shootCallback) {
        if (!isAlive) {
            return;
        }

        // Check fire rate cooldown
        if (lastFireTime < FIRE_RATE_COOLDOWN) {
            return; // Still in cooldown period
        }

        // Calculate spawn position at the tip of the ship IN PIXELS
        Vector2 shipPositionBox2D = body.getPosition().cpy();
        Vector2 shipPositionPixels = new Vector2(shipPositionBox2D.x * Configuration.PPM, 
                                                shipPositionBox2D.y * Configuration.PPM);
        Vector2 direction = getFacingDirection();

        // Move spawn position forward from ship center to tip (in pixels)
        float shipTipOffsetPixels = 30.0f * Configuration.PPM; // Convert to pixels
        Vector2 spawnPositionPixels = shipPositionPixels.add(direction.cpy().scl(shipTipOffsetPixels));

        Vector2 shipVelocity = body.getLinearVelocity().cpy();

        shootCallback.spawnProjectile(spawnPositionPixels, direction, shipVelocity);

        // Reset fire cooldown timer
        lastFireTime = 0.0f;
    }

    /**
     * Interface for handling projectile spawning.
     */
    public interface ShootCallback {
        void spawnProjectile(Vector2 position, Vector2 direction, Vector2 shipVelocity);
    }
}
