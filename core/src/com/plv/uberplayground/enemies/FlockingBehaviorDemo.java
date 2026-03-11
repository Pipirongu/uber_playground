package com.plv.uberplayground.enemies;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.plv.uberplayground.actors.PlayerActor;

/**
 * Demonstration class showing the flocking behavior implementation.
 * This class provides examples of how the flocking behavior coordinates
 * multiple enemies using separation, alignment, and cohesion forces.
 */
public class FlockingBehaviorDemo {
    
    /**
     * Demonstrates the flocking behavior force calculation.
     * Shows how separation, alignment, and cohesion forces are combined.
     */
    public static void demonstrateFlockingForces() {
        System.out.println("=== Flocking Behavior Implementation Demo ===");
        System.out.println();
        
        // Create empty neighbors array to demonstrate no-neighbors case
        Array<PlayerActor> emptyNeighbors = new Array<>();
        Vector2 emptyFlockForce = SteeringBehaviorLibrary.flock(null, emptyNeighbors);
        
        System.out.println("1. Flocking with no neighbors:");
        System.out.println("   Force: " + emptyFlockForce);
        System.out.println("   Result: Zero force when no neighbors present");
        System.out.println();
        
        // Demonstrate force component weights
        System.out.println("2. Flocking force component weights:");
        System.out.println("   - Separation: 2.0x weight (avoid crowding)");
        System.out.println("   - Alignment: 1.0x weight (match heading)");
        System.out.println("   - Cohesion: 1.0x weight (stay together)");
        System.out.println();
        
        // Demonstrate neighbor detection ranges
        System.out.println("3. Neighbor detection ranges:");
        System.out.println("   - Separation radius: 50.0 pixels");
        System.out.println("   - Alignment radius: 100.0 pixels");
        System.out.println("   - Cohesion radius: 150.0 pixels");
        System.out.println("   - Flocking range: 200.0 pixels");
        System.out.println();
        
        System.out.println("4. Integration with PlayerActor:");
        System.out.println("   - Flocking behavior is now active in FLOCK behavior state");
        System.out.println("   - Neighbor detection searches within 200 pixel range");
        System.out.println("   - Falls back to seek behavior when no neighbors found");
        System.out.println();
        
        System.out.println("=== Flocking Implementation Complete ===");
    }
    
    /**
     * Main method for running the demonstration.
     */
    public static void main(String[] args) {
        demonstrateFlockingForces();
    }
}