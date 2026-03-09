# Bugfix Requirements Document

## Introduction

The wander steering behavior in PlayerActor causes actors to rotate rapidly and erratically after running for some time, and can result in the actor completing full circular rotations. This occurs due to multiple issues: the `wanderOrientation` variable accumulates unbounded values, the wander circle can be positioned behind the actor, and there's no mechanism to prevent continuous rotation in one direction.

## Bug Analysis

### Current Behavior (Defect)

1.1 WHEN the wander behavior runs for an extended period (multiple seconds) THEN the `wanderOrientation` variable accumulates to large positive or negative values without bounds

1.2 WHEN `wanderOrientation` reaches large accumulated values THEN the actor exhibits rapid, erratic rotation instead of smooth turning

1.3 WHEN the steering force is calculated with unbounded `wanderOrientation` THEN the resulting orientation vector produces excessive angular changes

1.4 WHEN the wander circle is calculated using velocity angle THEN the circle can be positioned behind the actor (between the blue feeler lines) causing backward steering

1.5 WHEN the actor turns continuously in one direction THEN it can complete full circular rotations without changing direction

1.6 WHEN the actor needs to change direction THEN sudden orientation flips cause jarring, unnatural movement

### Expected Behavior (Correct)

2.1 WHEN the wander behavior runs for any duration THEN the `wanderOrientation` SHALL remain within a bounded range (e.g., -π to π or 0 to 2π)

2.2 WHEN `wanderOrientation` is updated each frame THEN the actor SHALL exhibit smooth, gradual turns characteristic of natural wandering behavior

2.3 WHEN the steering force is calculated THEN the orientation SHALL produce consistent, moderate angular changes regardless of elapsed time

2.4 WHEN the wander circle is calculated THEN it SHALL always be positioned in front of the actor using the body's heading

2.5 WHEN the actor accumulates rotation beyond a threshold (e.g., 120 degrees) THEN it SHALL gradually bias toward the opposite direction to prevent full circles

2.6 WHEN direction correction is applied THEN it SHALL be smooth and gradual, not sudden or jarring

### Unchanged Behavior (Regression Prevention)

3.1 WHEN the wander behavior generates random orientation changes THEN the system SHALL CONTINUE TO use `RandomBinomial()` for natural variation

3.2 WHEN the wander target position is calculated THEN the system SHALL CONTINUE TO use `CIRCLE_DISTANCE` and `CIRCLE_RADIUS` constants

3.3 WHEN wall avoidance is active THEN the system SHALL CONTINUE TO prioritize wall avoidance steering over wander steering

3.4 WHEN the actor has no velocity THEN the system SHALL CONTINUE TO handle the edge case without crashing
