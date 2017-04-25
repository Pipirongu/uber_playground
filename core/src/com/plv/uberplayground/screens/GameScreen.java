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
import com.plv.uberplayground.actors.Wall;
import com.plv.uberplayground.config.Configuration;
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
	public Array<Wall>Walls = new Array<Wall>();
	
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
				// Check to see if the collision is between the second sprite
				// and the bottom of the screen
				// If so apply a random amount of upward force to both
				// objects... just because
				if ((contact.getFixtureA().getFilterData().categoryBits == Configuration.EntityCategory.BOUNDARY.getValue() &&
						contact.getFixtureB().getFilterData().categoryBits == Configuration.EntityCategory.PLAYER.getValue()) ||

						(contact.getFixtureA().getFilterData().categoryBits == Configuration.EntityCategory.PLAYER.getValue() &&
						contact.getFixtureB().getFilterData().categoryBits == Configuration.EntityCategory.BOUNDARY.getValue()))
				{

					Gdx.app.log("Hit Detection", "Collided");
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

		float offsetTopBottom = 0f;
		float offsetLeftRight = 0f;
		//Bottom
		this.CreateBoundary(0f, 0 + offsetTopBottom, this.VIRTUAL_WIDTH, 0 + offsetTopBottom, false);
		//Top
		this.CreateBoundary(0f, this.VIRTUAL_HEIGHT - offsetTopBottom, this.VIRTUAL_WIDTH, this.VIRTUAL_HEIGHT - offsetTopBottom, false);
		//Left
		this.CreateBoundary(0 + offsetLeftRight, 0f, 0 + offsetLeftRight, this.VIRTUAL_HEIGHT, false);
		//Right
		this.CreateBoundary(this.VIRTUAL_WIDTH - offsetLeftRight, 0f, this.VIRTUAL_WIDTH - offsetLeftRight, this.VIRTUAL_HEIGHT, false);

		this.debugRenderer = new Box2DDebugRenderer();

		//Create Wall data for Wall Avoidance
		this.CreateWalls();
		
		// create agents
		this.player = new PlayerActor("agent", this.world, VIRTUAL_WIDTH / 2,
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
		this.player.getBody().applyForceToCenter(new Vector2(0f, 0.5f), true);
	}

	@Override
	public void render(float delta) {
		Configuration.ElapsedTime += delta;
		this.world.step(delta, 6, 2);



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
		ControlPointActor playerControlPoint = this.player.getControlPoint();
		// spawn someting
		if (playerControlPoint == null) {
			ControlPointActor newControlPoint = new ControlPointActor("spawn_unit", this.world, x, y, 2, 2);
			newControlPoint.setIdleAnimFrameDuration(0.1f);
			this.player.setControlPoint(newControlPoint);
			this.gameStage.addActor(newControlPoint);

		} else {
			Vector2 previousBodyPos = playerControlPoint.getBody().getPosition();
			AnimatedActor explosionAtPrevPos = new AnimatedActor("explosion", previousBodyPos.x, previousBodyPos.y, 5, 1);
			explosionAtPrevPos.setIdleAnimFrameDuration(0.1f);
			this.gameStage.addActor(explosionAtPrevPos);

			// remove old spawnUnit
			playerControlPoint.remove();
			this.world.destroyBody(playerControlPoint.getBody());

			// add new spawnUnit
			ControlPointActor newControlPoint = new ControlPointActor("spawn_unit", this.world, x, y, 2, 2);
			newControlPoint.setIdleAnimFrameDuration(0.1f);
			this.player.setControlPoint(newControlPoint);
			this.gameStage.addActor(newControlPoint);
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
		fixtureDef.filter.maskBits = (short) (Configuration.EntityCategory.PLAYER.getValue() | Configuration.EntityCategory.ENEMY_SHIP.getValue() | Configuration.EntityCategory.CONTROL_POINT.getValue());

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
