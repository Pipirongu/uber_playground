# HealthSystem Integration Guide

## Overview

The HealthSystem class provides centralized health management for enemies in the Enhanced Enemy System. It handles enemy health registration, damage processing from projectile collisions, and enemy destruction when health reaches zero.

## Key Features

- **Centralized Health Management**: All enemy health is managed through a single system
- **Damage Processing**: Handles both projectile damage (25 HP) and custom damage amounts
- **Enemy Destruction**: Automatically marks enemies for removal when health reaches zero
- **Null Safety**: Gracefully handles null inputs and unregistered enemies
- **Auto-Registration**: Automatically registers enemies when they take damage
- **Batch Processing**: Processes enemy removals in batches for efficient cleanup

## Integration Points

### 1. GameScreen Collision Detection

Integrate with the existing ContactListener in GameScreen:

```java
// In GameScreen.beginContact()
if (isCollision(fixtureA, fixtureB, Configuration.EntityCategory.PROJECTILE.getValue(), 
                Configuration.EntityCategory.ENEMY_SHIP.getValue())) {
    
    Projectile proj = getProjectileFromFixtures(fixtureA, fixtureB);
    PlayerActor enemy = getEnemyFromFixtures(fixtureA, fixtureB);
    
    if (proj != null && enemy != null) {
        // Process damage through HealthSystem
        boolean stillAlive = healthSystem.takeDamage(enemy, proj, Configuration.ElapsedTime);
        
        // Deactivate projectile
        proj.markForDeactivation();
        
        // Enemy destruction is handled automatically by HealthSystem
    }
}
```

### 2. EnemyManager Integration

Process enemy removals in the EnemyManager update cycle:

```java
// In EnemyManager.update()
public void update(float delta) {
    // Update all enemies
    for (PlayerActor enemy : activeEnemies) {
        enemy.act(delta);
    }
    
    // Process health system removals
    Array<PlayerActor> enemiesToRemove = healthSystem.processRemovals();
    for (PlayerActor enemy : enemiesToRemove) {
        removeEnemyFromWorld(enemy);
        activeEnemies.removeValue(enemy, true);
    }
}
```

### 3. Enemy Spawning Integration

Register new enemies with the HealthSystem during spawning:

```java
// In EnemySpawner.spawnEnemy()
PlayerActor newEnemy = new PlayerActor("enemy", world, x, y, 1, 1);
// ... configure enemy properties ...

// Register with health system
healthSystem.initializeHealth(newEnemy); // Uses default 100 HP
// OR
healthSystem.initializeHealth(newEnemy, customHealthValue);

enemyManager.addEnemy(newEnemy);
```

### 4. Health Bar Rendering Integration

Use health percentages for visual health bars:

```java
// In PlayerActor.act() or EnemyHealthBar.render()
if (isEnemy) {
    float healthPercentage = healthSystem.getHealthPercentage(this);
    if (healthPercentage > 0) {
        healthBar.render(shapeRenderer, getPosition(), healthPercentage);
    }
}
```

### 5. Game State Management

Handle game over and reset scenarios:

```java
// In GameScreen.triggerGameOver()
public void triggerGameOver() {
    isGameOver = true;
    // Clear all enemies from health system
    healthSystem.clear();
    // ... other game over logic ...
}

// In GameScreen.reset()
public void reset() {
    healthSystem.clear();
    // ... other reset logic ...
}
```

## Usage Examples

### Basic Enemy Health Management

```java
// Initialize health system
HealthSystem healthSystem = new HealthSystem();

// Register enemy with default health (100 HP)
healthSystem.initializeHealth(enemy);

// Process projectile damage (25 HP)
boolean stillAlive = healthSystem.takeDamage(enemy, projectile, gameTime);

// Check if enemy is still alive
if (healthSystem.isAlive(enemy)) {
    // Enemy is still active
    float healthPercent = healthSystem.getHealthPercentage(enemy);
    // Update health bar display
}

// Process removals each frame
Array<PlayerActor> deadEnemies = healthSystem.processRemovals();
for (PlayerActor deadEnemy : deadEnemies) {
    // Remove from game world
}
```

### Custom Damage Processing

```java
// Apply custom damage (e.g., from special weapons)
boolean stillAlive = healthSystem.takeDamage(enemy, 50, gameTime);

// Apply damage without projectile reference
boolean stillAlive = healthSystem.takeDamage(enemy, damageAmount, gameTime);
```

### Health System Queries

```java
// Get current health information
int currentHealth = healthSystem.getCurrentHealth(enemy);
int maxHealth = healthSystem.getMaxHealth(enemy);
float healthPercentage = healthSystem.getHealthPercentage(enemy);
boolean isAlive = healthSystem.isAlive(enemy);

// Get system statistics
int totalEnemies = healthSystem.getRegisteredEnemyCount();
Array<PlayerActor> allEnemies = healthSystem.getRegisteredEnemies();
```

## Configuration

The HealthSystem uses constants from SpawnConfiguration:

- `SpawnConfiguration.ENEMY_HEALTH` (100) - Default enemy health
- `SpawnConfiguration.PROJECTILE_DAMAGE` (25) - Damage per projectile hit

## Error Handling

The HealthSystem includes comprehensive error handling:

- **Null Safety**: All methods handle null inputs gracefully
- **Auto-Registration**: Unregistered enemies are automatically initialized when damaged
- **Damage Validation**: Negative damage is rejected
- **Health Clamping**: Damage exceeding current health is properly clamped to zero

## Performance Considerations

- **Efficient Lookups**: Uses LibGDX ObjectMap for O(1) enemy health lookups
- **Batch Processing**: Processes all removals in a single operation per frame
- **Memory Management**: Automatically cleans up destroyed enemies
- **Minimal Allocations**: Reuses arrays and avoids unnecessary object creation

## Testing

The HealthSystem includes comprehensive unit tests:

- `HealthSystemTest`: Core functionality tests
- `HealthSystemIntegrationTest`: Integration pattern demonstrations

Run tests with: `./gradlew :core:test --tests "com.plv.uberplayground.enemies.HealthSystem*"`