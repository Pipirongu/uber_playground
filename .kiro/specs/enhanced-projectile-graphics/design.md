# Design Document: Enhanced Projectile Graphics

## Overview

This design document outlines the technical implementation for enhancing the projectile system in the LibGDX game. The enhancement focuses on three key areas: visual consistency by switching from "agent" to "spawn_unit" graphics, large scaling using 20.0f scaling factor for high visibility, and performance improvement by increasing projectile speed from 5000.0f to 7500.0f pixels per second.

The current projectile system uses the AnimatedPhysicsActor base class with "agent" graphics and implements a dual-velocity approach where Box2D physics runs at low velocity (100.0f) while visual updates use direct position manipulation at high speed (5000.0f). This design maintains this proven architecture while updating graphics assets and parameters.

## Architecture

### Current System Architecture

The projectile system follows a layered architecture:

1. **ProjectilePool**: Object pool manager that creates and reuses Projectile instances
2. **Projectile**: Core projectile class extending AnimatedPhysicsActor with dual-velocity movement
3. **AnimatedPhysicsActor**: Base class providing graphics loading, scaling, and Box2D physics integration
4. **Graphics Assets**: PNG textures located in android/assets/actors/

### Enhanced Architecture

The enhanced system maintains the same architectural layers but updates key parameters:

```
ProjectilePool (Updated)
├── Creates Projectile instances with "spawn_unit" graphics
├── Maintains same pool size (20 instances)
└── Preserves existing lifecycle management

Projectile (Enhanced)
├── Uses "spawn_unit" graphics instead of "agent"
├── Applies 20.0f scaling factor (large scaling for high visibility)
├── Increases speed from 5000.0f to 7500.0f pixels/second
└── Maintains dual-velocity physics approach

AnimatedPhysicsActor (Unchanged)
├── Handles graphics loading and scaling
├── Creates Box2D physics bodies with scaled dimensions
└── Provides consistent rendering pipeline
```

## Components and Interfaces

### ProjectilePool Interface Changes

The ProjectilePool class requires minimal changes to support the new graphics:

**Constructor Update:**
```java
// Current implementation
Projectile projectile = new Projectile("agent", world, 1, 1);

// Enhanced implementation  
Projectile projectile = new Projectile("spawn_unit", world, 1, 1);
```

**Method Signatures (Unchanged):**
- `Projectile obtain(Vector2 position, Vector2 direction, Vector2 shipVelocity)`
- `void free(Projectile projectile)`
- `void updateAll(float delta)`
- `void disposeAll()`
- `void clearAllActive()`
- `void recreatePool()`

### Projectile Class Enhancements

**Constants Update:**
```java
// Current
public static final float PROJECTILE_SPEED = 5000.0f;

// Enhanced
public static final float PROJECTILE_SPEED = 7500.0f;
```

**Constructor Enhancement:**
```java
// Current constructor call to super
super(animationName, world, 0, 0, frameCols, frameRows);

// Enhanced constructor call with large scaling
super(animationName, world, 0, 0, frameCols, frameRows, 20.0f);
```

**Graphics Configuration:**
- Animation name: "spawn_unit" (instead of "agent")
- Frame configuration: 1 column, 1 row (unchanged)
- Scaling factor: 20.0f (explicit, large scaling for high visibility)

### AnimatedPhysicsActor Integration

The base class already supports the required functionality:

**Graphics Loading:**
- Loads textures from `android/assets/actors/{animationName}.png`
- Supports configurable frame splitting (frameCols, frameRows)
- Applies scaling to both visual rendering and physics body dimensions

**Physics Body Scaling:**
```java
// Physics body dimensions with scaling applied
shape.setAsBox((this.textureWidth/2.f * scale)/Configuration.PPM, 
               (this.textureHeight/2.f * scale)/Configuration.PPM);
```

**Rendering with Scaling:**
```java
// Scaled rendering in draw() method
batch.draw(animation, x, y, originX, originY, 
           (this.textureWidth * scale)/Configuration.PPM,
           (this.textureHeight * scale)/Configuration.PPM, 
           scaleX, scaleY, rotation);
```

## Data Models

### Projectile State Model

The Projectile class maintains the following state model (unchanged):

```java
public class Projectile {
    // Constants (Enhanced)
    public static final float PROJECTILE_SPEED = 7500.0f;  // Increased from 5000.0f
    public static final float PROJECTILE_LIFETIME = 15.0f; // Unchanged
    
    // State Fields (Unchanged)
    private float timeAlive;
    private boolean isActive;
    private Vector2 direction;
    private Vector2 currentPosition;
    private boolean markedForDeactivation;
    
    // Physics Integration (Unchanged)
    private Body body; // Box2D physics body
}
```

### Graphics Asset Model

**Current Asset Usage:**
- Projectile: `android/assets/actors/agent.png`
- ControlPointActor: `android/assets/actors/spawn_unit.png`

**Enhanced Asset Usage:**
- Projectile: `android/assets/actors/spawn_unit.png` (changed)
- ControlPointActor: `android/assets/actors/spawn_unit.png` (unchanged)

**Asset Specifications:**
- Format: PNG texture files
- Frame configuration: Single frame (1 column, 1 row)
- Scaling: Applied at runtime via AnimatedPhysicsActor

### Collision Model

The collision detection model remains unchanged but benefits from consistent scaling:

**Collision Categories (Unchanged):**
```java
filter.categoryBits = Configuration.EntityCategory.PROJECTILE.getValue();
filter.maskBits = (short)(Configuration.EntityCategory.BOUNDARY.getValue() |
                         Configuration.EntityCategory.ENEMY_SHIP.getValue());
```

**Physics Body Properties (Enhanced):**
- Dimensions: Based on "spawn_unit" texture dimensions with 20.0f scaling
- Collision boundaries: Automatically updated to match new graphics dimensions
- Physics behavior: Maintains bullet physics and collision filtering

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Graphics Consistency

*For any* projectile created by the ProjectilePool, it should use "spawn_unit" graphics with 1 column and 1 row frame configuration, matching the ControlPointActor graphics specification.

**Validates: Requirements 1.1, 1.2, 1.3**

### Property 2: Scaling Consistency  

*For any* projectile with a given scaling factor, both the rendered texture dimensions and physics body collision boundaries should be scaled by the same factor, maintaining proportional visual and collision representation.

**Validates: Requirements 2.1, 2.3, 2.5**

### Property 3: Configurable Scaling

*For any* valid scaling factor provided to the Projectile constructor, the projectile should be created with that scaling applied to both visual rendering and physics body dimensions.

**Validates: Requirements 2.2**

### Property 4: Collision System Consistency

*For any* projectile interaction with boundaries or enemies, the collision detection, filtering, and response behavior should remain identical to the original implementation regardless of graphics or scaling changes.

**Validates: Requirements 1.4, 4.3, 4.4**

### Property 5: Movement at Enhanced Speed

*For any* projectile fired with the enhanced speed (7500.0f pixels/second), the visual position updates should maintain smooth movement without visual artifacts or inconsistencies.

**Validates: Requirements 3.2**

### Property 6: Collision Accuracy at Enhanced Speed

*For any* projectile moving at the enhanced speed, collision detection with boundaries and enemies should maintain the same accuracy and timing as the original speed implementation.

**Validates: Requirements 3.3**

## Error Handling

### Graphics Loading Errors

**Missing Texture Files:**
- If "spawn_unit.png" is missing, the system should fail gracefully during ProjectilePool initialization
- Error logging should clearly indicate the missing asset path
- The application should not crash but should provide meaningful error messages

**Invalid Frame Configuration:**
- If frame configuration parameters are invalid (e.g., zero or negative values), default to 1x1 configuration
- Log warnings for invalid frame configurations but continue execution

### Physics Integration Errors

**Box2D World Disposal:**
- Ensure proper cleanup of projectile physics bodies when the world is disposed
- Prevent memory leaks by disposing projectile bodies before world destruction
- Handle cases where projectiles are active during world disposal

**Collision Detection Failures:**
- If collision filtering fails to initialize, use default collision settings
- Log errors for collision configuration failures but maintain basic collision detection

### Performance Considerations

**Pool Exhaustion:**
- When all 20 projectiles in the pool are active, reuse the oldest projectile as currently implemented
- Monitor pool usage and log warnings if pool exhaustion occurs frequently
- Consider dynamic pool expansion if performance analysis indicates benefit

**High-Speed Physics:**
- The dual-velocity approach (low Box2D velocity + high visual speed) prevents physics instability
- Maintain continuous collision detection (bullet physics) to handle high-speed collisions
- Monitor for tunneling effects at increased speeds and adjust physics timestep if necessary

## Testing Strategy

### Dual Testing Approach

The testing strategy employs both unit testing and property-based testing to ensure comprehensive coverage:

**Unit Tests:**
- Verify specific configuration values (graphics asset names, speed constants, scaling factors)
- Test integration points between ProjectilePool and Projectile classes
- Validate error conditions and edge cases (missing assets, invalid parameters)
- Confirm backward compatibility of public API methods

**Property-Based Tests:**
- Verify universal properties across all possible inputs and configurations
- Test scaling behavior with randomized scaling factors
- Validate collision consistency across different projectile states and speeds
- Ensure movement calculations remain accurate across various time deltas and speeds

### Property-Based Testing Configuration

**Testing Framework:** Use QuickCheck for Java (or similar property-based testing library)

**Test Configuration:**
- Minimum 100 iterations per property test to ensure statistical confidence
- Each property test references its corresponding design document property
- Tag format: **Feature: enhanced-projectile-graphics, Property {number}: {property_text}**

**Property Test Implementation Requirements:**

1. **Graphics Consistency Test:**
   - Generate random ProjectilePool instances
   - Verify all created projectiles use "spawn_unit" animation name
   - **Feature: enhanced-projectile-graphics, Property 1: Graphics consistency**

2. **Scaling Consistency Test:**
   - Generate random scaling factors (0.1f to 5.0f)
   - Create projectiles with various scaling factors
   - Verify visual and physics dimensions scale proportionally
   - **Feature: enhanced-projectile-graphics, Property 2: Scaling consistency**

3. **Configurable Scaling Test:**
   - Generate random valid scaling factors
   - Create projectiles with each scaling factor
   - Verify scaling is applied correctly to both rendering and physics
   - **Feature: enhanced-projectile-graphics, Property 3: Configurable scaling**

4. **Collision System Consistency Test:**
   - Generate random projectile states and collision scenarios
   - Compare collision behavior before and after enhancements
   - Verify collision categories, masks, and responses remain identical
   - **Feature: enhanced-projectile-graphics, Property 4: Collision system consistency**

5. **Movement at Enhanced Speed Test:**
   - Generate random time deltas and movement scenarios
   - Verify position calculations remain accurate at 7500.0f pixels/second
   - Check for smooth movement without visual artifacts
   - **Feature: enhanced-projectile-graphics, Property 5: Movement at enhanced speed**

6. **Collision Accuracy at Enhanced Speed Test:**
   - Generate random collision scenarios at enhanced speed
   - Verify collision detection timing and accuracy
   - Compare collision results with original speed implementation
   - **Feature: enhanced-projectile-graphics, Property 6: Collision accuracy at enhanced speed**

### Unit Testing Focus Areas

**Configuration Verification:**
- Test that PROJECTILE_SPEED constant equals 7500.0f
- Test that PROJECTILE_LIFETIME constant remains 15.0f
- Verify "spawn_unit" graphics are loaded correctly
- Confirm 1x1 frame configuration is applied

**Integration Testing:**
- Test ProjectilePool creates projectiles with correct parameters
- Verify AnimatedPhysicsActor scaling integration works correctly
- Test collision filtering setup matches original implementation
- Validate physics body creation with scaled dimensions

**Error Condition Testing:**
- Test behavior when "spawn_unit.png" is missing
- Verify graceful handling of invalid scaling factors
- Test pool exhaustion scenarios
- Validate proper cleanup during world disposal

**Backward Compatibility Testing:**
- Verify all public method signatures remain unchanged
- Test that existing collision detection behavior is preserved
- Confirm physics properties (density, damping, bullet physics) are maintained
- Validate that projectile lifecycle management works identically