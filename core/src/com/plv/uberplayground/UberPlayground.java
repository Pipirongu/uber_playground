package com.plv.uberplayground;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class UberPlayground extends Game {
	private Stage stage;
	private World world;
	private Box2DDebugRenderer debugRenderer;
	//group for agent and its particle emitter, physics applies to group, will rotate emitter as well

	@Override
	public void create () {
		this.stage = new Stage(new ExtendViewport(Gdx.graphics.getWidth(),Gdx.graphics.getHeight()));
		this.world = new World(new Vector2(), true);
		this.debugRenderer = new Box2DDebugRenderer();

		//add agent to stage
		Agent agent = new Agent(this.world);
		this.stage.addActor(agent);

		//Our Input Handler
		Gdx.input.setInputProcessor(new GestureDetector(new InputHandler()));
	}

	@Override
	public void dispose () {
		this.stage.dispose();
		this.world.dispose();
	}

	@Override
	public void render () {
		world.step(Gdx.graphics.getDeltaTime(), 6, 2);

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		//batch.setProjectionMatrix(camera.combined);
		//elapsedTime += Gdx.graphics.getDeltaTime();
		this.debugRenderer.render(this.world, stage.getViewport().getCamera().combined);
		this.stage.draw();
	}

	@Override
	public void resize(int width, int height) {
		this.stage.getViewport().setCamera(new OrthographicCamera(width, height));
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

}
