# Implementation Plan: Player Controls and Shooting

## Overview

This implementation adds a player-controlled ship with Asteroids-style physics controls and shooting mechanics to the existing libGDX game. The game window will be enlarged to 1600×1200 pixels (non-resizable) with a 1:1 pixel-to-unit ratio, the existing PlayerActor enemy will be resized by 50x, and a new PlayerShip class will be created with arrow key controls and spacebar shooting. Wall collisions will trigger game over and return to the main menu.

## Tasks

- [x] 1. Update game window configuration and wall boundaries
  - Modify GameScreen constructor to set window size to 1600×1200 pixels and make it non-resizable
  - Set VIRTUAL_WIDTH to 1600 and VIRTUAL_HEIGHT to 1200 for 1:1 pixel-to-unit ratio
  - Update wall boundary positions with bordersize offset (1 unit) from edges: bottom at y=1, top at y=1199, left at x=1, right at x=1599
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

- [x] 2. Resize PlayerActor enemy entity
  - Modify PlayerActor instantiation in GameScreen to scale texture by 50x
  - Update PlayerActor physics body dimensions to match new visual size
  - Verify autonomous steering behavior still functions correctly
  - _Requirements: 2.1, 2.2, 2.3_

- [ ]* 2.1 Write property test for PlayerActor physics body size
  - **Property 1: Physics body matches visual size**
  - **Validates: Requirements 2.2**

- [x] 3. Add new entity categories for player ship and projectiles
  - Add PLAYER_SHIP category with value (short) 0x5 to EntityCategory enum in Configuration
  - Add PROJECTILE category with value (short) 0x6 to EntityCategory enum in Configuration
  - _Requirements: 3.4, 5.4_

- [x] 4. Create PlayerShip class with physics and rendering
  - [x] 4.1 Implement PlayerShip class extending AnimatedPhysicsActor
    - Create PlayerShip.java with constructor accepting animationName, world, x, y, frameCols, frameRows
    - Scale PlayerShip texture by 50x to match PlayerActor size
    - Configure Box2D body as DynamicBody with collision category PLAYER_SHIP
    - Set collision mask to BOUNDARY | PROJECTILE | ENEMY_SHIP
    - Set linear damping to 0.05f and angular damping to 0.0f (adjusted for responsive movement with instant rotation stop)
    - Add constants: ROTATION_SPEED = 5.0f, THRUST_FORCE = 15000.0f, MAX_LINEAR_VELOCITY = 300.0f, FIRE_RATE_COOLDOWN = 0.2f
    - Add isAlive boolean field and lastFireTime float field for fire rate limiting
    - _Requirements: 3.1, 3.3, 3.4_
  
  - [x] 4.2 Implement PlayerShip control methods
    - Implement applyRotation(float direction) to apply angular force (1 for CW, -1 for CCW - directions corrected)
    - Clamp angular velocity to MAX_ANGULAR_VELOCITY in applyRotation
    - Implement applyThrust(float direction) to apply linear force in facing direction (1 forward, -1 reverse)
    - Implement getFacingDirection() to return Vector2 based on current angle
    - Implement destroy() to set isAlive to false
    - Implement isAlive() getter
    - Implement shoot() with ShootCallback interface and fire rate limiting (0.2s cooldown)
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_
  
  - [ ]* 4.3 Write property tests for PlayerShip rotation and thrust
    - **Property 2: Arrow key rotation direction**
    - **Validates: Requirements 4.1, 4.2**
    - **Property 3: Thrust direction alignment**
    - **Validates: Requirements 4.3, 4.4**
    - **Property 4: Continuous rotation on key hold**
    - **Validates: Requirements 4.6**

- [x] 5. Create Projectile class with lifecycle management
  - [x] 5.1 Implement Projectile class extending AnimatedPhysicsActor
    - Create Projectile.java with constructor accepting animationName, world, frameCols, frameRows
    - Configure Box2D body as DynamicBody with collision category PROJECTILE
    - Set collision mask to BOUNDARY | ENEMY_SHIP
    - Set body as bullet (continuous collision detection) and non-sensor
    - Add constants: PROJECTILE_SPEED = 5000.0f, PROJECTILE_LIFETIME = 15.0f
    - Add fields: timeAlive (float), isActive (boolean), direction (Vector2), currentPosition (Vector2), markedForDeactivation (boolean)
    - Set linear and angular damping to 0.0f (zero damping for consistent movement)
    - _Requirements: 5.4_
  
  - [x] 5.2 Implement Projectile lifecycle methods
    - Implement fire(Vector2 position, Vector2 direction, Vector2 shipVelocity) to activate and set hybrid movement
    - Use hybrid approach: 5000.0f pixels/second visual speed with 100.0f Box2D units/second physics velocity
    - Implement direct position updates to bypass Box2D velocity limitations while maintaining collision detection
    - Implement deactivate() to set isActive to false and stop physics body
    - Implement markForDeactivation() for safe collision handling
    - Override act(float delta) to track lifetime and deactivate after 15.0f seconds
    - Implement isActive() and isMarkedForDeactivation() getters
    - _Requirements: 5.2, 5.3, 5.5_
  
  - [ ]* 5.3 Write property tests for Projectile behavior
    - **Property 5: Projectile spawns at ship position**
    - **Validates: Requirements 5.1**
    - **Property 6: Projectile direction matches ship facing**
    - **Validates: Requirements 5.2**
    - **Property 7: Projectile faster than ship**
    - **Validates: Requirements 5.3**
    - **Property 8: Projectile removal on boundary exit**
    - **Validates: Requirements 5.5**

- [x] 6. Implement ProjectilePool for object pooling
  - Create ProjectilePool.java with POOL_SIZE constant of 20
  - Implement constructor to pre-create pool of Projectile instances
  - Implement obtain(Vector2 position, Vector2 direction, Vector2 shipVelocity) to get/activate projectile from pool
  - Implement free(Projectile projectile) to deactivate and return projectile to pool
  - Implement updateAll(float delta) to call act() on all active projectiles
  - Implement disposeAll() to dispose all projectiles in pool
  - _Requirements: 5.1, 5.5_

- [x] 7. Create PlayerShipInputHandler for keyboard controls
  - [x] 7.1 Implement PlayerShipInputHandler class extending InputAdapter
    - Create PlayerShipInputHandler.java with constructor accepting PlayerShip and GameScreen
    - Add boolean fields for key state: leftPressed, rightPressed, upPressed, downPressed
    - Override keyDown(int keycode) to set key state flags for arrow keys and handle spacebar
    - Override keyUp(int keycode) to clear key state flags for arrow keys
    - Map Input.Keys.LEFT to leftPressed (clockwise rotation - direction corrected)
    - Map Input.Keys.RIGHT to rightPressed (counter-clockwise rotation - direction corrected)
    - Map Input.Keys.UP to upPressed, Input.Keys.DOWN to downPressed
    - Map Input.Keys.SPACE to trigger shoot() on keyDown only with fire rate limiting
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 5.1_
  
  - [x] 7.2 Implement continuous input update method
    - Implement update() method to apply forces based on current key state
    - Call playerShip.applyRotation(1) if leftPressed (clockwise), applyRotation(-1) if rightPressed (counter-clockwise)
    - Call playerShip.applyThrust(1) if upPressed, applyThrust(-1) if downPressed
    - _Requirements: 4.6_

- [x] 8. Integrate PlayerShip and input handling into GameScreen
  - [x] 8.1 Add PlayerShip fields and initialization to GameScreen
    - Add fields: PlayerShip playerShip, ProjectilePool projectilePool, PlayerShipInputHandler playerShipInputHandler, boolean isGameOver
    - In constructor, instantiate PlayerShip at position (800, 600) with 50x scale and appropriate texture
    - Create ProjectilePool instance with world and gameStage
    - Create PlayerShipInputHandler instance with playerShip and this
    - Add playerShipInputHandler to InputMultiplexer
    - Add playerShip to gameStage
    - _Requirements: 3.1, 3.3, 3.4, 3.5_
  
  - [x] 8.2 Update GameScreen render loop for player ship
    - In render(float delta), call playerShipInputHandler.update() before world.step()
    - In render(float delta), call projectilePool.updateAll(delta) after world.step()
    - In dispose(), call projectilePool.disposeAll()
    - _Requirements: 3.5, 5.5_
  
  - [x] 8.3 Implement shooting integration methods
    - Implement spawnProjectile(Vector2 position, Vector2 direction, Vector2 shipVelocity) method
    - Call projectilePool.obtain() and add projectile to gameStage if not null
    - Modify PlayerShip.shoot() to use ShootCallback interface and spawn projectiles at ship tip (30.0f * PPM offset)
    - _Requirements: 5.1_

- [x] 9. Implement collision detection and game over logic
  - [x] 9.1 Enhance ContactListener for new collision types
    - In GameScreen's ContactListener.beginContact(), add helper method isCollision(Fixture a, Fixture b, short cat1, short cat2)
    - Add collision check for PLAYER_SHIP and BOUNDARY categories to trigger game over
    - Add collision check for PROJECTILE and BOUNDARY categories to deactivate projectile
    - Add collision check for PROJECTILE and ENEMY_SHIP categories to deactivate projectile
    - Implement getProjectileFromFixtures() helper to extract Projectile from fixture user data
    - _Requirements: 5.5, 6.1, 6.2, 7.1_
  
  - [x] 9.2 Implement game over transition logic
    - Implement triggerGameOver() method in GameScreen to set isGameOver flag
    - Schedule screen transition to MainMenuScreen within 1 second using Timer or frame counter
    - Ensure dispose() is called before screen transition
    - _Requirements: 7.1, 7.2, 7.3, 7.4_
  
  - [ ]* 9.3 Write property tests for collision and game over
    - **Property 9: Wall collision blocks movement**
    - **Validates: Requirements 6.1**
    - **Property 10: Wall collision triggers game over**
    - **Validates: Requirements 7.1, 7.2**
    - **Property 11: Game over transition timing**
    - **Validates: Requirements 7.3**
    - **Property 12: Resource disposal on screen transition**
    - **Validates: Requirements 7.4**

- [x] 10. Add error handling and edge cases
  - Implement fire rate limiting in PlayerShip with cooldown period (0.2 seconds)
  - Add velocity clamping in PlayerShip.act() to prevent excessive speed (MAX_LINEAR_VELOCITY = 100.0f)
  - Handle projectile pool exhaustion gracefully (return null or reuse oldest)
  - Add null checks for projectile body access after deactivation
  - Prevent multiple game over triggers with isGameOver flag check
  - Implement markForDeactivation() system for safe projectile collision handling
  - _Requirements: 4.5, 5.3, 7.1_

- [x] 11. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.
  - Verify both PlayerActor and PlayerShip are scaled to 50x and appear appropriately sized

## Notes

- Tasks marked with `*` are optional property-based tests and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Property tests use jqwik library with minimum 100 iterations per property
- Property test tags follow format: `@Tag("Feature: player-controls-and-shooting, Property N: [property text]")`
- All collision detection uses Box2D ContactListener for physics-driven events
- Projectile pooling prevents frequent allocation/deallocation during gameplay
- Input handling uses libGDX InputProcessor pattern integrated with existing InputMultiplexer

## Implementation Changes Made During Development

The following key values were adjusted from the original design to achieve the desired gameplay feel:

### Force Values Dramatically Increased
- **THRUST_FORCE**: Originally 5.0f → **15000.0f** (3000x increase) for rapid acceleration
- **ROTATION_SPEED**: Changed from angular force to **constant rotation at 5.0f radians/second** for instant, precise control
- **MAX_LINEAR_VELOCITY**: Originally 100.0f → **300.0f** for fast movement across large game field
- These much higher values were necessary to provide responsive controls suitable for the large game field

### Projectile System: Hybrid Movement Approach
- **PROJECTILE_SPEED**: **5000.0f pixels/second** for smooth visual movement
- **Physics velocity**: **100.0f Box2D units/second** for reliable collision detection
- **PROJECTILE_LIFETIME**: Originally 3.0f → **15.0f seconds** to cross large game field
- **Movement method**: Direct position updates bypass Box2D velocity limitations

### Control Direction Corrections
- **Left arrow**: Now applies **clockwise** rotation (was counter-clockwise)
- **Right arrow**: Now applies **counter-clockwise** rotation (was clockwise)
- These changes provide more intuitive control feel

### Physics Configuration Optimizations
- **Projectile damping**: Set to **0.0f** (zero damping) for consistent movement
- **PlayerShip linear damping**: Reduced to **0.1f** (was 0.5f) for responsive movement
- **PlayerShip angular damping**: Reduced to **0.5f** (was 2.0f) for responsive rotation

### Additional Features Added
- **Fire rate limiting**: 0.2 second cooldown between shots
- **Projectile spawn position**: Ship tip with 30.0f * PPM pixel offset from center
- **Velocity clamping**: MAX_LINEAR_VELOCITY = 100.0f to prevent excessive speed
- **Safe collision handling**: markForDeactivation() system for projectiles
