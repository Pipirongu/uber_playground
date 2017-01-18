package com.plv.uberplayground.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.plv.uberplayground.actors.AnimatedPhysicsActor;
import com.plv.uberplayground.UberPlayground;
import com.plv.uberplayground.inputhandlers.GameStageInputHandler;

public class GameScreen implements Screen {
    private World world;
    private Box2DDebugRenderer debugRenderer;

    private static final int VIRTUAL_WIDTH = 16;
    private static final int VIRTUAL_HEIGHT = 9;

    private OrthographicCamera mainCamera;
    //group for agent and its particle emitter, physics applies to group, will rotate emitter as well

    //UI menu in-game - different VIRTUAL_WIDTH/VIRTUAL_HEIGHT
    private Stage hudStage;
    //stage for actors, and also playfield to spawn 'targets'
    private Stage gameStage;
    private AnimatedPhysicsActor agent;
    private final UberPlayground app;

    public GameScreen(final UberPlayground app) {
        this.app = app;
        this.mainCamera = new OrthographicCamera();
        this.hudStage = new Stage(new FillViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, this.mainCamera));
        this.gameStage = new Stage(new FillViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, this.mainCamera));

        this.world = new World(new Vector2(), true);
        this.debugRenderer = new Box2DDebugRenderer();

        //create agents
        this.agent = new AnimatedPhysicsActor(AnimatedPhysicsActor.ActorType.PLAYER,"agent", this.world,VIRTUAL_WIDTH/2,VIRTUAL_HEIGHT/2,5,2);
        this.agent.setIdleAnimFrameDuration(0.05f);
        this.agent.setParticleEmitter("exhaust",0.f,0.f,true,true);
        this.agent.setLinearVelocity(0.f, 0.1f);

        //add button actors to hud
        //this.hudStage.addListener(new HudStageInputHandler());
        //this.hudStage.addActor(agent);

        //add agent to scene
        this.gameStage.addListener(new GameStageInputHandler(this.world, this.gameStage));
        this.gameStage.addActor(this.agent);

        //InputMultiplexer - HUD stage first, then Actor/Game stage
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

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


		//TODO
        // Calling to Stage methods - order?
        this.hudStage.act(delta);
        this.gameStage.act(delta);

        this.hudStage.draw();
        this.gameStage.draw();

        this.debugRenderer.render(this.world, this.gameStage.getViewport().getCamera().combined);
    }

    @Override
    public void resize(int width, int height) {
        //this.getViewport().update(width/PPM, height/PPM);
        //this.stage.getViewport().setCamera(this.app.gameCamera);
        //((OrthographicCamera)this.stage.getCamera()).setToOrtho(false,this.app.VIRTUAL_WIDTH/PPM, this.app.VIRTUAL_HEIGHT/PPM);
        //((OrthographicCamera)this.stage.getCamera()).translate((VIRTUAL_WIDTH/2)/PPM, (VIRTUAL_HEIGHT/2)/PPM);
        //this.stage.getViewport().update(16, 9, false);
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
}
