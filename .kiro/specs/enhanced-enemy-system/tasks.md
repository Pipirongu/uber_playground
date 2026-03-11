# Implementation Plan: Enhanced Enemy System

## Overview

This implementation plan creates a comprehensive enemy system for the LibGDX game, extending the existing PlayerActor architecture with AI behaviors, health management, and combat interactions. The system integrates seamlessly with the current GameScreen collision handling and provides engaging gameplay through intelligent enemy behaviors.

## Tasks

- [x] 1. Set up core system infrastructure and configuration
  - Create enemy system package structure under `com.plv.uberplayground.enemies`
  - Define SpawnConfiguration class with gameplay constants
  - Create SteeringBehaviorType enum for behavior management
  - Set up basic interfaces and data models
  - _Requirements: 8.1, 8.2, 8.3_

- [ ]* 1.1 Write property test for spawn configuration constants
  - **Property 22: Enemy Count Limit Enforcement**
  - **Validates: Requirements 8.1, 8.2**

- [x] 2. Implement health management system
  - [x] 2.1 Create HealthComponent class for enemy health tracking
    - Implement health initialization with 100 HP
    - Add damage processing with 25 damage per projectile hit
    - Include health percentage calculation for visual display
    - _Requirements: 4.1, 4.3_

  - [ ]* 2.2 Write property test for health initialization and management
    - **Property 12: Health Initialization and Management**
    - **Validates: Requirements 4.1**

  - [x] 2.3 Create HealthSystem class for centralized health management
    - Implement enemy health registration and tracking
    - Add damage processing from projectile collisions
    - Include enemy destruction logic when health reaches zero
    - _Requirements: 4.3, 4.4_

  - [ ]* 2.4 Write property test for projectile damage processing
    - **Property 14: Projectile Damage Processing**
    - **Validates: Requirements 4.3, 5.1, 5.2**

  - [ ]* 2.5 Write property test for enemy destruction on zero health
    - **Property 15: Enemy Destruction on Zero Health**
    - **Validates: Requirements 4.4, 8.4**

- [x] 3. Create visual health bar system
  - [x] 3.1 Implement EnemyHealthBar class for visual health display
    - Create health bar rendering with ShapeRenderer
    - Implement efficient updates only when health changes
    - Add proper positioning relative to enemy location
    - _Requirements: 4.2, 4.5, 8.3_

  - [ ]* 3.2 Write property test for health bar visual accuracy
    - **Property 13: Health Bar Visual Accuracy**
    - **Validates: Requirements 4.2, 4.5**

  - [ ]* 3.3 Write property test for health bar rendering optimization
    - **Property 23: Health Bar Rendering Optimization**
    - **Validates: Requirements 8.3**

- [x] 4. Extend PlayerActor for enemy functionality
  - [x] 4.1 Add enemy-specific properties to PlayerActor class
    - Add isEnemy flag and health component integration
    - Include behavior state management (current behavior, timers)
    - Add health bar instance and patrol waypoints
    - _Requirements: 2.1, 2.2, 4.1_

  - [x] 4.2 Integrate health bar rendering into PlayerActor.act() method
    - Add health bar rendering during enemy update cycle
    - Ensure proper positioning and visibility
    - _Requirements: 4.2, 4.5_

- [x] 5. Implement extended steering behavior library
  - [x] 5.1 Create flee behavior implementation
    - Add flee method to SteeringBehaviorLibrary
    - Implement movement away from player position
    - Integrate with existing PlayerActor steering system
    - _Requirements: 3.2_

  - [ ]* 5.2 Write property test for flee behavior directional correctness
    - **Property 7: Flee Behavior Directional Correctness**
    - **Validates: Requirements 3.2**

  - [x] 5.3 Create patrol behavior implementation
    - Add patrol method with waypoint following logic
    - Implement waypoint sequence management
    - Handle waypoint reaching and transition logic
    - _Requirements: 3.3_

  - [ ]* 5.4 Write property test for patrol behavior waypoint following
    - **Property 8: Patrol Behavior Waypoint Following**
    - **Validates: Requirements 3.3**

  - [x] 5.5 Create orbit behavior implementation
    - Add orbit method with circular movement around player
    - Implement fixed distance maintenance logic
    - Handle smooth circular motion calculations
    - _Requirements: 3.4_

  - [ ]* 5.6 Write property test for orbit behavior distance maintenance
    - **Property 9: Orbit Behavior Distance Maintenance**
    - **Validates: Requirements 3.4**

  - [x] 5.7 Create intercept behavior implementation
    - Add intercept method with predictive movement
    - Implement player velocity prediction logic
    - Calculate intercept trajectory and movement
    - _Requirements: 3.5_

  - [ ]* 5.8 Write property test for intercept behavior predictive movement
    - **Property 10: Intercept Behavior Predictive Movement**
    - **Validates: Requirements 3.5**

  - [x] 5.9 Create flocking behavior implementation (advanced)
    - Add flocking method with group coordination
    - Implement neighbor detection and group cohesion
    - Handle separation, alignment, and cohesion forces
    - _Requirements: 3.6_

  - [ ]* 5.10 Write property test for flocking behavior group coordination
    - **Property 11: Flocking Behavior Group Coordination**
    - **Validates: Requirements 3.6**

- [x] 6. Checkpoint - Ensure behavior system tests pass
  - Ensure all behavior tests pass, ask the user if questions arise.

- [x] 7. Implement enemy behavior management system
  - [x] 7.1 Add behavior switching logic to PlayerActor
    - Implement independent behavior timer management
    - Add random behavior selection with 2-10 second intervals
    - Ensure behavior switching doesn't affect other enemies
    - _Requirements: 2.2, 2.3, 2.5, 2.6_

  - [ ]* 7.2 Write property test for independent behavior management
    - **Property 5: Independent Behavior Management**
    - **Validates: Requirements 2.1, 2.2, 2.3, 2.5, 2.6**

  - [x] 7.3 Add seek behavior targeting integration
    - Ensure seek behavior targets current player position
    - Integrate with existing seek implementation
    - _Requirements: 2.4_

  - [ ]* 7.4 Write property test for seek behavior targeting accuracy
    - **Property 6: Seek Behavior Targeting Accuracy**
    - **Validates: Requirements 2.4**

- [x] 8. Create enemy manager system
  - [x] 8.1 Implement EnemyManager class for enemy lifecycle management
    - Create enemy registration and deregistration system
    - Add efficient enemy update coordination
    - Implement enemy count tracking and limits
    - _Requirements: 8.1, 8.2, 8.4_

  - [x] 8.2 Add enemy removal and cleanup functionality
    - Implement proper enemy destruction and memory cleanup
    - Ensure destroyed enemies are removed within one frame
    - Handle health system integration for enemy removal
    - _Requirements: 4.4, 8.4_

- [x] 9. Implement enemy spawning system
  - [x] 9.1 Create EnemySpawner class with timed spawning
    - Implement 5-second spawn interval during active gameplay
    - Add spawn timing management and control
    - Integrate with EnemyManager for enemy registration
    - _Requirements: 1.1_

  - [ ]* 9.2 Write property test for enemy spawning timing consistency
    - **Property 1: Enemy Spawning Timing Consistency**
    - **Validates: Requirements 1.1, 8.1, 8.2**

  - [x] 9.3 Add safe distance spawn validation
    - Implement 200-pixel safe distance from player
    - Add spawn location validation and retry logic
    - Handle maximum spawn attempts (10 attempts)
    - _Requirements: 1.3, 1.4_

  - [ ]* 9.4 Write property test for safe distance spawn enforcement
    - **Property 2: Safe Distance Spawn Enforcement**
    - **Validates: Requirements 1.3**

  - [ ]* 9.5 Write property test for graceful spawn failure handling
    - **Property 4: Graceful Spawn Failure Handling**
    - **Validates: Requirements 1.4**

  - [x] 9.6 Add spawn location distribution and collision avoidance
    - Implement random location selection across game map
    - Add collision detection with existing entities
    - Ensure no entity overlap during spawning
    - _Requirements: 1.2, 1.5_

  - [ ]* 9.7 Write property test for spawn location distribution and collision avoidance
    - **Property 3: Spawn Location Distribution and Collision Avoidance**
    - **Validates: Requirements 1.2, 1.5**

  - [x] 9.8 Add behavior assignment during spawning
    - Implement random initial behavior assignment
    - Initialize behavior switching timers for new enemies
    - Ensure proper behavior state initialization
    - _Requirements: 2.1, 2.2_

- [x] 10. Checkpoint - Ensure spawning system tests pass
  - Ensure all spawning tests pass, ask the user if questions arise.

- [x] 11. Integrate collision detection and combat system
  - [x] 11.1 Extend GameScreen collision handling for enemy-projectile interactions
    - Add collision detection between projectiles and enemies
    - Implement damage processing and projectile removal
    - Ensure single collision processing per projectile per frame
    - _Requirements: 5.1, 5.2, 5.3, 5.4_

  - [ ]* 11.2 Write property test for collision detection accuracy
    - **Property 16: Collision Detection Accuracy**
    - **Validates: Requirements 5.3, 6.6, 6.7**

  - [ ]* 11.3 Write property test for single collision processing per frame
    - **Property 17: Single Collision Processing Per Frame**
    - **Validates: Requirements 5.4**

  - [x] 11.4 Add player-enemy collision detection
    - Implement collision detection between player and enemies
    - Add immediate game over trigger on collision
    - Integrate with existing collision bounds system
    - _Requirements: 6.1, 6.6_

  - [x] 11.5 Add player-wall collision detection
    - Implement collision detection between player and walls
    - Add immediate game over trigger on wall collision
    - Use boundary detection for wall collision
    - _Requirements: 6.2, 6.7_

  - [x] 11.6 Ensure projectile non-interference
    - Verify projectiles pass through each other without collision
    - Test projectile-projectile interaction handling
    - _Requirements: 5.5_

  - [ ]* 11.7 Write property test for projectile non-interference
    - **Property 18: Projectile Non-Interference**
    - **Validates: Requirements 5.5**

- [x] 12. Implement game over state management
  - [x] 12.1 Add game over trigger system to GameScreen
    - Implement immediate game over on player collisions
    - Add game state transition to game over state
    - Ensure proper game over condition detection
    - _Requirements: 6.1, 6.2, 6.3_

  - [ ]* 12.2 Write property test for game over trigger conditions
    - **Property 19: Game Over Trigger Conditions**
    - **Validates: Requirements 6.1, 6.2, 6.3**

  - [x] 12.3 Add game over state management functionality
    - Implement game entity update pausing
    - Stop enemy spawning during game over
    - Clear all active projectiles from the game
    - _Requirements: 7.1, 7.2, 7.3_

  - [ ]* 12.4 Write property test for game over state management
    - **Property 20: Game Over State Management**
    - **Validates: Requirements 7.1, 7.2, 7.3**

  - [x] 12.5 Add game reset functionality
    - Implement game entity reset to initial state
    - Add restart option and normal gameplay resumption
    - Ensure proper state cleanup and reinitialization
    - _Requirements: 7.4, 7.5_

  - [ ]* 12.6 Write property test for game reset functionality
    - **Property 21: Game Reset Functionality**
    - **Validates: Requirements 7.4, 7.5**

- [x] 13. Integration and system wiring
  - [x] 13.1 Integrate EnemySpawner and EnemyManager with GameScreen
    - Add enemy system initialization to GameScreen
    - Integrate enemy spawner updates with main game loop
    - Connect enemy manager with collision detection system
    - _Requirements: 1.1, 8.1_

  - [x] 13.2 Wire health system with collision detection
    - Connect HealthSystem with GameScreen collision handling
    - Ensure proper damage processing flow
    - Integrate enemy destruction with game state
    - _Requirements: 4.3, 4.4, 5.1, 5.2_

  - [x] 13.3 Connect behavior system with enemy lifecycle
    - Integrate behavior switching with enemy updates
    - Ensure proper behavior initialization during spawning
    - Connect steering behaviors with PlayerActor movement
    - _Requirements: 2.1, 2.2, 3.1, 3.2, 3.3, 3.4, 3.5, 3.6_

  - [x] 13.4 Finalize game over integration
    - Connect game over triggers with enemy collision detection
    - Integrate game over state with enemy system pause/resume
    - Ensure proper cleanup during game reset
    - _Requirements: 6.1, 6.2, 7.1, 7.2, 7.3, 7.4, 7.5_

- [x] 14. Final system testing and validation
  - [ ]* 14.1 Write comprehensive integration tests
    - Test complete enemy lifecycle from spawn to destruction
    - Validate system performance with maximum enemy count
    - Test game over and restart functionality
    - _Requirements: All requirements_

  - [x] 14.2 Performance optimization and validation
    - Ensure 60 FPS performance with 10 active enemies
    - Optimize health bar rendering and behavior calculations
    - Validate memory management and cleanup
    - _Requirements: 8.1, 8.3, 8.5_

- [x] 15. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 16. Implement deferred physics body cleanup system
  - [x] 16.1 Add physics body cleanup queue to GameScreen
    - Create enemiesToDisposePhysics array to queue enemies for cleanup
    - Add processDeferredPhysicsCleanup() method to process cleanup after physics step
    - Add queueEnemyForPhysicsCleanup() method for safe queuing
    - _Requirements: 8.4_

  - [x] 16.2 Update EnemyManager to use deferred cleanup
    - Add GameScreen reference for cleanup coordination
    - Modify enemy removal to queue enemies for deferred physics cleanup
    - Add fallback safety mechanism if GameScreen reference unavailable
    - _Requirements: 8.4_

  - [x] 16.3 Update PlayerActor for safe cleanup
    - Modify remove() method to defer physics body cleanup
    - Add forceDispose() method for safe manual cleanup
    - Maintain collision crash prevention while enabling proper cleanup
    - _Requirements: 8.4_

  - [x] 16.4 Integrate cleanup with game state management
    - Clear cleanup queue during game over processing
    - Clear cleanup queue during game reset
    - Process remaining cleanup during screen disposal
    - _Requirements: 8.4_

- [x] 17. Implement behavior label display system
  - [x] 17.1 Create EnemyBehaviorLabel class
    - Implement behavior label rendering with ShapeRenderer
    - Add color-coded labels for each behavior type
    - Implement proper positioning below enemy (35 pixels)
    - Add performance-optimized rendering with graceful error handling
    - _Requirements: 9.1, 9.2, 9.3_

  - [x] 17.2 Integrate behavior label into PlayerActor
    - Add behaviorLabel field to enemy-specific properties
    - Integrate behavior label rendering in act() method
    - Add getter method for behavior label access
    - _Requirements: 9.1, 9.4_

  - [x] 17.3 Integrate behavior label into GameScreen
    - Initialize behavior label static resources in constructor
    - Add cleanup in dispose method
    - Connect with existing enemy system
    - _Requirements: 9.1, 9.5_

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Property tests validate universal correctness properties from the design document
- The system integrates with existing LibGDX architecture and PlayerActor class
- Checkpoints ensure incremental validation and early error detection
- All collision detection leverages existing GameScreen infrastructure
- Health system provides visual feedback through health bars above enemies
- Behavior system creates engaging and varied enemy AI patterns