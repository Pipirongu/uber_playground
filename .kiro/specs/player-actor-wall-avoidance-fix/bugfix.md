# Bugfix Requirements Document

## Introduction

The PlayerActor's wall avoidance steering behavior is not working effectively, particularly when the game is scaled. Despite having feelers for wall detection, the PlayerActor collides with walls before changing direction instead of smoothly avoiding them. The current implementation has issues with feeler positioning, length, steering force calculation, and scaling behavior that prevent proper wall avoidance behavior across different game scales.

## Bug Analysis

### Current Behavior (Defect)

1.1 WHEN PlayerActor approaches a wall at any angle THEN the system hits the wall directly before attempting to change direction

1.2 WHEN PlayerActor's feelers detect a wall intersection THEN the system calculates steering force only for the last feeler processed, ignoring earlier detections

1.3 WHEN PlayerActor needs to avoid walls THEN the system uses incorrectly positioned feelers with extreme angles (3.5π/2 and 0.5π/2) that don't properly sample the forward path

1.4 WHEN PlayerActor needs to avoid walls THEN the system uses feelers that are too short (1f * scale) to provide adequate warning distance for effective avoidance

1.5 WHEN PlayerActor is scaled up (scale > 1.0) THEN the system applies wall avoidance force with a hardcoded multiplier (10f) that doesn't account for the scaled mass (scale^2) and maxForce scaling

1.6 WHEN PlayerActor is scaled down (scale < 1.0) THEN the system may over-react to walls due to disproportionate force scaling relative to actor size

### Expected Behavior (Correct)

2.1 WHEN PlayerActor approaches a wall at any angle THEN the system SHALL detect the wall early and smoothly steer away before collision

2.2 WHEN PlayerActor's feelers detect wall intersections THEN the system SHALL calculate steering force based on the closest intersection point from all feelers

2.3 WHEN PlayerActor needs to avoid walls THEN the system SHALL use properly positioned feelers that sample the forward path at reasonable angles (straight ahead, slight left, slight right)

2.4 WHEN PlayerActor needs to avoid walls THEN the system SHALL use feelers with adequate length (slightly longer than current 1f * scale) to provide sufficient warning distance for effective avoidance

2.5 WHEN PlayerActor is scaled up (scale > 1.0) THEN the system SHALL apply wall avoidance force with scaling that accounts for the actor's scaled mass and maxForce to maintain consistent avoidance behavior

2.6 WHEN PlayerActor is scaled down (scale < 1.0) THEN the system SHALL apply proportionally appropriate wall avoidance forces that don't cause over-reaction relative to the smaller actor size

### Unchanged Behavior (Regression Prevention)

3.1 WHEN PlayerActor is not near any walls THEN the system SHALL CONTINUE TO move normally without wall avoidance steering forces

3.2 WHEN PlayerActor is using other steering behaviors (seek, wander, arrive) THEN the system SHALL CONTINUE TO combine them properly with wall avoidance forces

3.3 WHEN PlayerActor's feelers are rendered for debugging THEN the system SHALL CONTINUE TO display them visually as blue lines

3.4 WHEN PlayerActor is created with default scale (1.0) THEN the system SHALL CONTINUE TO use the existing parameter values for backward compatibility

3.5 WHEN PlayerActor is scaled THEN the system SHALL CONTINUE TO scale maxSpeed, maxForce, slowingDistance, and other parameters according to the existing scaling logic

3.6 WHEN PlayerActor uses wall avoidance in combination with other steering forces THEN the system SHALL CONTINUE TO respect the maxForce limit through the AccumulateSteeringForce method