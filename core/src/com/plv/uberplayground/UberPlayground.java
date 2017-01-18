package com.plv.uberplayground;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.plv.uberplayground.screens.MainMenuScreen;

//TODO
/*
* spawnUnit only one active - removed when user spawns new one
** agent uses steering behavior to move in the direction of the spawnUnit
**** hostileUnits are in the playfield - agent explodes(animation change) on collision, transition to gameOver state
***** powerUps randomly spawns which doubles the points but has negative effects such as 2x speed on agent or changes the hostileUnits
****** inGame HUD
* <move this TODO>
* */

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
