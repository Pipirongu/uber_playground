# Requirements Document

## Introduction

This feature enhances the projectile system in the LibGDX game to improve visual consistency and performance. The enhancement changes projectile graphics to use "spawn_unit" graphics with appropriate scaling, and increases projectile speed for better gameplay experience.

## Glossary

- **Projectile_System**: The game component responsible for creating, managing, and rendering projectiles fired by the player
- **ControlPointActor**: An animated physics actor that serves as a navigation target, using "spawn_unit" graphics with configurable scaling
- **ProjectilePool**: Object pool implementation that manages projectile instances for performance optimization
- **Graphics_Asset**: PNG texture files used for rendering game entities, located in android/assets/actors/
- **Frame_Configuration**: Parameters defining how sprite sheets are divided (frameCols, frameRows)
- **Scaling_Factor**: Multiplier applied to texture dimensions for consistent visual sizing across entities

## Requirements

### Requirement 1: Projectile Graphics Consistency

**User Story:** As a player, I want projectiles to have consistent visual appearance with other game entities, so that the game has a cohesive visual style.

#### Acceptance Criteria

1. THE Projectile_System SHALL use "spawn_unit" graphics instead of "agent" graphics
2. THE Projectile_System SHALL use the same frame configuration as ControlPointActor (1 column, 1 row)
3. WHEN a projectile is created, THE ProjectilePool SHALL instantiate it with "spawn_unit" animation name
4. THE Projectile_System SHALL maintain all existing collision detection and physics properties

### Requirement 2: Projectile Scaling and Collision Consistency

**User Story:** As a player, I want projectiles to have appropriate size and collision boundaries relative to other game entities, so that they are visually balanced and collision detection feels accurate.

#### Acceptance Criteria

1. THE Projectile_System SHALL apply large scaling factor (20.0f) to make spawn_unit graphics highly visible
2. THE Projectile_System SHALL support configurable scaling through constructor parameters
3. WHEN projectiles are rendered, THE Graphics_System SHALL apply scaling to both texture dimensions and physics body size
4. THE Projectile_System SHALL update collision boundaries to match the "spawn_unit" graphics dimensions instead of "agent" graphics
5. THE Projectile_System SHALL maintain proportional collision boundaries that accurately represent the visual appearance with large scaling

### Requirement 3: Enhanced Projectile Speed

**User Story:** As a player, I want projectiles to move faster, so that combat feels more responsive and engaging.

#### Acceptance Criteria

1. THE Projectile_System SHALL increase projectile speed from 5000.0f to at least 7500.0f pixels per second
2. WHEN a projectile is fired, THE Projectile_System SHALL maintain smooth visual movement at the increased speed
3. THE Projectile_System SHALL preserve existing collision detection accuracy at higher speeds
4. THE Projectile_System SHALL maintain the same lifetime duration (15.0f seconds) regardless of speed changes

### Requirement 4: Backward Compatibility

**User Story:** As a developer, I want the enhanced projectile system to maintain existing functionality, so that no game features are broken by the changes.

#### Acceptance Criteria

1. THE Projectile_System SHALL preserve all existing public method signatures
2. THE ProjectilePool SHALL continue to support the same initialization and management interface
3. THE Projectile_System SHALL maintain existing collision filtering and physics properties
4. WHEN projectiles interact with boundaries and enemies, THE Collision_System SHALL function identically to the current implementation