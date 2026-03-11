# Implementation Plan

- [x] 1. Write bug condition exploration test
  - **Property 1: Fault Condition** - Ineffective Wall Avoidance Across All Scales
  - **CRITICAL**: This test MUST FAIL on unfixed code - failure confirms the bug exists
  - **DO NOT attempt to fix the test or the code when it fails**
  - **NOTE**: This test encodes the expected behavior - it will validate the fix when it passes after implementation
  - **GOAL**: Surface counterexamples that demonstrate the bug exists
  - **Scoped PBT Approach**: Focus on concrete failing cases: scaled actors approaching walls with current feeler positioning and force calculation
  - Test that PlayerActor with various scales (0.5, 1.0, 2.0) approaching walls fails to avoid collision due to:
    - Feelers positioned at extreme angles (315° and 45°) missing walls directly ahead
    - Feelers that are too short (1f * scale) to provide adequate warning distance
    - Improper force scaling that doesn't account for scaled mass
    - Algorithmic flaw where only last feeler's force is used instead of closest intersection
  - Run test on UNFIXED code
  - **EXPECTED OUTCOME**: Test FAILS (this is correct - it proves the bug exists)
  - Document counterexamples found to understand root cause
  - Mark task complete when test is written, run, and failure is documented
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6_

- [x] 2. Write preservation property tests (BEFORE implementing fix)
  - **Property 2: Preservation** - Non-Wall-Avoidance Behavior
  - **IMPORTANT**: Follow observation-first methodology
  - Observe behavior on UNFIXED code for non-wall scenarios:
    - Normal movement without nearby walls
    - Steering force combination with other behaviors (seek, wander, arrive)
    - Debug rendering of feelers as blue lines
    - Parameter scaling for maxSpeed, maxForce, slowingDistance
  - Write property-based tests capturing observed behavior patterns from Preservation Requirements
  - Property-based testing generates many test cases for stronger guarantees
  - Run tests on UNFIXED code
  - **EXPECTED OUTCOME**: Tests PASS (this confirms baseline behavior to preserve)
  - Mark task complete when tests are written, run, and passing on unfixed code
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6_

- [x] 3. Fix PlayerActor wall avoidance behavior

  - [x] 3.1 Fix feeler positioning in CreateFeelers method
    - Replace extreme angles (3.5π/2 = 315°, 0.5π/2 = 45°) with proper forward-sampling angles
    - Implement center feeler: straight ahead (0° relative to heading)
    - Implement left feeler: -20° to -30° from heading
    - Implement right feeler: +20° to +30° from heading
    - Ensure feelers properly sample the forward path for wall detection
    - _Bug_Condition: isBugCondition(input) where input.feelerAnglesIncorrect == true_
    - _Expected_Behavior: Feelers positioned to detect walls in forward path_
    - _Preservation: Debug rendering of feelers as blue lines must remain unchanged_
    - _Requirements: 2.1, 2.2, 3.2_

  - [x] 3.2 Increase feeler length for better warning distance
    - Increase feeler length from current 1f * scale to 1.5f * scale
    - Provide better warning distance for wall detection without making feelers too long
    - Maintain proportional scaling with actor size
    - _Bug_Condition: isBugCondition(input) where input.feelerLengthTooShort == true_
    - _Expected_Behavior: Feelers provide adequate warning distance for effective wall avoidance_
    - _Preservation: Existing parameter scaling logic must be preserved_
    - _Requirements: 2.4, 3.5_

  - [x] 3.3 Fix force calculation algorithm in WallAvoidance method
    - Move force calculation outside the feeler loop to prevent overwriting
    - Find closest intersection across all feelers before calculating force
    - Calculate steering force only once using the closest intersection point
    - Remove per-feeler force overwriting behavior
    - _Bug_Condition: isBugCondition(input) where multiple feelers detect walls but only last one's force is used_
    - _Expected_Behavior: Steering force calculated from closest wall intersection across all feelers_
    - _Preservation: Steering force combination through AccumulateSteeringForce must continue to work_
    - _Requirements: 2.2, 3.1_

  - [x] 3.4 Implement proper force scaling
    - Replace hardcoded 10f multiplier with scale-aware force calculation
    - Consider mass scaling (scale²) in force magnitude calculation
    - Maintain consistent steering strength relative to actor size and momentum
    - Ensure forces are proportional to maxForce scaling
    - Add velocity-based force modulation to prevent over-reaction at low speeds
    - _Bug_Condition: isBugCondition(input) where input.forceScalingImproper == true_
    - _Expected_Behavior: Force magnitude scales appropriately with actor mass and momentum_
    - _Preservation: Backward compatibility with default scale (1.0) parameter values_
    - _Requirements: 2.5, 2.6, 3.4, 3.5_

  - [x] 3.5 Verify bug condition exploration test now passes
    - **Property 1: Expected Behavior** - Effective Wall Avoidance Across All Scales
    - **IMPORTANT**: Re-run the SAME test from task 1 - do NOT write a new test
    - The test from task 1 encodes the expected behavior
    - When this test passes, it confirms the expected behavior is satisfied
    - Run bug condition exploration test from step 1
    - **EXPECTED OUTCOME**: Test PASSES (confirms bug is fixed)
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6_

  - [x] 3.6 Verify preservation tests still pass
    - **Property 2: Preservation** - Non-Wall-Avoidance Behavior
    - **IMPORTANT**: Re-run the SAME tests from task 2 - do NOT write new tests
    - Run preservation property tests from step 2
    - **EXPECTED OUTCOME**: Tests PASS (confirms no regressions)
    - Confirm all tests still pass after fix (no regressions)
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6_

- [x] 4. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.