# Implementation Plan

- [x] 1. Write bug condition exploration test
  - **Property 1: Fault Condition** - Pooled Enemy Visual Artifacts
  - **CRITICAL**: This test MUST FAIL on unfixed code - failure confirms the bug exists
  - **DO NOT attempt to fix the test or the code when it fails**
  - **NOTE**: This test encodes the expected behavior - it will validate the fix when it passes after implementation
  - **GOAL**: Surface counterexamples that demonstrate the bug exists
  - **Scoped PBT Approach**: Focus on concrete failing cases: enemies at position (0,0) with visual rendering enabled
  - Test that pooled enemies at (0,0) do not render visual artifacts (health bars, behavior labels, debug elements)
  - Create test scenarios:
    - Enemy at (0,0) with health bar rendering - should produce no visual output
    - Enemy at (0,0) with behavior label rendering - should produce no visual output  
    - Enemy at (0,0) with debug rendering enabled - should produce no bounding box artifacts
    - Enemy at (0,0) in PlayerActor.act() - should not trigger wall rendering
  - Run test on UNFIXED code
  - **EXPECTED OUTCOME**: Test FAILS (this is correct - it proves the bug exists)
  - Document counterexamples found to understand root cause
  - Mark task complete when test is written, run, and failure is documented
  - _Requirements: 1.1, 1.2, 1.3_

- [x] 2. Write preservation property tests (BEFORE implementing fix)
  - **Property 2: Preservation** - Active Enemy Rendering Behavior
  - **IMPORTANT**: Follow observation-first methodology
  - Observe behavior on UNFIXED code for active enemies positioned away from (0,0)
  - Write property-based tests capturing observed behavior patterns:
    - Active enemies render health bars correctly at their actual positions
    - Active enemies render behavior labels correctly at their actual positions
    - Active enemies render debug elements correctly when debug mode is enabled
    - Enemy pool system maintains proper performance and lifecycle management
    - Collision detection works correctly for active enemies without affecting score
  - Property-based testing generates many test cases for stronger guarantees
  - Run tests on UNFIXED code
  - **EXPECTED OUTCOME**: Tests PASS (this confirms baseline behavior to preserve)
  - Mark task complete when tests are written, run, and passing on unfixed code
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

- [x] 3. Fix for bounding box artifacts from pooled enemies

  - [x] 3.1 Implement position validation in PlayerActor.act() method
    - Add position check before wall rendering loop
    - Add position check before health bar rendering call
    - Add position check before behavior label rendering call
    - Validate enemy is not at (0,0) before rendering operations
    - Combine with existing visibility and stage checks
    - _Bug_Condition: isBugCondition(input) where enemy at (0,0) renders visual elements_
    - _Expected_Behavior: No visual elements rendered for pooled enemies at (0,0)_
    - _Preservation: Active enemy rendering behavior unchanged_
    - _Requirements: 1.1, 1.2, 1.3, 2.1, 2.2, 2.3_

  - [x] 3.2 Implement position validation in EnemyHealthBar.render() method
    - Add early return for pooled position (0,0)
    - Check if enemyPosition.x == 0 && enemyPosition.y == 0
    - Return early without rendering if at pooled position
    - _Bug_Condition: Health bar renders for enemies at (0,0)_
    - _Expected_Behavior: No health bar rendering for pooled enemies_
    - _Preservation: Health bar rendering for active enemies unchanged_
    - _Requirements: 1.2, 2.2, 3.1_

  - [x] 3.3 Implement position validation in EnemyBehaviorLabel.render() method
    - Add early return for pooled position (0,0)
    - Check if enemyPosition.x == 0 && enemyPosition.y == 0
    - Return early without rendering if at pooled position
    - _Bug_Condition: Behavior label renders for enemies at (0,0)_
    - _Expected_Behavior: No behavior label rendering for pooled enemies_
    - _Preservation: Behavior label rendering for active enemies unchanged_
    - _Requirements: 1.2, 2.2, 3.2_

  - [x] 3.4 Verify bug condition exploration test now passes
    - **Property 1: Expected Behavior** - Pooled Enemy Visual Artifacts
    - **IMPORTANT**: Re-run the SAME test from task 1 - do NOT write a new test
    - The test from task 1 encodes the expected behavior
    - When this test passes, it confirms the expected behavior is satisfied
    - Run bug condition exploration test from step 1
    - **EXPECTED OUTCOME**: Test PASSES (confirms bug is fixed)
    - _Requirements: Expected Behavior Properties from design_

  - [x] 3.5 Verify preservation tests still pass
    - **Property 2: Preservation** - Active Enemy Rendering Behavior
    - **IMPORTANT**: Re-run the SAME tests from task 2 - do NOT write new tests
    - Run preservation property tests from step 2
    - **EXPECTED OUTCOME**: Tests PASS (confirms no regressions)
    - Confirm all tests still pass after fix (no regressions)

- [x] 4. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.