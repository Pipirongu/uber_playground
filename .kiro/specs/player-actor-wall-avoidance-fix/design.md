# PlayerActor Wall Avoidance Fix Design

## Overview

The PlayerActor's wall avoidance steering behavior fails to work effectively due to multiple scaling and algorithmic issues. The current implementation uses incorrectly positioned feelers with extreme angles, feelers that are too short to provide adequate warning distance, and improper force scaling that doesn't account for the actor's scaled mass and momentum. This design addresses these issues by implementing proper feeler positioning, slightly longer feeler length for better warning distance, and appropriate force scaling that maintains consistent avoidance behavior across all scale factors.

## Glossary

- **Bug_Condition (C)**: The condition that triggers ineffective wall avoidance - when PlayerActor approaches walls but fails to avoid them smoothly due to scaling issues
- **Property (P)**: The desired behavior when wall avoidance is needed - smooth steering away from walls with appropriate warning distance and force scaling
- **Preservation**: Existing steering behavior combinations, debug rendering, and parameter scaling that must remain unchanged by the fix
- **WallAvoidance**: The method in `core/src/com/plv/uberplayground/actors/PlayerActor.java` that calculates steering forces to avoid walls
- **CreateFeelers**: The method that positions detection rays for wall intersection testing
- **wallDetectionFeelerLength**: The property that determines how far ahead the actor looks for walls
- **Scale Factor**: The multiplier applied to the actor's size that affects mass, speed, and force calculations

## Bug Details

### Fault Condition

The bug manifests when PlayerActor approaches walls at any angle and scale factor. The WallAvoidance method fails to provide effective steering due to multiple algorithmic issues: feelers positioned at extreme angles (3.5π/2 and 0.5π/2) that don't properly sample the forward path, feelers that are too short (1f * scale) to provide adequate warning distance, and force calculation that uses a hardcoded multiplier (10f) without considering the actor's scaled mass and momentum.

**Formal Specification:**
```
FUNCTION isBugCondition(input)
  INPUT: input of type PlayerActorState
  OUTPUT: boolean
  
  RETURN input.approachingWall == true
         AND (input.feelerAnglesIncorrect == true
              OR input.feelerLengthTooShort == true
              OR input.forceScalingImproper == true)
         AND input.collisionOccurs == true
END FUNCTION
```

### Examples

- **Scaled Up Actor (scale=2.0)**: Feelers at extreme angles (315° and 45°) miss walls directly ahead, and short feelers (2f units) don't provide adequate warning distance, causing frontal collisions instead of smooth avoidance
- **Normal Scale Actor (scale=1.0)**: Feelers at extreme angles (315° and 45°) miss walls directly ahead, and short feelers (1f unit) don't provide adequate warning distance, causing frontal collisions instead of smooth avoidance
- **Scaled Down Actor (scale=0.5)**: Force multiplier of 10f creates over-reaction relative to the smaller actor's mass (0.25x), causing erratic steering behavior
- **Multiple Feeler Detection**: When multiple feelers detect walls, only the last processed feeler's force is used, ignoring closer intersections from earlier feelers

## Expected Behavior

### Preservation Requirements

**Unchanged Behaviors:**
- Steering force combination through AccumulateSteeringForce method must continue to work with other behaviors (seek, wander, arrive)
- Debug rendering of feelers as blue lines must remain unchanged for visual debugging
- Existing parameter scaling logic for maxSpeed, maxForce, slowingDistance must be preserved
- Backward compatibility with default scale (1.0) parameter values must be maintained

**Scope:**
All inputs that do NOT involve wall proximity should be completely unaffected by this fix. This includes:
- Normal movement without nearby walls
- Other steering behaviors when walls are not present
- Actor scaling for non-wall-avoidance parameters
- Debug rendering of non-feeler elements

## Hypothesized Root Cause

Based on the code analysis, the root causes are:

1. **Incorrect Feeler Positioning**: Feelers use extreme angles (3.5π/2 = 315° and 0.5π/2 = 45°) instead of sampling the forward path with reasonable angles like ±15° to ±30°

2. **Insufficient Feeler Length**: Feelers are too short (1f * scale) to provide adequate warning distance for effective wall avoidance

3. **Improper Force Scaling**: Hardcoded 10f multiplier doesn't consider that mass scales with scale² while the force magnitude should scale proportionally to maintain consistent steering strength

4. **Algorithmic Flaw in Force Calculation**: The loop processes each feeler independently and overwrites the steering force, using only the last feeler's result instead of finding the closest intersection across all feelers

## Correctness Properties

Property 1: Fault Condition - Effective Wall Avoidance Across All Scales

_For any_ PlayerActor state where walls are approached (isBugCondition returns true), the fixed WallAvoidance method SHALL detect walls early with properly positioned feelers, calculate appropriate steering forces based on the closest intersection, and apply forces scaled correctly for the actor's mass and momentum to achieve smooth avoidance before collision.

**Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5, 2.6**

Property 2: Preservation - Non-Wall-Avoidance Behavior

_For any_ PlayerActor state where walls are not present or not being approached (isBugCondition returns false), the fixed code SHALL produce exactly the same behavior as the original code, preserving all existing steering combinations, debug rendering, and parameter scaling functionality.

**Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6**

## Fix Implementation

### Changes Required

Assuming our root cause analysis is correct:

**File**: `core/src/com/plv/uberplayground/actors/PlayerActor.java`

**Function**: `WallAvoidance` and `CreateFeelers`

**Specific Changes**:
1. **Fix Feeler Positioning in CreateFeelers**: Replace extreme angles (3.5π/2, 0.5π/2) with reasonable forward-sampling angles
   - Center feeler: straight ahead (0°)
   - Left feeler: -20° to -30° from heading
   - Right feeler: +20° to +30° from heading

2. **Increase Feeler Length**: Increase feeler length from current 1f * scale to a slightly longer length (e.g., 1.5f * scale) to provide better warning distance

3. **Fix Force Calculation Algorithm**: Modify WallAvoidance to find closest intersection across all feelers before calculating force
   - Move force calculation outside the feeler loop
   - Calculate force only once using the closest intersection point
   - Remove the per-feeler force overwriting behavior

4. **Implement Proper Force Scaling**: Replace hardcoded 10f multiplier with scale-aware force calculation
   - Consider mass scaling (scale²) in force magnitude
   - Maintain consistent steering strength relative to actor size and momentum
   - Ensure forces are proportional to maxForce scaling

5. **Add Velocity-Based Force Modulation**: Scale avoidance force based on approach velocity to prevent over-reaction at low speeds

## Testing Strategy

### Validation Approach

The testing strategy follows a two-phase approach: first, surface counterexamples that demonstrate the bug on unfixed code, then verify the fix works correctly and preserves existing behavior.

### Exploratory Fault Condition Checking

**Goal**: Surface counterexamples that demonstrate the bug BEFORE implementing the fix. Confirm or refute the root cause analysis. If we refute, we will need to re-hypothesize.

**Test Plan**: Write tests that simulate PlayerActor approaching walls at various scales and angles. Run these tests on the UNFIXED code to observe failures and understand the root cause.

**Test Cases**:
1. **Scaled Up Collision Test**: PlayerActor with scale=2.0 approaching wall head-on (will fail on unfixed code)
2. **Extreme Angle Detection Test**: PlayerActor approaching wall at slight angle where current feelers miss detection (will fail on unfixed code)  
3. **Force Scaling Test**: Compare steering force magnitude across different scales to show inconsistent behavior (will fail on unfixed code)
4. **Multiple Feeler Test**: Setup where multiple feelers detect walls but only last one's force is used (will fail on unfixed code)

**Expected Counterexamples**:
- Actors hit walls despite feelers detecting intersections
- Possible causes: insufficient warning distance, incorrect force scaling, algorithmic flaws in force calculation

### Fix Checking

**Goal**: Verify that for all inputs where the bug condition holds, the fixed function produces the expected behavior.

**Pseudocode:**
```
FOR ALL input WHERE isBugCondition(input) DO
  result := WallAvoidance_fixed(input)
  ASSERT expectedBehavior(result)
END FOR
```

### Preservation Checking

**Goal**: Verify that for all inputs where the bug condition does NOT hold, the fixed function produces the same result as the original function.

**Pseudocode:**
```
FOR ALL input WHERE NOT isBugCondition(input) DO
  ASSERT WallAvoidance_original(input) = WallAvoidance_fixed(input)
END FOR
```

**Testing Approach**: Property-based testing is recommended for preservation checking because:
- It generates many test cases automatically across the input domain
- It catches edge cases that manual unit tests might miss
- It provides strong guarantees that behavior is unchanged for all non-buggy inputs

**Test Plan**: Observe behavior on UNFIXED code first for non-wall scenarios, then write property-based tests capturing that behavior.

**Test Cases**:
1. **No Wall Preservation**: Observe that actors move normally when no walls are present on unfixed code, then write test to verify this continues after fix
2. **Steering Combination Preservation**: Observe that wall avoidance combines properly with other steering forces on unfixed code, then write test to verify this continues after fix
3. **Debug Rendering Preservation**: Observe that feeler debug lines render correctly on unfixed code, then write test to verify this continues after fix

### Unit Tests

- Test feeler positioning with various headings and scales
- Test feeler length calculation with different velocities and scales  
- Test force calculation with multiple wall intersections
- Test force scaling consistency across different actor scales

### Property-Based Tests

- Generate random actor states and scales to verify consistent wall avoidance behavior
- Generate random wall configurations to verify preservation of non-wall-avoidance behavior
- Test that steering force magnitudes scale appropriately with actor properties across many scenarios

### Integration Tests

- Test full steering behavior with wall avoidance combined with other steering forces
- Test visual feedback with feeler debug rendering during wall avoidance
- Test wall avoidance effectiveness across different game scales and wall configurations