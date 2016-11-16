package com.plv.uberplayground;

import com.badlogic.gdx.Game;
import com.plv.uberplayground.screens.MainMenuScreen;
import com.plv.uberplayground.util.ScreenManager;

public class UberPlayground extends Game {
	@Override
	public void create () {
		ScreenManager.getInstance().initialize(this);
		ScreenManager.getInstance().showScreen(new MainMenuScreen());
	}

//	@Override
//	public void dispose () {
//		this.stage.dispose();
//		this.world.dispose();
//	}
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
