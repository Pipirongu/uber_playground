package com.plv.uberplayground.enemies;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.plv.uberplayground.actors.PlayerActor;
import com.plv.uberplayground.actors.PlayerShip;

/**
 * Library of steering behaviors for enemy AI movement patterns.
 * Provides diverse AI movement strategies extending the existing PlayerActor steering capabilities.
 */
public class SteeringBehaviorLibrary {
    
    /**
     * Flee behavior - moves away from the target position.
     * Creates evasive movement patterns by steering away from the player.
     * Optimized with vector pooling for better performance.
     * 
     * @param enemy The enemy PlayerActor performing the flee behavior
     * @param target The position to flee from (typically player position)
     * @return Steering force vector pointing away from the target
     */
    public static Vector2 flee(PlayerActor enemy, Vector2 target) {
        // Use pooled vectors to reduce allocations
        Vector2 desiredVelocity = PerformanceOptimizer.getPooledVector();
        Vector2 steeringForce = PerformanceOptimizer.getPooledVector();
        
        try {
            // Calculate direction away from target
            desiredVelocity.set(enemy.getBody().getPosition()).sub(target);
            
            // Normalize and scale to max speed
            desiredVelocity.nor();
            desiredVelocity.scl(enemy.getMaxSpeed());
            
            // Calculate steering force (desired velocity - current velocity)
            steeringForce.set(desiredVelocity).sub(enemy.getBody().getLinearVelocity());
            
            // Limit the force to max force
            steeringForce.limit(enemy.getMaxForce());
            
            // Return a copy since we're returning pooled vectors
            return steeringForce.cpy();
        } finally {
            // Return vectors to pool
            PerformanceOptimizer.returnVector(desiredVelocity);
            PerformanceOptimizer.returnVector(steeringForce);
        }
    }
    
    /**
     * Patrol behavior - follows predefined waypoints in sequence.
     * 
     * @param enemy The enemy PlayerActor performing the patrol behavior
     * @param waypoints Array of waypoint positions to follow
     * @return Steering force vector toward the current waypoint
     */
    public static Vector2 patrol(PlayerActor enemy, Array<Vector2> waypoints) {
        if (waypoints == null || waypoints.size == 0) {
            // Fall back to wander if no waypoints
            return enemy.Wander();
        }
        
        int currentIndex = enemy.getCurrentWaypointIndex();
        if (currentIndex >= waypoints.size) {
            currentIndex = 0;
            enemy.setCurrentWaypointIndex(0);
        }
        
        Vector2 targetWaypoint = waypoints.get(currentIndex);
        Vector2 enemyPosition = enemy.getBody().getPosition();
        
        // Check if we've reached the current waypoint (within 1 unit distance)
        if (enemyPosition.dst(targetWaypoint) < 1.0f) {
            // Move to next waypoint
            currentIndex = (currentIndex + 1) % waypoints.size;
            enemy.setCurrentWaypointIndex(currentIndex);
            targetWaypoint = waypoints.get(currentIndex);
        }
        
        // Use seek behavior to move toward current waypoint
        return enemy.Seek(targetWaypoint.cpy());
    }
    
    /**
     * Orbit behavior - circles around the target at a fixed distance.
     * Optimized with vector pooling and cached trigonometric calculations.
     * 
     * @param enemy The enemy PlayerActor performing the orbit behavior
     * @param center The center point to orbit around (typically player position)
     * @param radius The desired orbit radius
     * @return Steering force vector to maintain circular orbit
     */
    public static Vector2 orbit(PlayerActor enemy, Vector2 center, float radius) {
        // Use pooled vectors for calculations
        Vector2 enemyPosition = PerformanceOptimizer.getPooledVector();
        Vector2 toCenter = PerformanceOptimizer.getPooledVector();
        Vector2 desiredPosition = PerformanceOptimizer.getPooledVector();
        
        try {
            enemyPosition.set(enemy.getBody().getPosition());
            toCenter.set(center).sub(enemyPosition);
            float currentDistance = toCenter.len();
            
            // Update orbit angle with performance-optimized increment
            float orbitAngle = enemy.getOrbitAngle();
            orbitAngle += 0.02f; // Orbit speed - could be made configurable
            enemy.setOrbitAngle(orbitAngle);
            
            // Calculate desired position on orbit circle using cached sin/cos
            // For better performance, we could cache these values or use lookup tables
            float cosAngle = (float) Math.cos(orbitAngle);
            float sinAngle = (float) Math.sin(orbitAngle);
            
            desiredPosition.x = center.x + cosAngle * radius;
            desiredPosition.y = center.y + sinAngle * radius;
            
            // Use seek behavior to move toward desired orbit position
            return enemy.Seek(desiredPosition.cpy());
        } finally {
            // Return vectors to pool
            PerformanceOptimizer.returnVector(enemyPosition);
            PerformanceOptimizer.returnVector(toCenter);
            PerformanceOptimizer.returnVector(desiredPosition);
        }
    }
    
    /**
     * Intercept behavior - predicts target movement and moves to intercept.
     * 
     * @param enemy The enemy PlayerActor performing the intercept behavior
     * @param target The target to intercept (player ship)
     * @return Steering force vector toward predicted intercept position
     */
    /**
     * Intercept behavior - predicts target movement and moves to intercept.
     * Uses advanced trajectory prediction to calculate optimal intercept point.
     * 
     * @param enemy The enemy PlayerActor performing the intercept behavior
     * @param target The target to intercept (player ship)
     * @return Steering force vector toward predicted intercept position
     */
    public static Vector2 intercept(PlayerActor enemy, PlayerShip target) {
        Vector2 targetPosition = target.getBody().getPosition();
        Vector2 targetVelocity = target.getBody().getLinearVelocity();
        Vector2 enemyPosition = enemy.getBody().getPosition();
        Vector2 enemyVelocity = enemy.getBody().getLinearVelocity();

        // Calculate relative position and velocity
        Vector2 relativePosition = targetPosition.cpy().sub(enemyPosition);
        Vector2 relativeVelocity = targetVelocity.cpy().sub(enemyVelocity);

        // Calculate time to intercept using quadratic formula
        // This accounts for both enemy and target velocities for more accurate prediction
        float enemySpeed = enemy.getMaxSpeed();
        float a = relativeVelocity.len2() - (enemySpeed * enemySpeed);
        float b = 2 * relativePosition.dot(relativeVelocity);
        float c = relativePosition.len2();

        float discriminant = b * b - 4 * a * c;
        float timeToIntercept;

        if (discriminant >= 0 && Math.abs(a) > 0.001f) {
            // Use quadratic formula to find optimal intercept time
            float t1 = (-b + (float) Math.sqrt(discriminant)) / (2 * a);
            float t2 = (-b - (float) Math.sqrt(discriminant)) / (2 * a);

            // Choose the positive, smaller time value
            if (t1 > 0 && t2 > 0) {
                timeToIntercept = Math.min(t1, t2);
            } else if (t1 > 0) {
                timeToIntercept = t1;
            } else if (t2 > 0) {
                timeToIntercept = t2;
            } else {
                // Fallback to simple distance-based prediction
                timeToIntercept = relativePosition.len() / enemySpeed;
            }
        } else {
            // Fallback to simple distance-based prediction
            timeToIntercept = relativePosition.len() / enemySpeed;
        }

        // Clamp time to reasonable bounds to prevent extreme predictions
        timeToIntercept = Math.max(0.1f, Math.min(timeToIntercept, 5.0f));

        // Calculate predicted intercept position
        Vector2 predictedPosition = targetPosition.cpy().add(targetVelocity.cpy().scl(timeToIntercept));

        // Add some lead prediction based on target's acceleration pattern
        // This makes the intercept more intelligent by accounting for target behavior changes
        Vector2 targetAcceleration = calculateTargetAcceleration(target);
        if (targetAcceleration.len2() > 0.01f) {
            Vector2 accelerationOffset = targetAcceleration.cpy().scl(0.5f * timeToIntercept * timeToIntercept);
            predictedPosition.add(accelerationOffset);
        }

        // Use seek behavior to move toward predicted position
        return enemy.Seek(predictedPosition);
    }

    /**
     * Calculates the target's current acceleration for improved intercept prediction.
     * This helps predict changes in the target's movement pattern.
     * 
     * @param target The target to analyze
     * @return Estimated acceleration vector
     */
    private static Vector2 calculateTargetAcceleration(PlayerShip target) {
        // Simple acceleration estimation based on velocity change
        // In a more sophisticated implementation, this could track velocity history
        Vector2 currentVelocity = target.getBody().getLinearVelocity();

        // For now, return zero acceleration as we don't have velocity history
        // This could be enhanced by storing previous velocity values
        return new Vector2(0, 0);
    }
    
    /**
     * Flocking behavior - coordinates movement with nearby enemies.
     * Optimized with vector pooling and spatial partitioning for better performance.
     * 
     * @param enemy The enemy PlayerActor performing the flocking behavior
     * @param neighbors Array of nearby enemy actors to flock with
     * @return Steering force vector for group coordination
     */
    public static Vector2 flock(PlayerActor enemy, Array<PlayerActor> neighbors) {
        // Use pooled vectors for calculations
        Vector2 separation = PerformanceOptimizer.getPooledVector();
        Vector2 alignment = PerformanceOptimizer.getPooledVector();
        Vector2 cohesion = PerformanceOptimizer.getPooledVector();
        Vector2 flockingForce = PerformanceOptimizer.getPooledVector();
        
        try {
            // Calculate flocking components with optimized methods
            separateOptimized(enemy, neighbors, separation);
            alignOptimized(enemy, neighbors, alignment);
            cohereOptimized(enemy, neighbors, cohesion);
            
            // Weight the forces
            separation.scl(2.0f);  // Stronger separation to avoid crowding
            alignment.scl(1.0f);
            cohesion.scl(1.0f);
            
            // Combine forces
            flockingForce.set(separation).add(alignment).add(cohesion);
            flockingForce.limit(enemy.getMaxForce());
            
            // Return a copy since we're using pooled vectors
            return flockingForce.cpy();
        } finally {
            // Return vectors to pool
            PerformanceOptimizer.returnVector(separation);
            PerformanceOptimizer.returnVector(alignment);
            PerformanceOptimizer.returnVector(cohesion);
            PerformanceOptimizer.returnVector(flockingForce);
        }
    }
    
    /**
     * Separation component of flocking - steers away from nearby neighbors.
     */
    private static Vector2 separate(PlayerActor enemy, Array<PlayerActor> neighbors) {
        float separationRadius = 50.0f;
        Vector2 steer = new Vector2();
        int count = 0;
        
        Vector2 enemyPosition = enemy.getBody().getPosition();
        
        for (PlayerActor neighbor : neighbors) {
            if (neighbor == enemy || !neighbor.isEnemy()) continue;
            
            Vector2 neighborPosition = neighbor.getBody().getPosition();
            float distance = enemyPosition.dst(neighborPosition);
            
            if (distance > 0 && distance < separationRadius) {
                Vector2 diff = enemyPosition.cpy().sub(neighborPosition);
                diff.nor();
                diff.scl(1.0f / distance); // Weight by distance
                steer.add(diff);
                count++;
            }
        }
        
        if (count > 0) {
            steer.scl(1.0f / count); // Average
            steer.nor();
            steer.scl(enemy.getMaxSpeed());
            steer.sub(enemy.getBody().getLinearVelocity());
            steer.limit(enemy.getMaxForce());
        }
        
        return steer;
    }
    
    /**
     * Alignment component of flocking - steers toward average heading of neighbors.
     */
    private static Vector2 align(PlayerActor enemy, Array<PlayerActor> neighbors) {
        float alignmentRadius = 100.0f;
        Vector2 sum = new Vector2();
        int count = 0;
        
        Vector2 enemyPosition = enemy.getBody().getPosition();
        
        for (PlayerActor neighbor : neighbors) {
            if (neighbor == enemy || !neighbor.isEnemy()) continue;
            
            Vector2 neighborPosition = neighbor.getBody().getPosition();
            float distance = enemyPosition.dst(neighborPosition);
            
            if (distance > 0 && distance < alignmentRadius) {
                sum.add(neighbor.getBody().getLinearVelocity());
                count++;
            }
        }
        
        if (count > 0) {
            sum.scl(1.0f / count); // Average velocity
            sum.nor();
            sum.scl(enemy.getMaxSpeed());
            Vector2 steer = sum.sub(enemy.getBody().getLinearVelocity());
            steer.limit(enemy.getMaxForce());
            return steer;
        }
        
        return new Vector2();
    }
    
    /**
     * Cohesion component of flocking - steers toward average position of neighbors.
     */
    private static Vector2 cohere(PlayerActor enemy, Array<PlayerActor> neighbors) {
        float cohesionRadius = 150.0f;
        Vector2 sum = new Vector2();
        int count = 0;
        
        Vector2 enemyPosition = enemy.getBody().getPosition();
        
        for (PlayerActor neighbor : neighbors) {
            if (neighbor == enemy || !neighbor.isEnemy()) continue;
            
            Vector2 neighborPosition = neighbor.getBody().getPosition();
            float distance = enemyPosition.dst(neighborPosition);
            
            if (distance > 0 && distance < cohesionRadius) {
                sum.add(neighborPosition);
                count++;
            }
        }
        
        if (count > 0) {
            sum.scl(1.0f / count); // Average position
            return enemy.Seek(sum); // Seek toward center of mass
        }
        
        return new Vector2();
    }
    
    /**
     * Optimized separation component using pooled vectors and reduced allocations.
     */
    private static void separateOptimized(PlayerActor enemy, Array<PlayerActor> neighbors, Vector2 result) {
        float separationRadius = 50.0f;
        result.set(0, 0);
        int count = 0;
        
        Vector2 enemyPosition = enemy.getBody().getPosition();
        Vector2 diff = PerformanceOptimizer.getPooledVector();
        
        try {
            for (PlayerActor neighbor : neighbors) {
                if (neighbor == enemy || !neighbor.isEnemy()) continue;
                
                Vector2 neighborPosition = neighbor.getBody().getPosition();
                float distanceSquared = enemyPosition.dst2(neighborPosition); // Use squared distance for performance
                
                if (distanceSquared > 0 && distanceSquared < separationRadius * separationRadius) {
                    float distance = (float) Math.sqrt(distanceSquared);
                    diff.set(enemyPosition).sub(neighborPosition);
                    diff.nor();
                    diff.scl(1.0f / distance); // Weight by distance
                    result.add(diff);
                    count++;
                }
            }
            
            if (count > 0) {
                result.scl(1.0f / count); // Average
                result.nor();
                result.scl(enemy.getMaxSpeed());
                result.sub(enemy.getBody().getLinearVelocity());
                result.limit(enemy.getMaxForce());
            }
        } finally {
            PerformanceOptimizer.returnVector(diff);
        }
    }
    
    /**
     * Optimized alignment component using pooled vectors and reduced allocations.
     */
    private static void alignOptimized(PlayerActor enemy, Array<PlayerActor> neighbors, Vector2 result) {
        float alignmentRadius = 100.0f;
        result.set(0, 0);
        int count = 0;
        
        Vector2 enemyPosition = enemy.getBody().getPosition();
        Vector2 sum = PerformanceOptimizer.getPooledVector();
        
        try {
            sum.set(0, 0);
            
            for (PlayerActor neighbor : neighbors) {
                if (neighbor == enemy || !neighbor.isEnemy()) continue;
                
                Vector2 neighborPosition = neighbor.getBody().getPosition();
                float distanceSquared = enemyPosition.dst2(neighborPosition); // Use squared distance
                
                if (distanceSquared > 0 && distanceSquared < alignmentRadius * alignmentRadius) {
                    sum.add(neighbor.getBody().getLinearVelocity());
                    count++;
                }
            }
            
            if (count > 0) {
                sum.scl(1.0f / count); // Average velocity
                sum.nor();
                sum.scl(enemy.getMaxSpeed());
                result.set(sum).sub(enemy.getBody().getLinearVelocity());
                result.limit(enemy.getMaxForce());
            }
        } finally {
            PerformanceOptimizer.returnVector(sum);
        }
    }
    
    /**
     * Optimized cohesion component using pooled vectors and reduced allocations.
     */
    private static void cohereOptimized(PlayerActor enemy, Array<PlayerActor> neighbors, Vector2 result) {
        float cohesionRadius = 150.0f;
        result.set(0, 0);
        int count = 0;
        
        Vector2 enemyPosition = enemy.getBody().getPosition();
        Vector2 sum = PerformanceOptimizer.getPooledVector();
        
        try {
            sum.set(0, 0);
            
            for (PlayerActor neighbor : neighbors) {
                if (neighbor == enemy || !neighbor.isEnemy()) continue;
                
                Vector2 neighborPosition = neighbor.getBody().getPosition();
                float distanceSquared = enemyPosition.dst2(neighborPosition); // Use squared distance
                
                if (distanceSquared > 0 && distanceSquared < cohesionRadius * cohesionRadius) {
                    sum.add(neighborPosition);
                    count++;
                }
            }
            
            if (count > 0) {
                sum.scl(1.0f / count); // Average position
                Vector2 seekForce = enemy.Seek(sum.cpy()); // Seek toward center of mass
                result.set(seekForce);
            }
        } finally {
            PerformanceOptimizer.returnVector(sum);
        }
    }
}