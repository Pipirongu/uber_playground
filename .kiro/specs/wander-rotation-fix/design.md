# Wander Rotation Fix Bugfix Design

## Overview

The wander steering behavior in PlayerActor exhibits erratic rotation after running for extended periods due to unbounded accumulation of the `wanderOrientation` variable. The fix normalizes this angle to a bounded range (-π to π) after each update, ensuring smooth, natural wandering behavior regardless of elapsed time. This is a minimal, targeted fix that preserves all existing wander behavior characteristics while eliminating the rotation instability.

## Glossary

- **Bug_Condition (C)**: The condition that triggers the bug - when `wanderOrientation` accumulates to large unbounded values over time
- **Property (P)**: The desired behavior - `wanderOrientation` remains within [-π, π] and produces smooth rotation
- **Preservation**: Existing wander randomness, circle distance/radius parameters, and wall avoidance behavior that must remain unchanged
- **wanderOrientation**: The accumulated angle offset (in radians) used to calculate the wander target position on the circle
- **Wander()**: The method in `PlayerActor.java` that implements the wander steering behavior
- **CIRCLE_DISTANCE**: The distance ahead of the actor where the wander circle is projected
- **CIRCLE_RADIUS**: The radius of the wander circle on which the target point moves
- **RandomBinomial()**: Helper function that returns values in range [-1, 1] for natural random variation

## Bug Details

### Fault Condition

The bug manifests when the wander behavior runs continuously over multiple frames. The `wanderOrientation` variable is incremented each frame by `RandomBinomial() * 0.1f` without any normalization, causing it to grow unbounded in either positive or negative direction. When the accumulated value becomes large (e.g., > 10π), the trigonometric calculations produce erratic angular changes that result in rapid, jerky rotation instead of smooth turning.

**Formal Specification:**
```
FUNCTION isBugCondition(state)
  INPUT: state containing wanderOrientation value and elapsed frames
  OUTPUT: boolean
  
  RETURN (abs(state.wanderOrientation) > π)
         AND (state.elapsedFrames > threshold)
         AND (rotation appears erratic)
END FUNCTION
```

### Examples

- **Example 1**: After 100 frames with positive random values, `wanderOrientation = 5.2` (> π), causing the actor to spin rapidly clockwise instead of gentle turning
- **Example 2**: After 200 frames with mixed random values, `wanderOrientation = -8.7` (< -π), causing the actor to exhibit jerky counter-clockwise rotation
- **Example 3**: In first 10 frames, `wanderOrientation = 0.3` (within [-π, π]), actor exhibits smooth, natural wandering behavior as expected
- **Edge Case**: After 1000 frames, `wanderOrientation = 47.2`, the accumulated angle wraps around the unit circle multiple times, producing unpredictable rotation behavior

## Expected Behavior

### Preservation Requirements

**Unchanged Behaviors:**
- Random orientation changes must continue to use `RandomBinomial() * 0.1f` for natural variation
- Wander target calculation must continue to use `CIRCLE_DISTANCE` and `CIRCLE_RADIUS` constants
- Wall avoidance steering must continue to take priority over wander steering in `CalculateSteeringForce()`
- Edge case handling when actor has no velocity must continue to work without crashes
- Debug visualization (red circle, green target point) must continue to render correctly

**Scope:**
All inputs and behaviors that do NOT involve the accumulated `wanderOrientation` value should be completely unaffected by this fix. This includes:
- Mouse/touch input for actor control
- Collision detection and physics simulation
- Other steering behaviors (Seek, Arrive, WallAvoidance)
- Animation and rendering logic
- Debug drawing functionality

## Hypothesized Root Cause

Based on the bug description, code analysis, and iterative testing, the root causes are:

1. **Missing Angle Normalization**: The `wanderOrientation` variable updates without normalization, causing unbounded accumulation over time.

2. **Inconsistent Angle Reference**: The original code used velocity angle (`body.getLinearVelocity().angleRad()`) which creates a feedback loop - velocity changes affect the wander calculation, which changes velocity further, compounding the spinning effect.

3. **Mixed Implementation**: Two different wander implementations were present in the code - one using `vWanderTargert` vector and another using `wanderOrientation` angle, with inconsistent debug visualization.

4. **Circle Positioning Behind Actor**: Using velocity angle for circle center calculation could position the wander circle behind the actor (between the blue feeler lines), causing backward steering.

5. **No Rotation Limiting**: There was no mechanism to prevent the actor from completing full circular rotations, leading to unnatural movement patterns.

6. **Rate of Change Too High**: The original `0.1f` multiplier for random changes was too aggressive, contributing to rapid spinning.

## Correctness Properties

Property 1: Fault Condition - Bounded Wander Orientation and Smooth Rotation

_For any_ frame update where the wander behavior is active, the fixed `Wander()` method SHALL ensure that:
- `wanderOrientation` remains within the range [-π, π] after the update
- The wander circle is always positioned in front of the actor
- Accumulated rotation does not exceed approximately 120 degrees in one direction
- Direction corrections are applied smoothly and gradually
- The actor produces smooth and consistent rotation behavior regardless of how many frames have elapsed

**Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5, 2.6**

Property 2: Preservation - Wander Behavior Characteristics

_For any_ frame update where the wander behavior is active, the fixed code SHALL produce the same random variation pattern, use the same circle distance and radius parameters, and maintain the same relationship with wall avoidance as the original code, preserving all existing wander behavior characteristics except for the angle normalization.

**Validates: Requirements 3.1, 3.2, 3.3, 3.4**

## Fix Implementation

### Changes Required

**File**: `core/src/com/plv/uberplayground/actors/PlayerActor.java`

**Class Fields** (lines 17-40):
- Add `accumulatedRotation` field to track total rotation
- Add `previousHeading` field to measure frame-to-frame rotation changes

**Function**: `Wander()` (line 170)

**Specific Changes**:

1. **Complete Method Rewrite**: Replace the mixed implementation with a clean, heading-based approach:
   - Remove the old `vWanderTargert` vector-based code
   - Use `this.heading` (smoothed body rotation) instead of velocity angle
   - Set random change rate to `0.08f` for balanced turning behavior (not too gentle, not too aggressive)

2. **Add Rotation Tracking**: 
   - Calculate heading delta from previous frame
   - Accumulate rotation when turning in the same direction
   - Reset accumulator when direction changes naturally

3. **Add Smooth Direction Correction**:
   - When accumulated rotation exceeds 60 degrees (half of 120-degree threshold), start applying bias
   - Bias strength scales from 0 to 1 as rotation approaches 120 degrees
   - Bias is added to random wander: `wanderOrientation += RandomBinomial() * 0.05f + bias`
   - Maximum bias strength: `0.15f` (adjustable for stronger/weaker correction)

4. **Add Angle Normalization**: 
   ```java
   wanderOrientation = ((wanderOrientation + MathUtils.PI) % MathUtils.PI2 + MathUtils.PI2) % MathUtils.PI2 - MathUtils.PI;
   ```

5. **Fix Circle Center Calculation**:
   ```java
   float angle = this.heading + MathUtils.PI / 2f;
   Vector2 forwardDirection = new Vector2(MathUtils.cos(angle), MathUtils.sin(angle));
   Vector2 circleCenter = this.body.getPosition().cpy().add(forwardDirection.cpy().scl(CIRCLE_DISTANCE));
   ```

6. **Fix Debug Visualization**: Update to show actual circle center and target position being used

**Implementation**:
```java
public Vector2 Wander(){
    // Track rotation to prevent full circles
    float headingDelta = this.heading - this.previousHeading;
    
    // Normalize the delta to [-π, π]
    while (headingDelta > MathUtils.PI) headingDelta -= MathUtils.PI2;
    while (headingDelta < -MathUtils.PI) headingDelta += MathUtils.PI2;
    
    // Accumulate rotation in the same direction
    if (Math.abs(headingDelta) > 0.01f) {
        if ((accumulatedRotation > 0 && headingDelta > 0) || 
            (accumulatedRotation < 0 && headingDelta < 0)) {
            accumulatedRotation += headingDelta;
        } else {
            accumulatedRotation = headingDelta;
        }
    }
    
    // Calculate bias to gradually push away from continued rotation
    float maxRotation = MathUtils.PI * 0.66f; // About 120 degrees
    float bias = 0;
    
    if (Math.abs(accumulatedRotation) > maxRotation * 0.5f) {
        float excessRotation = Math.abs(accumulatedRotation) - (maxRotation * 0.5f);
        float biasStrength = excessRotation / (maxRotation * 0.5f);
        bias = -Math.signum(accumulatedRotation) * biasStrength * 0.15f;
    }
    
    // Normal wander behavior with bias applied
    wanderOrientation += RandomBinomial() * 0.08f + bias;
    
    // Reset accumulator if we've successfully changed direction
    if (Math.abs(accumulatedRotation) > maxRotation && 
        Math.signum(accumulatedRotation) != Math.signum(wanderOrientation)) {
        accumulatedRotation = 0;
    }
    
    // Normalize to [-π, π] range
    wanderOrientation = ((wanderOrientation + MathUtils.PI) % MathUtils.PI2 + 
                         MathUtils.PI2) % MathUtils.PI2 - MathUtils.PI;
    
    this.previousHeading = this.heading;
    
    // Use heading for stable forward direction
    float angle = this.heading + MathUtils.PI / 2f;
    Vector2 forwardDirection = new Vector2(MathUtils.cos(angle), MathUtils.sin(angle));
    Vector2 circleCenter = this.body.getPosition().cpy().add(
        forwardDirection.cpy().scl(CIRCLE_DISTANCE));
    
    Vector2 wanderDirection = new Vector2(
        MathUtils.cos(angle + wanderOrientation), 
        MathUtils.sin(angle + wanderOrientation));
    Vector2 targetPosition = circleCenter.cpy().add(
        wanderDirection.cpy().scl(CIRCLE_RADIUS));
    
    // Debug visualization
    if(Configuration.isDebug){
        // Red circle shows wander circle, green dot shows target
        // ... (visualization code)
    }
    
    return this.Seek(targetPosition);
}
```

**Key Improvements**:
- Wander circle always positioned in front of actor
- Smooth, gradual direction correction prevents full circles
- Consistent use of heading instead of velocity angle
- Tuned rate of change (0.08f) for balanced turning behavior
- Clean, maintainable implementation

## Testing Strategy

### Validation Approach

The testing strategy follows a two-phase approach: first, surface counterexamples that demonstrate the bug on unfixed code by running the wander behavior for extended periods, then verify the fix keeps `wanderOrientation` bounded and preserves existing behavior.

### Exploratory Fault Condition Checking

**Goal**: Surface counterexamples that demonstrate the bug BEFORE implementing the fix. Confirm that `wanderOrientation` grows unbounded and causes erratic rotation.

**Test Plan**: Create a test that runs the wander behavior for many frames (e.g., 500-1000 frames) and logs the `wanderOrientation` value. Run this test on the UNFIXED code to observe the unbounded growth and correlate it with erratic rotation behavior.

**Test Cases**:
1. **Long Duration Wander Test**: Run wander for 1000 frames, log `wanderOrientation` every 100 frames (will show unbounded growth on unfixed code)
2. **Positive Bias Test**: Mock `RandomBinomial()` to return mostly positive values, verify `wanderOrientation` grows large positive (will fail on unfixed code)
3. **Negative Bias Test**: Mock `RandomBinomial()` to return mostly negative values, verify `wanderOrientation` grows large negative (will fail on unfixed code)
4. **Rotation Smoothness Test**: Measure angular velocity variance over 500 frames (will show high variance on unfixed code after accumulation)

**Expected Counterexamples**:
- `wanderOrientation` values exceeding ±10π after 500+ frames
- Increased angular velocity variance as `wanderOrientation` grows
- Visual observation of jerky, erratic rotation in game

### Fix Checking

**Goal**: Verify that for all frame updates where the wander behavior is active, the fixed function keeps `wanderOrientation` bounded.

**Pseudocode:**
```
FOR ALL frame_update WHERE wander_is_active DO
  Wander_fixed()
  ASSERT wanderOrientation >= -π AND wanderOrientation <= π
END FOR
```

**Test Cases**:
1. **Bounded Range Test**: Run wander for 10,000 frames, assert `wanderOrientation` stays in [-π, π] after every update
2. **Smooth Rotation Test**: Measure angular velocity variance over 1000 frames, assert it remains low and consistent
3. **Visual Inspection**: Run game with fixed code, observe smooth, natural wandering behavior

### Preservation Checking

**Goal**: Verify that for all aspects of wander behavior except angle normalization, the fixed function produces the same results as the original function.

**Pseudocode:**
```
FOR ALL frame_update WHERE wander_is_active DO
  // Capture behavior before normalization
  original_random_value = RandomBinomial()
  original_circle_distance = CIRCLE_DISTANCE
  original_circle_radius = CIRCLE_RADIUS
  
  result_fixed = Wander_fixed()
  
  // Verify same parameters used
  ASSERT fixed_uses_same_random_function
  ASSERT fixed_uses_same_circle_distance
  ASSERT fixed_uses_same_circle_radius
END FOR
```

**Testing Approach**: Property-based testing is recommended for preservation checking because:
- It generates many test cases automatically across different random seeds
- It catches edge cases in the normalization logic (e.g., angles near ±π)
- It provides strong guarantees that behavior is unchanged except for the normalization

**Test Plan**: Observe behavior on UNFIXED code first for the initial frames (before accumulation causes issues), then write property-based tests capturing that behavior and verify it continues after fix.

**Test Cases**:
1. **Random Variation Preservation**: Verify `RandomBinomial()` is still called and its result is still multiplied by 0.1f
2. **Circle Parameters Preservation**: Verify `CIRCLE_DISTANCE` and `CIRCLE_RADIUS` are still used in calculations
3. **Target Position Preservation**: For small `wanderOrientation` values (< π), verify target position calculation produces same results
4. **Wall Avoidance Priority Preservation**: Verify wall avoidance still overrides wander in `CalculateSteeringForce()`
5. **Debug Visualization Preservation**: Verify red circle and green target point still render correctly

### Unit Tests

- Test angle normalization logic in isolation with various input values (0, π, -π, 2π, -2π, 10π, -10π)
- Test that `wanderOrientation` stays bounded after 1000 consecutive updates
- Test edge cases: exactly π, exactly -π, values very close to boundaries
- Test that normalized angles produce correct trigonometric values

### Property-Based Tests

- Generate random sequences of `RandomBinomial()` outputs and verify `wanderOrientation` always stays in [-π, π]
- Generate random initial `wanderOrientation` values and verify normalization brings them into range
- Test that for any two angles that differ by 2πn, the resulting target positions are equivalent
- Verify that angular velocity variance remains low across many random seeds

### Integration Tests

- Run full game with wander behavior for extended periods (5+ minutes of game time)
- Test wander behavior in combination with wall avoidance
- Test wander behavior with different actor velocities (slow, medium, fast)
- Verify smooth visual behavior and no erratic rotation in actual gameplay
