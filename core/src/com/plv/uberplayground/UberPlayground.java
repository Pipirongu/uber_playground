package com.plv.uberplayground;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class UberPlayground extends ApplicationAdapter {
	private OrthographicCamera camera;
	private Stage stage;
	//group for agent and its particle emitter, physics applies to group, will rotate emitter as well

	@Override
	public void create () {
		camera = new OrthographicCamera(1280, 720);

		stage = new Stage(new ExtendViewport(Gdx.graphics.getWidth(),Gdx.graphics.getHeight()));
		//add agent to stage
		Agent agent = new Agent();
		stage.addActor(agent);

		//Our Input Handler
		Gdx.input.setInputProcessor(new GestureDetector(new InputHandler(camera)));
	}

	@Override
	public void dispose () {
		stage.dispose();
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		//batch.setProjectionMatrix(camera.combined);
		//elapsedTime += Gdx.graphics.getDeltaTime();
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

}
