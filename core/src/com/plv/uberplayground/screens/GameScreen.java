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
import com.plv.uberplayground.actors.Agent;
import com.plv.uberplayground.UberPlayground;
import com.plv.uberplayground.inputlisteners.AgentListener;
import com.plv.uberplayground.inputlisteners.HudListener;
import com.plv.uberplayground.inputlisteners.SceneListener;

public class GameScreen implements Screen {
    private World world;
    private Box2DDebugRenderer debugRenderer;

    private static final int VIRTUAL_WIDTH = 16;
    private static final int VIRTUAL_HEIGHT = 9;

    private OrthographicCamera mainCamera;
    //group for agent and its particle emitter, physics applies to group, will rotate emitter as well

    //UI menu in-game - different VIRTUAL_WIDTH/VIRTUAL_HEIGHT
    private Stage hud;
    //stage for actors, and also playfield to spawn 'targets'
    private Stage scene;
    private Agent agent;
    private final UberPlayground app;

    public GameScreen(final UberPlayground app) {
        this.app = app;
        this.mainCamera = new OrthographicCamera();
        this.hud = new Stage(new FillViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, this.mainCamera));
        this.scene = new Stage(new FillViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, this.mainCamera));

        this.world = new World(new Vector2(), true);
        this.debugRenderer = new Box2DDebugRenderer();

        //add button actors to hud

        //add agent to scene
        this.agent = new Agent(this.world, this.app);
        this.hud.addActor(agent);

        //add inputlisteners for stage/actor
        this.hud.addListener(new HudListener(this.world)); //add listeners for buttons
        //this.scene.addListener(new SceneListener());
        this.agent.addListener(new AgentListener(this.world));

        //InputMultiplexer - UI stage first, then Actor/Playfield stage
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(this.hud);
        //multiplexer.addProcessor(this.scene);
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


        // Calling to Stage methods
        this.scene.act(delta);
        this.hud.act(delta);
        this.scene.draw();
        this.hud.draw();

        this.debugRenderer.render(this.world, this.scene.getViewport().getCamera().combined);
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
        this.scene.dispose();
    }
}
