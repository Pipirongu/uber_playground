# Requirements Document

## Introduction

This feature enhances the libGDX game by introducing a new player-controlled ship entity with Asteroids-style controls and shooting mechanics. The game field will be enlarged, the existing PlayerActor (enemy with steering behavior) will be resized, and a new PlayerShip entity will be created for player control. The PlayerShip will have rotation controls with arrow keys, shooting capability with the spacebar, and wall collision detection that triggers game over.

## Glossary

- **Game_Field**: The visible play area where game entities exist and interact
- **PlayerShip**: The new player-controlled entity with Asteroids-style rotation and shooting mechanics
- **PlayerActor**: The existing enemy entity with autonomous steering behavior (not player-controlled)
- **Game_Window**: The application window displaying the game
- **Wall**: Static boundary entities at the edges of the Game_Field
- **Rotation_Force**: Angular force applied to PlayerShip similar to Asteroids game mechanics
- **Projectile**: A physics-based entity fired by the PlayerShip
- **Game_Screen**: The screen displaying active gameplay
- **Main_Menu_Screen**: The initial screen with the play button
- **Collision**: Physical contact between two game entities detected by Box2D physics

## Requirements

### Requirement 1: Enlarge Game Field and Window

**User Story:** As a player, I want a larger game field and window, so that I have more space to maneuver and play.

#### Acceptance Criteria

1. THE Game_Window SHALL be sized at 1600 pixels width by 1200 pixels height
2. THE Game_Window SHALL NOT be resizable by the user (fixed size window)
3. THE Game_Screen SHALL set virtual width to 1600 units and virtual height to 1200 units (1:1 pixel-to-unit ratio)
4. THE Game_Screen SHALL update wall boundaries with bordersize offset (1 unit) from edges at coordinates: bottom wall at y=1, top wall at y=1199, left wall at x=1, right wall at x=1599
5. THE wall boundaries SHALL be visible within the Game_Field due to the bordersize offset

### Requirement 2: Resize PlayerActor Enemy

**User Story:** As a player, I want the enemy to be larger and faster, so that it is more visible, easier to track, and provides dynamic gameplay.

#### Acceptance Criteria

1. THE PlayerActor SHALL increase its visual size by a factor of 50x compared to the current size
2. THE PlayerActor SHALL update its physics body to match the new visual size
3. THE PlayerActor SHALL maintain its autonomous steering behavior after resizing
4. THE PlayerActor SHALL have significantly increased movement speed (maxSpeed = 200f * scale = 10,000 units for 50x scale, which is 25x faster than the original base speed of 8f)
5. THE PlayerActor SHALL have dramatically increased steering force capability (maxForce = 20.0f * massScale = 50,000 units for 50x scale, which is 40x stronger than the original base force of 0.5f)
6. THE PlayerActor SHALL have very low physics damping (linearDamping = 0.01f, angularDamping = 0.01f) for highly responsive movement
7. THE PlayerActor SHALL have aggressive wander behavior with rapid direction changes (wanderOrientation change rate = 1.5f)
8. THE PlayerActor SHALL use minimal velocity smoothing (Smoother value = 1) for immediate response to direction changes
9. THE PlayerActor SHALL have a corrected Seek method that properly scales desired velocity by maxSpeed and limits steering force by maxForce to ensure effective movement and turning

### Requirement 3: Create PlayerShip Entity

**User Story:** As a player, I want a new player-controlled ship entity, so that I can control my own character distinct from the enemy.

#### Acceptance Criteria

1. THE Game_Screen SHALL create a PlayerShip entity as a new physics-based actor
2. THE PlayerShip SHALL have a distinct visual appearance from the PlayerActor
3. THE PlayerShip SHALL be sized similarly to the PlayerActor (50x scale)
4. THE PlayerShip SHALL spawn at the center of the Game_Field at position (800, 600)
5. THE PlayerShip SHALL have a physics body with collision detection enabled
6. THE PlayerShip SHALL be rendered on the Game_Screen each frame

### Requirement 4: Arrow Key Controls for PlayerShip

**User Story:** As a player, I want to control the PlayerShip with arrow keys, so that I can navigate the game field.

#### Acceptance Criteria

1. WHEN the left arrow key is pressed, THE PlayerShip SHALL apply constant clockwise rotation at 5.0f radians/second
2. WHEN the right arrow key is pressed, THE PlayerShip SHALL apply constant counter-clockwise rotation at 5.0f radians/second
3. WHEN the up arrow key is pressed, THE PlayerShip SHALL apply forward thrust of 15000.0f in the direction it is facing with rapid acceleration
4. WHEN the down arrow key is pressed, THE PlayerShip SHALL apply reverse thrust of 15000.0f opposite to the direction it is facing with rapid acceleration
5. THE PlayerShip SHALL rotate at constant speed without momentum-based physics (direct angular velocity setting)
6. THE PlayerShip SHALL continue rotating while arrow keys are held down
7. THE PlayerShip SHALL have linear damping of 0.05f and angular damping of 0.0f for responsive movement with instant rotation stop
8. THE PlayerShip SHALL have maximum linear velocity of 300.0f for fast movement across the large game field

### Requirement 5: Spacebar Shooting from PlayerShip

**User Story:** As a player, I want to shoot projectiles with the spacebar, so that I can interact with game elements.

#### Acceptance Criteria

1. WHEN the spacebar is pressed, THE PlayerShip SHALL spawn a Projectile at the ship's tip position (30.0f * PPM pixel offset from center)
2. THE Projectile SHALL travel in the direction the PlayerShip is facing at the time of firing
3. THE Projectile SHALL have a visual speed of 5000.0f pixels/second and physics velocity of 100.0f Box2D units/second (hybrid approach)
4. THE Projectile SHALL be a physics-based entity with collision detection enabled and zero damping
5. THE Projectile SHALL be removed from the game after 15.0f seconds or when it collides with boundaries
6. THE PlayerShip SHALL have a fire rate limit of 0.2 seconds cooldown between shots

### Requirement 6: Wall Collision Prevention for PlayerShip

**User Story:** As a player, I want to be prevented from passing through walls, so that the game boundaries are enforced.

#### Acceptance Criteria

1. WHEN the PlayerShip contacts a Wall, THE PlayerShip SHALL be blocked from passing through
2. THE PlayerShip physics body SHALL have collision enabled with Wall entities
3. THE Wall entities SHALL be non-sensor static bodies that physically block movement

### Requirement 7: Game Over on PlayerShip Wall Collision

**User Story:** As a player, I want the game to end when my ship hits a wall, so that there are consequences for poor navigation.

#### Acceptance Criteria

1. WHEN the PlayerShip collides with a Wall, THE Game_Screen SHALL trigger a game over state
2. WHEN game over is triggered, THE Game_Screen SHALL transition to the Main_Menu_Screen
3. THE transition SHALL occur within 1 second of the Collision
4. THE Game_Screen SHALL dispose of game resources before transitioning to Main_Menu_Screen
