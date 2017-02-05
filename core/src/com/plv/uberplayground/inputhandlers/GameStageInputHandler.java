package com.plv.uberplayground.inputhandlers;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.plv.uberplayground.UberPlayground;
import com.plv.uberplayground.config.Configuration;
import com.plv.uberplayground.screens.GameScreen;

public class GameStageInputHandler extends InputListener {
	private UberPlayground app;
	private World world;
	private Stage gameStage;
	private Vector2 point = new Vector2();
	// used to store the body which collides with touch point
	private Body bodyThatWasHit = null;

	public GameStageInputHandler(final UberPlayground app, final World world, final Stage gameStage){
		this.app = app;
		this.world = world;
		this.gameStage = gameStage;
	}

	@Override
	public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
		this.point.set(x, y); // Translate to world coordinates.

		QueryCallback callback = new QueryCallback() {
			@Override
			public boolean reportFixture (Fixture fixture) {
				if (fixture.testPoint(GameStageInputHandler.this.point.x, GameStageInputHandler.this.point.y)) {
					GameStageInputHandler.this.bodyThatWasHit = fixture.getBody();
					return false;
				} else {
					return true;
				}
			}
		};

		float someOffset = 0.1f;
		this.world.QueryAABB(callback, this.point.x - someOffset, this.point.y - someOffset, this.point.x + someOffset, this.point.y + someOffset);

		//return true to handle the input here and prevent the input event to be sent to child actors
		return true;
	}

	@Override
	public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
		if(this.bodyThatWasHit != null){
			Filter filter = this.bodyThatWasHit.getFixtureList().get(0).getFilterData();

			//do something when touching units
			if(filter.categoryBits == Configuration.EntityCategory.CONTROL_POINT.getValue()) {

			}

			this.bodyThatWasHit = null;
		}else{
			//Spawn new control point
			GameScreen gameScreen = (GameScreen)this.app.getScreen();
			gameScreen.spawnControlPointAtPos(this.point.x, this.point.y);
		}
	}

}
