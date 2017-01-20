package com.plv.uberplayground;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.plv.uberplayground.screens.MainMenuScreen;

public class UberPlayground extends Game {
	@Override
	public void create () {
		//TODO
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
		this.setScreen(new MainMenuScreen(this));
	}

	@Override
	public void dispose () {
	}
}
