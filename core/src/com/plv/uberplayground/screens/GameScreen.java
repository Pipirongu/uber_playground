package com.plv.uberplayground.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.plv.uberplayground.Agent;
import com.plv.uberplayground.UberPlayground;

public class GameScreen implements Screen {
    private World world;
    private Box2DDebugRenderer debugRenderer;

    private final static float PPM = 200.f;
    //group for agent and its particle emitter, physics applies to group, will rotate emitter as well

    private Stage stage;
    private final UberPlayground app;

    private static final int VIRTUAL_WIDTH = 1280;
    private static final int VIRTUAL_HEIGHT = 720;

    public GameScreen(final UberPlayground app) {
        this.app = app;
        this.stage = new Stage(new FillViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, this.app.gameCamera));
        Gdx.input.setInputProcessor(stage);

        //
        this.world = new World(new Vector2(), true);
        this.debugRenderer = new Box2DDebugRenderer();

        //add agent to stage
        Agent agent = new Agent(this.world);
        this.stage.addActor(agent);

        //Our Input Handler
        //Gdx.input.setInputProcessor(new GestureDetector(new InputHandler()));
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
        this.stage.act(delta);
        this.stage.draw();

        this.debugRenderer.render(this.world, this.stage.getViewport().getCamera().combined);
    }

    @Override
    public void resize(int width, int height) {
        //this.getViewport().update(width/PPM, height/PPM);
        //this.stage.getViewport().setCamera(this.app.gameCamera);
        ((OrthographicCamera)this.stage.getCamera()).setToOrtho(false,VIRTUAL_WIDTH/PPM, VIRTUAL_HEIGHT/PPM);
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
        this.stage.dispose();
    }
}
