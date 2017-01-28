package com.plv.uberplayground.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.plv.uberplayground.UberPlayground;
import com.plv.uberplayground.actors.AnimatedActor;
import com.plv.uberplayground.actors.AnimatedPhysicsActor;
import com.plv.uberplayground.actors.ControlPointActor;
import com.plv.uberplayground.actors.PlayerActor;
import com.plv.uberplayground.inputhandlers.GameStageInputHandler;

public class GameScreen implements Screen {
	private World world;
	private Box2DDebugRenderer debugRenderer;

	private static final int VIRTUAL_WIDTH = 16;
	private static final int VIRTUAL_HEIGHT = 9;

	private OrthographicCamera mainCamera;
	// group for agent and its particle emitter, physics applies to group, will
	// rotate emitter as well

	// UI menu in-game - different VIRTUAL_WIDTH/VIRTUAL_HEIGHT
	private Stage hudStage;
	// stage for actors, and also playfield to spawn 'targets'
	private Stage gameStage;
	private PlayerActor player;
	private ControlPointActor controlPoint = null;
	private final UberPlayground app;

	private long startTime;

	public GameScreen(final UberPlayground app) {
		this.app = app;
		this.mainCamera = new OrthographicCamera();
		this.hudStage = new Stage(new FillViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, this.mainCamera));
		this.gameStage = new Stage(new FillViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, this.mainCamera));

		this.world = new World(new Vector2(), true);
		this.debugRenderer = new Box2DDebugRenderer();

		// create agents
		this.player = new PlayerActor(AnimatedPhysicsActor.ActorType.PLAYER, "agent", this.world, VIRTUAL_WIDTH / 2,
				VIRTUAL_HEIGHT / 2, 5, 2);
		this.player.setIdleAnimFrameDuration(0.05f);
		this.player.setParticleEmitter("exhaust", 0.f, 0.f, true, true);
		// this.player.setLinearVelocity(0.f, 0.1f);

		// TODO
		// add button actors to hud
		// this.hudStage.addListener(new HudStageInputHandler());
		// this.hudStage.addActor(agent);

		// add agent to scene
		this.gameStage.addListener(new GameStageInputHandler(this.app, this.world, this.gameStage));
		this.gameStage.addActor(this.player);

		// InputMultiplexer - HUD stage first, then Actor/Game stage
		InputMultiplexer multiplexer = new InputMultiplexer();
		multiplexer.addProcessor(this.hudStage);
		multiplexer.addProcessor(this.gameStage);
		Gdx.input.setInputProcessor(multiplexer);
	}

	@Override
	public void show() {
	}

	@Override
	public void render(float delta) {
		this.world.step(delta, 6, 2);

		if (this.controlPoint != null) {
			Body playerBody = this.player.getBody();
			Body controlPointBody = this.controlPoint.getBody();

			// TODO
			float velocity = 5f; // Your desired velocity of the car.
			float angle = playerBody.getAngle(); // Body angle in radians.
			float velX = (float) (Math.cos(angle) * velocity); // X-component.
			float velY = (float) (Math.sin(angle) * velocity); // Y-component.

			/*
			 * interpolate from player's angle to desired angle. 0.1f amount of
			 * desired angle for each iteration next interpolation uses new
			 * player's angle with some rotation from desired angle which makes
			 * 0.1f as progress argument possible
			 */

			/*
			 * different solution would be to store the player angle and then
			 * update the progress arg with if statement player angle will only
			 * be updated when a new controlPoint is spawned //TODO test
			 * interpolation method
			 */
			playerBody.setTransform(playerBody.getPosition(),
					MathUtils.lerpAngle(playerBody.getAngle(), this.getDesiredAngle(), 0.1f));
			playerBody.setLinearVelocity(-velY, velX);
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
		this.debugRenderer.dispose();
		this.world.dispose();
		this.gameStage.dispose();
	}

	public void spawnControlPointAtPos(float x, float y) {
		// spawn someting
		if (this.controlPoint == null) {
			this.controlPoint = new ControlPointActor(AnimatedPhysicsActor.ActorType.SPAWNUNIT, "spawn_unit",
					this.world, x, y, 2, 2);
			this.controlPoint.setIdleAnimFrameDuration(0.1f);
			this.gameStage.addActor(this.controlPoint);

			// start timer
			this.startTime = System.currentTimeMillis();
		} else {
			// if timer 1 sec spawn and reset
			// animate previous spawnUnit position
			float elapsedTime = (System.currentTimeMillis() - this.startTime) / 1000f;

			if (elapsedTime >= 0f) {
				this.startTime = System.currentTimeMillis();

				Vector2 previousBodyPos = this.controlPoint.getBodyPosition();
				AnimatedActor explosionAtPrevPos = new AnimatedActor("explosion", previousBodyPos.x, previousBodyPos.y,
						5, 1);
				explosionAtPrevPos.setIdleAnimFrameDuration(0.1f);
				this.gameStage.addActor(explosionAtPrevPos);

				// remove old spawnUnit
				this.controlPoint.remove();
				this.world.destroyBody(this.controlPoint.getBody());

				// add new spawnUnit
				this.controlPoint = new ControlPointActor(AnimatedPhysicsActor.ActorType.SPAWNUNIT, "spawn_unit",
						this.world, x, y, 2, 2);
				this.controlPoint.setIdleAnimFrameDuration(0.1f);
				this.gameStage.addActor(this.controlPoint);
			}
		}
	}

	// TODO test funcs
	public float getDesiredAngle() {
		float targetX = this.controlPoint.getBody().getPosition().x - this.player.getBody().getPosition().x;
		float targetY = this.controlPoint.getBody().getPosition().y - this.player.getBody().getPosition().y;

		return (float) (Math.atan2(-targetX, targetY));
	}
}
