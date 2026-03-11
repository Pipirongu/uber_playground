# Bounding Box Artifact Fix Design

## Overview

This bugfix addresses visual artifacts appearing in the bottom left corner of the game screen caused by pooled enemies positioned at (0,0) that continue to render visual elements despite being inactive. The fix ensures pooled enemies are completely invisible while preserving normal rendering for active enemies through comprehensive position-based visibility checks.

## Glossary

- **Bug_Condition (C)**: The condition that triggers the bug - when pooled enemies at position (0,0) render visual elements
- **Property (P)**: The desired behavior when pooled enemies are at (0,0) - no visual elements should be rendered
- **Preservation**: Existing rendering behavior for active enemies that must remain unchanged by the fix
- **PlayerActor**: The main actor class in `core/src/com/plv/uberplayground/actors/PlayerActor.java` that handles enemy rendering and behavior
- **EnemyPool**: The pooling system in `core/src/com/plv/uberplayground/enemies/EnemyPool.java` that manages inactive enemies at (0,0)
- **EnemyHealthBar**: The health bar rendering component in `core/src/com/plv/uberplayground/enemies/EnemyHealthBar.java`
- **EnemyBehaviorLabel**: The behavior label rendering component in `core/src/com/plv/uberplayground/enemies/EnemyBehaviorLabel.java`
- **Pooled Enemy**: An inactive enemy positioned at (0,0) waiting to be reused by the enemy pool system

## Bug Details

### Fault Condition

The bug manifests when pooled enemies positioned at (0,0) render visual elements including debug bounding boxes, health bars, and behavior labels. The rendering system is not properly checking enemy position before rendering, causing inactive pooled enemies to display visual artifacts in the bottom left corner of the screen.

**Formal Specification:**
```
FUNCTION isBugCondition(input)
  INPUT: input of type EnemyRenderingContext
  OUTPUT: boolean
  
  RETURN input.enemy.isEnemy() == true
         AND input.enemy.getBody().getPosition().equals(0, 0)
         AND (input.renderingHealthBar OR input.renderingBehaviorLabel OR input.renderingDebugElements)
         AND input.enemy.isVisible() == false
END FUNCTION
```

### Examples

- **Health Bar Artifact**: Pooled enemy at (0,0) with health bar rendered at bottom left corner when `healthBar.render()` is called
- **Behavior Label Artifact**: Pooled enemy at (0,0) with "WANDER" or other behavior text displayed at bottom left corner
- **Debug Bounding Box Artifact**: Pooled enemy at (0,0) with debug collision boundaries visible at bottom left corner when debug mode is enabled
- **Wall Rendering Artifact**: Pooled enemy at (0,0) triggering duplicate wall rendering from its `act()` method

## Expected Behavior

### Preservation Requirements

**Unchanged Behaviors:**
- Active enemies positioned away from (0,0) must continue to render health bars normally
- Active enemies positioned away from (0,0) must continue to render behavior labels normally  
- Active enemies positioned away from (0,0) must continue to show debug bounding boxes when debug mode is enabled
- Enemy pool system performance and lifecycle management must remain unchanged
- Collision detection for active enemies must continue to work correctly

**Scope:**
All rendering operations for enemies that are NOT positioned at (0,0) should be completely unaffected by this fix. This includes:
- Health bar rendering for active enemies
- Behavior label rendering for active enemies
- Debug visualization for active enemies
- Wall rendering optimization for active enemies
- Physics body rendering and collision detection

## Hypothesized Root Cause

Based on the bug description and code analysis, the most likely issues are:

1. **Missing Position Validation**: The rendering methods in PlayerActor's `act()` method check `isVisible()` and `getStage() != null` but do not validate that the enemy is not at the pooled position (0,0)

2. **Incomplete Pooling Reset**: The `EnemyPool.resetEnemy()` method sets `setVisible(false)` but the rendering logic in `act()` may still execute for enemies with physics bodies at (0,0)

3. **Render Method Position Ignorance**: The `EnemyHealthBar.render()` and `EnemyBehaviorLabel.render()` methods accept position parameters but don't validate if the position indicates a pooled enemy

4. **Debug Rendering Bypass**: Debug rendering (walls, bounding boxes) may bypass visibility checks and render for any enemy with a valid physics body, regardless of pool status

## Correctness Properties

Property 1: Fault Condition - Pooled Enemy Invisibility

_For any_ enemy positioned at (0,0) that is marked as invisible or pooled, the rendering system SHALL not display any visual elements including health bars, behavior labels, debug bounding boxes, or other visual artifacts.

**Validates: Requirements 2.1, 2.2, 2.3**

Property 2: Preservation - Active Enemy Rendering

_For any_ enemy positioned away from (0,0) that is active and visible, the rendering system SHALL produce exactly the same visual output as the original system, preserving all health bar rendering, behavior label display, and debug visualization functionality.

**Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5**

## Fix Implementation

### Changes Required

Assuming our root cause analysis is correct:

**File**: `core/src/com/plv/uberplayground/actors/PlayerActor.java`

**Function**: `act(float delta)`

**Specific Changes**:
1. **Position Validation for Wall Rendering**: Add position check before wall rendering loop
   - Check if `body.getPosition().x == 0 && body.getPosition().y == 0`
   - Skip wall rendering for pooled enemies

2. **Position Validation for Health Bar Rendering**: Add position check before health bar render call
   - Validate enemy is not at (0,0) before calling `healthBar.render()`
   - Combine with existing visibility and stage checks

3. **Position Validation for Behavior Label Rendering**: Add position check before behavior label render call
   - Validate enemy is not at (0,0) before calling `behaviorLabel.render()`
   - Combine with existing visibility and stage checks

**File**: `core/src/com/plv/uberplayground/enemies/EnemyHealthBar.java`

**Function**: `render(ShapeRenderer renderer, Camera camera, Vector2 enemyPosition, float healthPercentage, boolean forceRender)`

**Specific Changes**:
4. **Position Validation in Health Bar**: Add early return for pooled position
   - Check if `enemyPosition.x == 0 && enemyPosition.y == 0`
   - Return early without rendering if at pooled position

**File**: `core/src/com/plv/uberplayground/enemies/EnemyBehaviorLabel.java`

**Function**: `render(ShapeRenderer shapeRenderer, Camera camera, Vector2 enemyPosition, SteeringBehaviorType behaviorType, boolean forceRender)`

**Specific Changes**:
5. **Position Validation in Behavior Label**: Add early return for pooled position
   - Check if `enemyPosition.x == 0 && enemyPosition.y == 0`
   - Return early without rendering if at pooled position

## Testing Strategy

### Validation Approach

The testing strategy follows a two-phase approach: first, surface counterexamples that demonstrate the bug on unfixed code, then verify the fix works correctly and preserves existing behavior.

### Exploratory Fault Condition Checking

**Goal**: Surface counterexamples that demonstrate the bug BEFORE implementing the fix. Confirm or refute the root cause analysis. If we refute, we will need to re-hypothesize.

**Test Plan**: Write tests that create pooled enemies at (0,0) and verify that visual elements are rendered. Run these tests on the UNFIXED code to observe failures and understand the root cause.

**Test Cases**:
1. **Health Bar Rendering Test**: Create enemy at (0,0), call health bar render, verify artifact appears (will fail on unfixed code)
2. **Behavior Label Rendering Test**: Create enemy at (0,0), call behavior label render, verify artifact appears (will fail on unfixed code)  
3. **Wall Rendering Test**: Create enemy at (0,0), call act() method, verify duplicate wall rendering occurs (will fail on unfixed code)
4. **Debug Rendering Test**: Enable debug mode, create enemy at (0,0), verify bounding box artifacts appear (may fail on unfixed code)

**Expected Counterexamples**:
- Visual artifacts appear at screen coordinates (0,0) when pooled enemies render
- Possible causes: missing position validation, incomplete visibility checks, render method position ignorance

### Fix Checking

**Goal**: Verify that for all inputs where the bug condition holds, the fixed function produces the expected behavior.

**Pseudocode:**
```
FOR ALL input WHERE isBugCondition(input) DO
  result := renderingSystem_fixed(input)
  ASSERT noVisualArtifacts(result)
END FOR
```

### Preservation Checking

**Goal**: Verify that for all inputs where the bug condition does NOT hold, the fixed function produces the same result as the original function.

**Pseudocode:**
```
FOR ALL input WHERE NOT isBugCondition(input) DO
  ASSERT renderingSystem_original(input) = renderingSystem_fixed(input)
END FOR
```

**Testing Approach**: Property-based testing is recommended for preservation checking because:
- It generates many test cases automatically across the input domain
- It catches edge cases that manual unit tests might miss
- It provides strong guarantees that behavior is unchanged for all non-buggy inputs

**Test Plan**: Observe behavior on UNFIXED code first for active enemy rendering, then write property-based tests capturing that behavior.

**Test Cases**:
1. **Active Enemy Health Bar Preservation**: Observe that health bars render correctly for active enemies on unfixed code, then write test to verify this continues after fix
2. **Active Enemy Behavior Label Preservation**: Observe that behavior labels render correctly for active enemies on unfixed code, then write test to verify this continues after fix
3. **Debug Rendering Preservation**: Observe that debug elements render correctly for active enemies on unfixed code, then write test to verify this continues after fix

### Unit Tests

- Test position validation logic for enemies at exactly (0,0)
- Test position validation logic for enemies near but not at (0,0)
- Test that active enemies continue to render all visual elements correctly
- Test edge cases like enemies at (-0.001, 0.001) to ensure proper threshold handling

### Property-Based Tests

- Generate random enemy positions and verify only (0,0) enemies are filtered from rendering
- Generate random active enemy configurations and verify preservation of all rendering behavior
- Test across many game states to ensure fix works consistently across different scenarios

### Integration Tests

- Test full enemy lifecycle from spawn to pool to reactivation with rendering verification
- Test multiple pooled enemies at (0,0) to ensure no artifacts appear
- Test switching between debug and normal modes with pooled enemies present
- Test that visual feedback occurs correctly for active enemies while pooled enemies remain invisible