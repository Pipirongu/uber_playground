package com.plv.uberplayground;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.input.GestureDetector;

public class UberPlayground extends ApplicationAdapter {
	private SpriteBatch batch;
	private OrthographicCamera camera;
	private TextureAtlas textureAtlas;
	private Animation animation;
	private float elapsedTime = 0;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		camera = new OrthographicCamera(1280, 720);
		textureAtlas = new TextureAtlas(Gdx.files.internal("agent/agent.pack"));

		animation = new Animation(1 / 30f, textureAtlas.getRegions());

		//Our Input Handler
		Gdx.input.setInputProcessor(new GestureDetector(new InputHandler(camera)));
	}


	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		elapsedTime += Gdx.graphics.getDeltaTime();
		batch.draw(animation.getKeyFrame(elapsedTime, true), 0, 0);
		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		textureAtlas.dispose();
	}
}
