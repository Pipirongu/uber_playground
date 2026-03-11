# Patrol Behavior Usage Guide

## Overview

The patrol behavior allows enemies to follow predefined waypoints in sequence, creating predictable movement patterns. This behavior integrates with the existing PlayerActor waypoint management system and provides various pre-built patrol patterns.

## Basic Usage

### Setting Up Patrol Waypoints

```java
// Create an enemy PlayerActor
PlayerActor enemy = new PlayerActor(world, x, y);
enemy.setIsEnemy(true);

// Create waypoints for patrol
Array<Vector2> waypoints = new Array<>();
waypoints.add(new Vector2(10, 10));
waypoints.add(new Vector2(50, 10));
waypoints.add(new Vector2(50, 50));
waypoints.add(new Vector2(10, 50));

// Set the patrol waypoints
enemy.setPatrolWaypoints(waypoints);

// Set behavior to patrol
enemy.getBehaviorState().forceBehaviorSwitch(SteeringBehaviorType.PATROL);
```

### Using Pre-built Patrol Patterns

The `PatrolPatterns` utility class provides common patrol patterns:

#### Rectangular Patrol
```java
Vector2 center = new Vector2(25, 25);
Array<Vector2> waypoints = PatrolPatterns.createRectangularPatrol(center, 20, 15);
enemy.setPatrolWaypoints(waypoints);
```

#### Circular Patrol
```java
Vector2 center = new Vector2(30, 30);
Array<Vector2> waypoints = PatrolPatterns.createCircularPatrol(center, 15, 6);
enemy.setPatrolWaypoints(waypoints);
```

#### Linear Back-and-Forth
```java
Vector2 start = new Vector2(10, 20);
Vector2 end = new Vector2(60, 20);
Array<Vector2> waypoints = PatrolPatterns.createLinearPatrol(start, end);
enemy.setPatrolWaypoints(waypoints);
```

#### L-Shaped Patrol
```java
Vector2 corner = new Vector2(15, 15);
Array<Vector2> waypoints = PatrolPatterns.createLShapedPatrol(corner, 25, 20, true);
enemy.setPatrolWaypoints(waypoints);
```

## How Patrol Behavior Works

### Waypoint Following Logic

1. **Current Waypoint**: The enemy moves toward the current waypoint using seek behavior
2. **Distance Threshold**: When the enemy gets within 1.0 unit of the waypoint, it's considered "reached"
3. **Waypoint Advancement**: Upon reaching a waypoint, the enemy advances to the next waypoint in sequence
4. **Cycling**: After reaching the last waypoint, the enemy cycles back to the first waypoint

### Waypoint Management

- **getCurrentWaypointIndex()**: Returns the index of the current target waypoint
- **setCurrentWaypointIndex(int)**: Sets the current waypoint index (useful for starting at specific points)
- **getPatrolWaypoints()**: Returns the array of waypoints
- **setPatrolWaypoints(Array<Vector2>)**: Sets the waypoints array and resets index to 0

### Fallback Behavior

If no waypoints are set or the waypoints array is empty, the patrol behavior automatically falls back to wander behavior to ensure the enemy continues moving.

## Integration with Behavior System

### Automatic Behavior Switching

When using the behavior switching system, patrol behavior will be randomly selected along with other behaviors:

```java
// The enemy will automatically switch between behaviors including patrol
// No additional setup needed - just ensure waypoints are set
enemy.setPatrolWaypoints(waypoints);
```

### Manual Behavior Control

You can force an enemy to use patrol behavior:

```java
enemy.getBehaviorState().forceBehaviorSwitch(SteeringBehaviorType.PATROL);
```

## Best Practices

### Waypoint Placement

1. **Spacing**: Place waypoints with reasonable spacing (not too close together)
2. **Accessibility**: Ensure all waypoints are reachable and not inside walls
3. **Flow**: Design waypoint sequences that create smooth, natural movement patterns

### Performance Considerations

1. **Waypoint Count**: Keep waypoint counts reasonable (typically 3-8 waypoints)
2. **Distance Calculations**: The system efficiently calculates distances each frame
3. **Memory Usage**: Waypoints are stored per enemy, so consider memory usage with many enemies

### Common Patterns

1. **Perimeter Guards**: Use rectangular patrols around important areas
2. **Roaming Enemies**: Use circular or figure-8 patterns for area coverage
3. **Corridor Guards**: Use linear back-and-forth patterns in narrow spaces
4. **Complex Routes**: Combine multiple patterns or create custom waypoint sequences

## Example: Complete Enemy Setup

```java
public PlayerActor createPatrollingEnemy(World world, float x, float y, PatrolType type) {
    PlayerActor enemy = new PlayerActor(world, x, y);
    enemy.setIsEnemy(true);
    
    Array<Vector2> waypoints;
    Vector2 center = new Vector2(x, y);
    
    switch (type) {
        case RECTANGULAR:
            waypoints = PatrolPatterns.createRectangularPatrol(center, 30, 20);
            break;
        case CIRCULAR:
            waypoints = PatrolPatterns.createCircularPatrol(center, 20, 8);
            break;
        case LINEAR:
            Vector2 end = new Vector2(x + 40, y);
            waypoints = PatrolPatterns.createLinearPatrol(center, end);
            break;
        default:
            waypoints = PatrolPatterns.createRectangularPatrol(center, 25, 25);
    }
    
    enemy.setPatrolWaypoints(waypoints);
    enemy.getBehaviorState().forceBehaviorSwitch(SteeringBehaviorType.PATROL);
    
    return enemy;
}
```

This guide provides everything needed to effectively use the patrol behavior system in your game.