package com.plv.uberplayground.actors;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.World;
import com.plv.uberplayground.config.Configuration;

public class Projectile extends AnimatedPhysicsActor {
    // Constants
    public static final float PROJECTILE_SPEED = 5000.0f; // Pixels per second for direct position updates
    public static final float PROJECTILE_LIFETIME = 15.0f;

    // Fields
    private float timeAlive;
    private boolean isActive;
    private Vector2 direction; // Direction vector (normalized)
    private Vector2 currentPosition; // Current position for direct updates
    private boolean markedForDeactivation;

    public float getTimeAlive() {
        return this.timeAlive;
    }

    public Projectile(String animationName, World world, int frameCols, int frameRows) {
        super(animationName, world, 0, 0, frameCols, frameRows);

        // Configure physics body for projectile
        this.body.setType(BodyDef.BodyType.DynamicBody);
        this.body.setBullet(true); // Enable continuous collision detection

        // Remove damping completely
        this.body.setLinearDamping(0.0f);
        this.body.setAngularDamping(0.0f);

        // Set very light density for collision detection
        this.body.getFixtureList().get(0).setDensity(0.01f);

        // Set collision filtering
        Filter filter = new Filter();
        filter.categoryBits = Configuration.EntityCategory.PROJECTILE.getValue();
        filter.maskBits = (short)(Configuration.EntityCategory.BOUNDARY.getValue() |
                         Configuration.EntityCategory.ENEMY_SHIP.getValue());
        this.body.getFixtureList().get(0).setFilterData(filter);

        // Set as non-sensor (physical collision)
        this.body.getFixtureList().get(0).setSensor(false);

        // Initialize state
        this.timeAlive = 0.0f;
        this.isActive = false;
        this.markedForDeactivation = false;
        this.direction = new Vector2();
        this.currentPosition = new Vector2();
    }

    public void fire(Vector2 position, Vector2 direction, Vector2 shipVelocity) {
        // Null check for body safety
        if (body == null) {
            return;
        }

        // Activate the projectile
        this.isActive = true;
        this.timeAlive = 0.0f;
        this.markedForDeactivation = false;

        // Store direction and starting position
        this.direction.set(direction).nor();
        this.currentPosition.set(position);

        // Set initial physics body position
        this.body.setTransform(position, 0);

        // Set a minimal Box2D velocity for collision detection (much lower than visual speed)
        Vector2 physicsVelocity = new Vector2(direction).nor().scl(100.0f); // Low velocity for physics
        this.body.setLinearVelocity(physicsVelocity);
        this.body.setAwake(true);

        // Set rotation to match direction
        float angle = (float) Math.atan2(direction.y, direction.x);
        this.body.setTransform(this.body.getPosition(), angle);

        System.out.println("PROJECTILE FIRE: Visual Speed=" + PROJECTILE_SPEED +
                          ", Physics Velocity=" + physicsVelocity.len() +
                          ", Direction=" + direction);
    }

    public void deactivate() {
        this.isActive = false;
        this.markedForDeactivation = false;

        // Null check for body safety
        if (body != null) {
            // Stop physics body
            this.body.setLinearVelocity(0, 0);
            this.body.setAngularVelocity(0);
            // Move off-screen
            this.body.setTransform(-1000, -1000, 0);
        }
    }

    public void markForDeactivation() {
        this.markedForDeactivation = true;
    }

    public boolean isMarkedForDeactivation() {
        return this.markedForDeactivation;
    }

    public boolean isActive() {
        return this.isActive;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (isActive) {
            // Check if marked for deactivation (from collision)
            if (markedForDeactivation) {
                deactivate();
                return;
            }

            timeAlive += delta;

            // Deactivate after lifetime expires
            if (timeAlive >= PROJECTILE_LIFETIME) {
                deactivate();
                return;
            }

            // DIRECT POSITION UPDATE - bypass Box2D velocity limitations
            // Move the visual position at high speed (pixels per second)
            float pixelsToMove = PROJECTILE_SPEED * delta;
            Vector2 movement = new Vector2(direction).scl(pixelsToMove);
            currentPosition.add(movement);

            // Convert pixels to Box2D units (divide by PPM)
            Vector2 box2dPosition = new Vector2(currentPosition.x / Configuration.PPM,
                                               currentPosition.y / Configuration.PPM);

            // Update the physics body position to match visual position
            if (body != null) {
                body.setTransform(box2dPosition, body.getAngle());
            }

            // Debug output every 30 frames (roughly every 0.5 seconds at 60 FPS)
            if ((int)(timeAlive * 60) % 30 == 0) {
                System.out.println("PROJECTILE UPDATE: Time=" + String.format("%.1f", timeAlive) +
                                  "s, Pixels/sec=" + PROJECTILE_SPEED +
                                  ", PixelPos=(" + String.format("%.0f", currentPosition.x) +
                                  "," + String.format("%.0f", currentPosition.y) + ")");
            }
        }
    }
}
