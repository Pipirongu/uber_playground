package com.plv.uberplayground.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.plv.uberplayground.UberPlayground;
import com.plv.uberplayground.actors.AnimatedActor;
import com.plv.uberplayground.actors.ControlPointActor;
import com.plv.uberplayground.actors.PlayerActor;
import com.plv.uberplayground.actors.PlayerShip;
import com.plv.uberplayground.actors.Projectile;
import com.plv.uberplayground.actors.ProjectilePool;
import com.plv.uberplayground.actors.Wall;
import com.plv.uberplayground.config.Configuration;
import com.plv.uberplayground.enemies.EnemyManager;
import com.plv.uberplayground.enemies.EnemySpawner;
import com.plv.uberplayground.enemies.HealthSystem;
import com.plv.uberplayground.inputhandlers.GameStageInputHandler;
import com.plv.uberplayground.inputhandlers.PlayerShipInputHandler;
import com.plv.uberplayground.screens.MainMenuScreen;

public class GameScreen implements Screen {
	private World world;
	private Box2DDebugRenderer debugRenderer;

	private static final int VIRTUAL_WIDTH = 1600;
	private static final int VIRTUAL_HEIGHT = 1200;

	private OrthographicCamera mainCamera;
	// group for agent and its particle emitter, physics applies to group, will
	// rotate emitter as well

	// UI menu in-game - different VIRTUAL_WIDTH/VIRTUAL_HEIGHT
	private Stage hudStage;
	// stage for actors, and also playfield to spawn 'targets'
	private Stage gameStage;
	// Note: Removed player field as we only use playerShip now
	public Array<Wall>Walls = new Array<Wall>();
	
	// PlayerShip integration fields
	private PlayerShip playerShip;
	private ProjectilePool projectilePool;
	private PlayerShipInputHandler playerShipInputHandler;
	private boolean isGameOver;
	private float gameOverTimer;
	private boolean gameOverTriggered = false;
	
	// Enemy system integration fields
	private HealthSystem healthSystem;
	private EnemyManager enemyManager;
	private EnemySpawner enemySpawner;
	
	// Deferred physics body cleanup to prevent Box2D crashes
	private Array<PlayerActor> enemiesToDisposePhysics = new Array<>();
	
	private final UberPlayground app;

	public GameScreen(final UberPlayground app) {
		this.app = app;
		this.mainCamera = new OrthographicCamera();
		this.hudStage = new Stage(new FillViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, this.mainCamera));
		this.gameStage = new Stage(new FillViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, this.mainCamera));

		this.world = new World(new Vector2(), true);
		this.world.setContactListener(new ContactListener() {
			@Override
			public void beginContact(Contact contact) {
				try {
					Fixture fixtureA = contact.getFixtureA();
					Fixture fixtureB = contact.getFixtureB();
					
					// Skip collision processing if game is already over or triggered
					if (isGameOver || gameOverTriggered) {
						return;
					}
					
					// Check for Player-Enemy collision (trigger game over)
					if (isCollision(fixtureA, fixtureB, Configuration.EntityCategory.PLAYER_SHIP.getValue(), Configuration.EntityCategory.ENEMY_SHIP.getValue())) {
						triggerGameOver();
						return; // Exit early to prevent further collision processing
					}
					
					// Check for PlayerShip-Wall collision (triggers game over)
					if (isCollision(fixtureA, fixtureB, Configuration.EntityCategory.PLAYER_SHIP.getValue(), Configuration.EntityCategory.BOUNDARY.getValue())) {
						triggerGameOver();
						return; // Exit early to prevent further collision processing
					}
					
					// Only process other collisions if game is still active
					if (!gameOverTriggered) {
						// Check for Projectile-Wall collision (deactivate projectile)
						if (isCollision(fixtureA, fixtureB, Configuration.EntityCategory.PROJECTILE.getValue(), Configuration.EntityCategory.BOUNDARY.getValue())) {
							Projectile proj = getProjectileFromFixtures(fixtureA, fixtureB);
							if (proj != null) {
								proj.markForDeactivation();
							}
						}
						
						// Check for Projectile-Enemy collision (damage enemy and deactivate projectile)
						if (isCollision(fixtureA, fixtureB, Configuration.EntityCategory.PROJECTILE.getValue(), Configuration.EntityCategory.ENEMY_SHIP.getValue())) {
							Projectile proj = getProjectileFromFixtures(fixtureA, fixtureB);
							PlayerActor enemy = getEnemyFromFixtures(fixtureA, fixtureB);
							
							if (proj != null && enemy != null && enemy.isEnemy()) {
								// Process damage through health system
								boolean enemyStillAlive = healthSystem.takeDamage(enemy, proj, Configuration.ElapsedTime);
								
								// Debug logging for damage processing
								float healthPercentage = healthSystem.getHealthPercentage(enemy);
								int currentHealth = healthSystem.getCurrentHealth(enemy);
								Gdx.app.log("Damage", String.format("Enemy hit! Health: %d (%.1f%%), Alive: %s", 
									currentHealth, healthPercentage * 100, enemyStillAlive));
								
								// Deactivate projectile after damage processing
								proj.markForDeactivation();
								
								// If enemy was destroyed, it will be handled by EnemyManager in the next update
							}
						}
						
						// Existing collision logic for PlayerActor-Boundary
						if ((contact.getFixtureA().getFilterData().categoryBits == Configuration.EntityCategory.BOUNDARY.getValue() &&
								contact.getFixtureB().getFilterData().categoryBits == Configuration.EntityCategory.PLAYER.getValue()) ||

								(contact.getFixtureA().getFilterData().categoryBits == Configuration.EntityCategory.PLAYER.getValue() &&
								contact.getFixtureB().getFilterData().categoryBits == Configuration.EntityCategory.BOUNDARY.getValue()))
						{
							Gdx.app.log("Hit Detection", "Collided");
						}
					}
				} catch (Exception e) {
					Gdx.app.error("Collision", "Error during collision processing: " + e.getMessage());
					// Don't crash the game, just trigger game over
					if (!isGameOver && !gameOverTriggered) {
						gameOverTriggered = true;
					}
				}
			}

			@Override
			public void endContact(Contact contact) {
			}

			@Override
			public void preSolve(Contact contact, Manifold oldManifold) {
			}

			@Override
			public void postSolve(Contact contact, ContactImpulse impulse) {
			}
		});

		float bordersize = 1f;
		//Bottom - at y=1
		this.CreateBoundary(0f, bordersize, this.VIRTUAL_WIDTH, bordersize, false);
		//Top - at y=1199
		this.CreateBoundary(0f, this.VIRTUAL_HEIGHT - bordersize, this.VIRTUAL_WIDTH, this.VIRTUAL_HEIGHT - bordersize, false);
		//Left - at x=1
		this.CreateBoundary(bordersize, 0f, bordersize, this.VIRTUAL_HEIGHT, false);
		//Right - at x=1599
		this.CreateBoundary(this.VIRTUAL_WIDTH - bordersize, 0f, this.VIRTUAL_WIDTH - bordersize, this.VIRTUAL_HEIGHT, false);

		this.debugRenderer = new Box2DDebugRenderer();

		//Create Wall data for Wall Avoidance
		this.CreateWalls();
		
		// Note: Removed the old PlayerActor that was conflicting with PlayerShip
		// The PlayerShip serves as the main player entity
		
		// add input handler to scene
		this.gameStage.addListener(new GameStageInputHandler(this.app, this.world, this.gameStage));

		// Create PlayerShip at center of game field (800, 600) with 50x scale
		this.playerShip = new PlayerShip("agent", this.world, 800, 600, 5, 2, 50.0f);
		this.playerShip.setIdleAnimFrameDuration(0.05f);
		this.gameStage.addActor(this.playerShip);
		
		// Create ProjectilePool
		this.projectilePool = new ProjectilePool(this.world, this.gameStage);
		
		// Initialize enemy system components
		this.healthSystem = new HealthSystem();
		this.enemyManager = new EnemyManager(this.healthSystem);
		this.enemyManager.setGameScreen(this); // Set GameScreen reference for deferred physics cleanup
		this.enemySpawner = new EnemySpawner(this.world, this.gameStage, this.enemyManager, this.playerShip, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
		
		// Initialize behavior label rendering system
		com.plv.uberplayground.enemies.EnemyBehaviorLabel.initialize();
		
		// Create PlayerShipInputHandler
		this.playerShipInputHandler = new PlayerShipInputHandler(this.playerShip, this);
		
		// Initialize game state
		this.isGameOver = false;
		this.gameOverTimer = 0.0f;

		// InputMultiplexer - HUD stage first, then PlayerShip input, then Actor/Game stage
		InputMultiplexer multiplexer = new InputMultiplexer();
		multiplexer.addProcessor(this.hudStage);
		multiplexer.addProcessor(this.playerShipInputHandler);
		multiplexer.addProcessor(this.gameStage);
		Gdx.input.setInputProcessor(multiplexer);
	}

	@Override
	public void show() {
		//this.player.getBody().applyForceToCenter(new Vector2(0f, 0.5f), true);
	}

	@Override
	public void render(float delta) {
		// Start performance monitoring
		com.plv.uberplayground.enemies.PerformanceOptimizer.startFrame();
		
		Configuration.ElapsedTime += delta;
		
		// Only update game systems if not in game over state
		if (!isGameOver) {
			// Update player ship input before physics step
			this.playerShipInputHandler.update();
			
			// Update enemy system before physics step
			this.enemySpawner.update(delta);
			
			// Update player targets for enemy behaviors
			if (this.playerShip != null && this.playerShip.getBody() != null) {
				this.enemyManager.updatePlayerTargets(this.playerShip.getBody().getPosition());
			}
			
			this.enemyManager.updateAll(delta);
			
			// Update projectiles after physics step (only during active gameplay)
			this.projectilePool.updateAll(delta);
		}
		
		// Always step physics world to maintain stability
		try {
			this.world.step(delta, 6, 2);
		} catch (Exception e) {
			Gdx.app.error("Physics", "Error during physics step: " + e.getMessage());
			// If physics step fails, trigger game over to prevent further issues
			if (!isGameOver) {
				gameOverTriggered = true;
			}
		}
		
		// Process deferred physics body cleanup after physics step
		processDeferredPhysicsCleanup();
		
		// Process game over trigger after physics step to prevent Box2D crashes
		processGameOverTrigger();
		
		// Handle game over transition
		if (isGameOver) {
			// Check for restart input (R key)
			if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.R)) {
				resetGame();
				return; // Exit early to start fresh gameplay
			}
			
			gameOverTimer -= delta;
			if (gameOverTimer <= 0) {
				try {
					// Dispose resources before transitioning
					dispose();
					// Transition to MainMenuScreen
					app.setScreen(new MainMenuScreen(app));
					return;
				} catch (Exception e) {
					Gdx.app.error("GameOver", "Error during screen transition: " + e.getMessage());
					// Force exit if screen transition fails
					Gdx.app.exit();
				}
			}
		}

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// TODO
		// Calling to Stage methods - order?
		this.hudStage.act(delta);
		this.gameStage.act(delta);

		this.hudStage.draw();
		this.gameStage.draw();

		this.debugRenderer.render(this.world, this.gameStage.getViewport().getCamera().combined);
		
		// End performance monitoring and log if performance is poor
		float frameTime = com.plv.uberplayground.enemies.PerformanceOptimizer.endFrame();
		if (!com.plv.uberplayground.enemies.PerformanceOptimizer.isPerformanceAcceptable()) {
			// Log performance warning every 5 seconds to avoid spam
			if (Configuration.ElapsedTime % 5.0f < delta) {
				Gdx.app.log("Performance", com.plv.uberplayground.enemies.PerformanceOptimizer.getPerformanceStats());
			}
		}
	}

	@Override
	public void resize(int width, int height) {
		// TODO
		// this.getViewport().update(width/PPM, height/PPM);
		// this.stage.getViewport().setCamera(this.app.gameCamera);
		// ((OrthographicCamera)this.stage.getCamera()).setToOrtho(false,this.app.VIRTUAL_WIDTH/PPM,
		// this.app.VIRTUAL_HEIGHT/PPM);
		// ((OrthographicCamera)this.stage.getCamera()).translate((VIRTUAL_WIDTH/2)/PPM,
		// (VIRTUAL_HEIGHT/2)/PPM);
		// this.stage.getViewport().update(16, 9, false);
	}

	@Override
	public void pause() {

	}

	@Override
	public void resume() {

	}

	@Override
	public void hide() {

	}

	@Override
	public void dispose() {
		// Process any remaining deferred physics cleanups before disposing world
		processDeferredPhysicsCleanup();
		
		// Dispose behavior label resources
		try {
			com.plv.uberplayground.enemies.EnemyBehaviorLabel.dispose();
		} catch (Exception e) {
			Gdx.app.error("Dispose", "Error disposing behavior label resources: " + e.getMessage());
		}
		
		try {
			if (this.projectilePool != null) {
				this.projectilePool.disposeAll();
			}
		} catch (Exception e) {
			Gdx.app.error("Dispose", "Error disposing projectile pool: " + e.getMessage());
		}
		
		try {
			if (this.debugRenderer != null) {
				this.debugRenderer.dispose();
			}
		} catch (Exception e) {
			Gdx.app.error("Dispose", "Error disposing debug renderer: " + e.getMessage());
		}
		
		try {
			if (this.world != null) {
				this.world.dispose();
			}
		} catch (Exception e) {
			Gdx.app.error("Dispose", "Error disposing world: " + e.getMessage());
		}
		
		try {
			if (this.gameStage != null) {
				this.gameStage.dispose();
			}
		} catch (Exception e) {
			Gdx.app.error("Dispose", "Error disposing game stage: " + e.getMessage());
		}
		
		try {
			if (this.hudStage != null) {
				this.hudStage.dispose();
			}
		} catch (Exception e) {
			Gdx.app.error("Dispose", "Error disposing hud stage: " + e.getMessage());
		}
	}

	public void spawnControlPointAtPos(float x, float y) {
		// Note: This method was for the old PlayerActor system
		// With the new enemy system, we use PlayerShip as the main player
		// This method can be removed or repurposed if needed
		Gdx.app.log("SpawnControl", "Control point spawning not used in current enemy system");
	}

	public void spawnProjectile(Vector2 position, Vector2 direction, Vector2 shipVelocity) {
		Projectile projectile = this.projectilePool.obtain(position, direction, shipVelocity);
		// Note: ProjectilePool.obtain() already adds the projectile to gameStage if not null
		// No additional action needed here as the pool manages the lifecycle
	}
	
	// Helper method to check if collision is between two specific categories
	private boolean isCollision(Fixture a, Fixture b, short cat1, short cat2) {
		return (a.getFilterData().categoryBits == cat1 && b.getFilterData().categoryBits == cat2) ||
			   (a.getFilterData().categoryBits == cat2 && b.getFilterData().categoryBits == cat1);
	}
	
	// Helper method to extract Projectile from fixture user data
	private Projectile getProjectileFromFixtures(Fixture a, Fixture b) {
		// Check fixture A
		if (a.getFilterData().categoryBits == Configuration.EntityCategory.PROJECTILE.getValue()) {
			Object userData = a.getBody().getUserData();
			if (userData instanceof Projectile) {
				return (Projectile) userData;
			}
		}
		
		// Check fixture B
		if (b.getFilterData().categoryBits == Configuration.EntityCategory.PROJECTILE.getValue()) {
			Object userData = b.getBody().getUserData();
			if (userData instanceof Projectile) {
				return (Projectile) userData;
			}
		}
		
		return null;
	}
	
	// Helper method to extract Enemy (PlayerActor) from fixture user data
	private PlayerActor getEnemyFromFixtures(Fixture a, Fixture b) {
		// Check fixture A
		if (a.getFilterData().categoryBits == Configuration.EntityCategory.ENEMY_SHIP.getValue()) {
			Object userData = a.getBody().getUserData();
			if (userData instanceof PlayerActor) {
				PlayerActor actor = (PlayerActor) userData;
				if (actor.isEnemy()) {
					return actor;
				}
			}
		}
		
		// Check fixture B
		if (b.getFilterData().categoryBits == Configuration.EntityCategory.ENEMY_SHIP.getValue()) {
			Object userData = b.getBody().getUserData();
			if (userData instanceof PlayerActor) {
				PlayerActor actor = (PlayerActor) userData;
				if (actor.isEnemy()) {
					return actor;
				}
			}
		}
		
		return null;
	}
	
	// Trigger game over and schedule transition to MainMenuScreen
	public void triggerGameOver() {
		if (!isGameOver && !gameOverTriggered) {
			gameOverTriggered = true;
			Gdx.app.log("GameOver", "Game Over collision detected - will process after physics step");
		}
	}
	
	/**
	 * Processes deferred physics body cleanup after the physics step is complete.
	 * This prevents Box2D crashes that can occur when destroying bodies during collision callbacks.
	 */
	private void processDeferredPhysicsCleanup() {
		if (enemiesToDisposePhysics.size > 0) {
			Gdx.app.log("PhysicsCleanup", "Processing " + enemiesToDisposePhysics.size + " deferred physics body cleanups");
			
			for (PlayerActor enemy : enemiesToDisposePhysics) {
				try {
					if (enemy != null) {
						enemy.forceDispose();
					}
				} catch (Exception e) {
					Gdx.app.error("PhysicsCleanup", "Error disposing enemy physics body: " + e.getMessage());
				}
			}
			
			enemiesToDisposePhysics.clear();
		}
	}
	
	/**
	 * Queues an enemy for deferred physics body cleanup.
	 * This should be called when an enemy dies to ensure safe physics body disposal.
	 * @param enemy The enemy to queue for physics cleanup
	 */
	public void queueEnemyForPhysicsCleanup(PlayerActor enemy) {
		if (enemy != null && !enemiesToDisposePhysics.contains(enemy, true)) {
			enemiesToDisposePhysics.add(enemy);
			Gdx.app.log("PhysicsCleanup", "Queued enemy for deferred physics cleanup");
		}
	}
	private void processGameOverTrigger() {
		if (gameOverTriggered && !isGameOver) {
			isGameOver = true;
			gameOverTriggered = false;
			gameOverTimer = 2.0f; // Increased to 2 seconds for more stable transition
			
			try {
				// Stop enemy spawning immediately
				if (enemySpawner != null) {
					enemySpawner.setSpawningEnabled(false);
				}
				
				// Pause all enemy updates by clearing all enemies
				if (enemyManager != null) {
					enemyManager.clearAllEnemies();
				}
				
				// Clear all active projectiles from the game
				if (projectilePool != null) {
					projectilePool.clearAllActive();
				}
				
				// Clear any pending physics body cleanups
				enemiesToDisposePhysics.clear();
				
				Gdx.app.log("GameOver", "Game Over processed! Press R to restart or wait to return to main menu.");
			} catch (Exception e) {
				Gdx.app.error("GameOver", "Error during game over cleanup: " + e.getMessage());
				// Ensure game over state is still set even if cleanup fails
				isGameOver = true;
				gameOverTimer = 0.5f; // Shorter timer to transition quickly
			}
		}
	}
	
	/**
	 * Resets the game to its initial state for restarting gameplay.
	 * This method clears all game entities and reinitializes systems.
	 */
	public void resetGame() {
		try {
			// Clear game over state
			isGameOver = false;
			gameOverTimer = 0.0f;
			gameOverTriggered = false;
			
			// Clear all enemies and reset enemy system
			if (enemyManager != null) {
				enemyManager.clearAllEnemies();
			}
			if (healthSystem != null) {
				healthSystem.clear();
			}
			
			// Clear all projectiles
			if (projectilePool != null) {
				projectilePool.clearAllActive();
			}
			
			// Clear any pending physics body cleanups
			enemiesToDisposePhysics.clear();
			
			// Reset player ship to center position
			if (playerShip != null && playerShip.getBody() != null) {
				playerShip.getBody().setTransform(800, 600, 0); // Center of game field
				playerShip.getBody().setLinearVelocity(0, 0);
				playerShip.getBody().setAngularVelocity(0);
			}
			
			// Re-enable enemy spawning
			if (enemySpawner != null) {
				enemySpawner.setSpawningEnabled(true);
				enemySpawner.resetSpawnTimer();
			}
			
			Gdx.app.log("GameReset", "Game has been reset to initial state");
		} catch (Exception e) {
			Gdx.app.error("GameReset", "Error during game reset: " + e.getMessage());
			// If reset fails, force transition to main menu
			try {
				dispose();
				app.setScreen(new MainMenuScreen(app));
			} catch (Exception e2) {
				Gdx.app.error("GameReset", "Critical error, forcing exit: " + e2.getMessage());
				Gdx.app.exit();
			}
		}
	}

	public void CreateBoundary(float x1, float y1, float x2, float y2, Boolean isSensor){
		Body boundary;
		//body def
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.StaticBody;

		//body shape
		EdgeShape shape = new EdgeShape();
		shape.set(x1, y1, x2, y2);


		//body fixture
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = 0.1f;
		fixtureDef.isSensor = isSensor;
		fixtureDef.filter.categoryBits = Configuration.EntityCategory.BOUNDARY.getValue();
		fixtureDef.filter.maskBits = (short) (Configuration.EntityCategory.PLAYER.getValue() | 
											  Configuration.EntityCategory.ENEMY_SHIP.getValue() | 
											  Configuration.EntityCategory.CONTROL_POINT.getValue() |
											  Configuration.EntityCategory.PLAYER_SHIP.getValue() |
											  Configuration.EntityCategory.PROJECTILE.getValue());

		boundary = this.world.createBody(bodyDef);
		boundary.createFixture(fixtureDef);

		shape.dispose();
	}
	
	public void CreateWalls(){
		
		//create the walls  
		float bordersize = 1f;
		float CornerSize = 0f;
		
		int m_cyClient = this.VIRTUAL_HEIGHT;
		int m_cxClient = this.VIRTUAL_WIDTH;
		float vDist = m_cyClient-2*bordersize;
		float hDist = m_cxClient-2*bordersize;

//		float offsetTopBottom = 0.5f;
//		Vector2 w1A = new Vector2(0, this.VIRTUAL_HEIGHT - offsetTopBottom);
//		Vector2 w1B = new Vector2(this.VIRTUAL_WIDTH, this.VIRTUAL_HEIGHT - offsetTopBottom);
//		
//		Vector2 w2A = new Vector2(0f, 0 + offsetTopBottom);
//		Vector2 w2B = new Vector2(this.VIRTUAL_WIDTH, 0 + offsetTopBottom);

		Vector2 w1A = new Vector2(hDist*CornerSize+bordersize, bordersize);
		Vector2 w1B = new Vector2(m_cxClient-bordersize-hDist*CornerSize, bordersize);
		Vector2 w2A = new Vector2(m_cxClient-bordersize, bordersize+vDist*CornerSize);
		Vector2 w2B = new Vector2(m_cxClient-bordersize, m_cyClient-bordersize-vDist*CornerSize);

		Vector2 w3A = new Vector2(m_cxClient-bordersize-hDist*CornerSize, m_cyClient-bordersize);
		Vector2 w3B = new Vector2(hDist*CornerSize+bordersize, m_cyClient-bordersize);
		Vector2 w4A = new Vector2(bordersize, m_cyClient-bordersize-vDist*CornerSize);
		Vector2 w4B = new Vector2(bordersize, bordersize+vDist*CornerSize);
		  
		
		Configuration.Walls.add(new Wall(w1A, w1B));
		//Configuration.Walls.add(new Wall(w2A, w2B));
		Configuration.Walls.add(new Wall(w1B, w2A));
		Configuration.Walls.add(new Wall(w2A, w2B));
		Configuration.Walls.add(new Wall(w2B, w3A));
		Configuration.Walls.add(new Wall(w3A, w3B));
		Configuration.Walls.add(new Wall(w3B, w4A));
		Configuration.Walls.add(new Wall(w4A, w4B));
		Configuration.Walls.add(new Wall(w4B, w1A));
		
		
//		//everything except last one
//		for (int w=0; w<NumWallVerts-1; ++w)
//		{
//		  m_Walls.push_back(Wall2D(walls[w], walls[w+1]));
//		}
//		
//		//last one and first one
//		m_Walls.push_back(Wall2D(walls[NumWallVerts-1], walls[0]));
		
		
		
//		Body boundary;
//		//body def
//		BodyDef bodyDef = new BodyDef();
//		bodyDef.type = BodyDef.BodyType.StaticBody;
//
//		//body shape
//		EdgeShape shape = new EdgeShape();
//		shape.set(x1, y1, x2, y2);
//
//
//		//body fixture
//		FixtureDef fixtureDef = new FixtureDef();
//		fixtureDef.shape = shape;
//		fixtureDef.density = 0.1f;
//		fixtureDef.isSensor = isSensor;
//		fixtureDef.filter.categoryBits = Configuration.EntityCategory.BOUNDARY.getValue();
//		fixtureDef.filter.maskBits = (short) (Configuration.EntityCategory.PLAYER.getValue() | Configuration.EntityCategory.ENEMY_SHIP.getValue() | Configuration.EntityCategory.CONTROL_POINT.getValue());
//
//		boundary = this.world.createBody(bodyDef);
//		boundary.createFixture(fixtureDef);
//
//		shape.dispose();
	}
}
