package com.plv.uberplayground.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.plv.uberplayground.Agent;
import com.plv.uberplayground.InputHandler;

public class GameScreen extends AbstractScreen{
    private World world;
    private Box2DDebugRenderer debugRenderer;

    private final static float PPM = 200.f;
    //group for agent and its particle emitter, physics applies to group, will rotate emitter as well

    public GameScreen() {
        super();
    }

    @Override
    public void buildStage() {
		this.world = new World(new Vector2(), true);
		this.debugRenderer = new Box2DDebugRenderer();

		//add agent to stage
		Agent agent = new Agent(this.world);
		this.addActor(agent);

        //Our Input Handler
        //Gdx.input.setInputProcessor(new GestureDetector(new InputHandler()));
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void render(float delta) {
        this.world.step(delta, 6, 2);

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        // Calling to Stage methods
        this.act(delta);
        this.draw();

        this.debugRenderer.render(this.world, this.getViewport().getCamera().combined);
    }

    @Override
    public void resize(int width, int height) {
        //this.getViewport().update(width/PPM, height/PPM);
        this.getViewport().setCamera(new OrthographicCamera(width/PPM, height/PPM));
    }

    @Override
    public void pause() {
        super.pause();
    }

    @Override
    public void resume() {
        super.resume();
    }

    @Override
    public void hide() {
        super.hide();
    }

    @Override
    public void dispose() {
        super.dispose();
//        this.stage.dispose();
//		this.world.dispose();
    }
}
