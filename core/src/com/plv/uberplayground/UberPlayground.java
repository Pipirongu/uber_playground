package com.plv.uberplayground;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.plv.uberplayground.screens.MainMenuScreen;

public class UberPlayground extends Game {
	@Override
	public void create () {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
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
