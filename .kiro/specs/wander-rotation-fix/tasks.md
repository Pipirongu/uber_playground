# Implementation Plan

- [x] 1. Write bug condition exploration test
  - **Property 1: Fault Condition** - Bounded Wander Orientation
  - **CRITICAL**: This test MUST FAIL on unfixed code - failure confirms the bug exists
  - **DO NOT attempt to fix the test or the code when it fails**
  - **NOTE**: This test encodes the expected behavior - it will validate the fix when it passes after implementation
  - **GOAL**: Surface counterexamples that demonstrate the bug exists
  - **Scoped PBT Approach**: Run wander behavior for extended periods (500-1000 frames) to ensure `wanderOrientation` accumulates to large unbounded values
  - Test that `wanderOrientation` remains within [-π, π] after each frame update
  - Test that rotation behavior remains smooth (low angular velocity variance)
  - Run test on UNFIXED code
  - **EXPECTED OUTCOME**: Test FAILS (this is correct - it proves the bug exists)
  - Document counterexamples found: `wanderOrientation` values exceeding ±10π, high angular velocity variance, erratic rotation
  - Mark task complete when test is written, run, and failure is documented
  - _Requirements: 2.1, 2.2, 2.3_

- [x] 2. Write preservation property tests (BEFORE implementing fix)
  - **Property 2: Preservation** - Wander Behavior Characteristics
  - **IMPORTANT**: Follow observation-first methodology
  - Observe behavior on UNFIXED code for initial frames (before accumulation causes issues)
  - Write property-based tests capturing observed behavior patterns:
    - Random variation still uses `RandomBinomial() * 0.1f`
    - Circle parameters still use `CIRCLE_DISTANCE` and `CIRCLE_RADIUS`
    - Target position calculation produces same results for small angles
    - Wall avoidance still takes priority over wander
    - Debug visualization still renders correctly
  - Property-based testing generates many test cases for stronger guarantees
  - Run tests on UNFIXED code
  - **EXPECTED OUTCOME**: Tests PASS (this confirms baseline behavior to preserve)
  - Mark task complete when tests are written, run, and passing on unfixed code
  - _Requirements: 3.1, 3.2, 3.3, 3.4_

- [x] 3. Fix for wander rotation instability

  - [x] 3.1 Implement the fix
    - **ACTUAL IMPLEMENTATION**: Complete rewrite of Wander() method with multiple improvements:
      - Added class fields: `accumulatedRotation` and `previousHeading` for rotation tracking
      - Removed mixed implementation (old `vWanderTargert` vector code)
      - Changed angle reference from velocity (`body.getLinearVelocity().angleRad()`) to heading (`this.heading`)
      - Tuned random change rate to `0.08f` for balanced turning behavior (stronger turns than 0.05f, but controlled)
      - Added rotation tracking: measures frame-to-frame heading changes and accumulates rotation
      - Added smooth direction correction: applies gradual bias when rotation exceeds 60 degrees
      - Bias strength scales from 0 to 1 as rotation approaches 120-degree threshold
      - Maximum bias: `0.15f` (prevents full circular rotations)
      - Fixed circle center calculation to always position circle in front of actor
      - Added angle normalization: `wanderOrientation = ((wanderOrientation + MathUtils.PI) % MathUtils.PI2 + MathUtils.PI2) % MathUtils.PI2 - MathUtils.PI`
      - Updated debug visualization to show actual circle center and target position
    - _Bug_Condition: Multiple issues - unbounded orientation, velocity-based feedback loop, circle behind actor, no rotation limiting_
    - _Expected_Behavior: Bounded orientation, stable heading-based calculation, circle always in front, smooth direction correction, no full circles, balanced turning strength_
    - _Preservation: Random variation (now 0.08f), circle parameters, wall avoidance priority, debug visualization_
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 3.1, 3.2, 3.3, 3.4_

  - [x] 3.2 Verify bug condition exploration test now passes
    - **Property 1: Expected Behavior** - Bounded Wander Orientation
    - **IMPORTANT**: Re-run the SAME test from task 1 - do NOT write a new test
    - The test from task 1 encodes the expected behavior
    - When this test passes, it confirms the expected behavior is satisfied
    - Run bug condition exploration test from step 1
    - **EXPECTED OUTCOME**: Test PASSES (confirms bug is fixed)
    - _Requirements: 2.1, 2.2, 2.3_

  - [x] 3.3 Verify preservation tests still pass
    - **Property 2: Preservation** - Wander Behavior Characteristics
    - **IMPORTANT**: Re-run the SAME tests from task 2 - do NOT write new tests
    - Run preservation property tests from step 2
    - **EXPECTED OUTCOME**: Tests PASS (confirms no regressions)
    - Confirm all tests still pass after fix (no regressions)

- [x] 4. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.
