package com.plv.uberplayground;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.plv.uberplayground.screens.MainMenuScreen;

public class UberPlayground extends Game {
	public static final int VIRTUAL_WIDTH = 1280;
	public static final int VIRTUAL_HEIGHT = 720;
	public static final float ASPECT_RATIO = (float)VIRTUAL_WIDTH/(float)VIRTUAL_HEIGHT;
	public OrthographicCamera mainMenuCamera;
	public OrthographicCamera gameCamera;

	@Override
	public void create () {
		this.mainMenuCamera = new OrthographicCamera();
		this.gameCamera = new OrthographicCamera();
		this.mainMenuCamera.setToOrtho(false, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
		//this.gameCamera.setToOrtho(false, 16, 9);
		this.setScreen(new MainMenuScreen(this));
	}

	@Override
	public void dispose () {
	}
//
//	@Override
//	public void render () {
//		this.world.step(Gdx.graphics.getDeltaTime(), 6, 2);
//
//		Gdx.gl.glClearColor(0, 0, 0, 1);
//		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
//
//		this.stage.act(Gdx.graphics.getDeltaTime());
//		//batch.setProjectionMatrix(camera.combined);
//		//elapsedTime += Gdx.graphics.getDeltaTime();
//		this.debugRenderer.render(this.world, stage.getViewport().getCamera().combined);
//		this.stage.draw();
//	}
}
