# Implementation Plan: Enhanced Projectile Graphics

## Overview

This implementation plan converts the enhanced projectile graphics design into actionable coding tasks. The enhancement focuses on three key areas: switching from "agent" to "spawn_unit" graphics for visual consistency, applying 20.0f scaling factor for high visibility, and increasing projectile speed from 5000.0f to 7500.0f pixels per second. The implementation leverages the existing AnimatedPhysicsActor architecture and dual-velocity physics approach.

## Tasks

- [x] 1. Update ProjectilePool graphics configuration
  - [x] 1.1 Modify ProjectilePool constructor to use "spawn_unit" graphics
    - Change animation name parameter from "agent" to "spawn_unit" in Projectile instantiation
    - Maintain existing frame configuration (1 column, 1 row)
    - _Requirements: 1.1, 1.2, 1.3_

  - [ ]* 1.2 Write property test for graphics consistency
    - **Property 1: Graphics Consistency**
    - **Validates: Requirements 1.1, 1.2, 1.3**

- [x] 2. Enhance Projectile class constants and scaling
  - [x] 2.1 Update projectile speed constant
    - Change PROJECTILE_SPEED from 5000.0f to 7500.0f pixels per second
    - Maintain PROJECTILE_LIFETIME at 15.0f seconds
    - _Requirements: 3.1, 3.4_

  - [x] 2.2 Add explicit scaling to Projectile constructor
    - Update super() call to include 20.0f scaling parameter
    - Ensure scaling is applied to both visual rendering and physics body
    - _Requirements: 2.1, 2.3_

  - [ ]* 2.3 Write property test for scaling consistency
    - **Property 2: Scaling Consistency**
    - **Validates: Requirements 2.1, 2.3, 2.5**

  - [ ]* 2.4 Write property test for configurable scaling
    - **Property 3: Configurable Scaling**
    - **Validates: Requirements 2.2**

- [x] 3. Checkpoint - Verify core changes compile and basic functionality works
  - Ensure all tests pass, ask the user if questions arise.

- [x] 4. Validate collision system consistency
  - [x] 4.1 Verify collision boundaries update with new graphics
    - Confirm physics body dimensions match "spawn_unit" texture dimensions
    - Ensure collision filtering and categories remain unchanged
    - _Requirements: 1.4, 2.4, 2.5_

  - [ ]* 4.2 Write property test for collision system consistency
    - **Property 4: Collision System Consistency**
    - **Validates: Requirements 1.4, 4.3, 4.4**

- [x] 5. Validate enhanced speed performance
  - [x] 5.1 Test movement accuracy at enhanced speed
    - Verify position calculations remain accurate at 7500.0f pixels/second
    - Ensure smooth visual movement without artifacts
    - _Requirements: 3.2_

  - [ ]* 5.2 Write property test for movement at enhanced speed
    - **Property 5: Movement at Enhanced Speed**
    - **Validates: Requirements 3.2**

  - [ ]* 5.3 Write property test for collision accuracy at enhanced speed
    - **Property 6: Collision Accuracy at Enhanced Speed**
    - **Validates: Requirements 3.3**

- [x] 6. Backward compatibility validation
  - [x] 6.1 Verify all public method signatures remain unchanged
    - Test ProjectilePool interface compatibility
    - Confirm Projectile class public API preservation
    - _Requirements: 4.1, 4.2_

  - [ ]* 6.2 Write unit tests for backward compatibility
    - Test existing collision detection behavior preservation
    - Verify physics properties (density, damping, bullet physics) maintained
    - _Requirements: 4.3, 4.4_

- [x] 7. Integration testing and validation
  - [x] 7.1 Test complete projectile lifecycle with enhancements
    - Verify projectile creation, movement, collision, and cleanup
    - Test pool management with enhanced projectiles
    - _Requirements: 1.4, 2.4, 3.2, 3.3_

  - [ ]* 7.2 Write integration tests for enhanced projectile system
    - Test end-to-end projectile behavior with new graphics and speed
    - Verify interaction with boundaries and enemies
    - _Requirements: 1.4, 3.2, 3.3, 4.4_

- [x] 8. Final checkpoint - Ensure all tests pass and feature is complete
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Property tests validate universal correctness properties from the design document
- Unit tests validate specific examples and edge cases
- The dual-velocity physics approach (low Box2D velocity + high visual speed) is maintained
- All changes leverage existing AnimatedPhysicsActor scaling capabilities
- Graphics asset "spawn_unit.png" must exist in android/assets/actors/ directory