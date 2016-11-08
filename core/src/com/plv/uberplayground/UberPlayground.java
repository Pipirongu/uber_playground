package com.plv.uberplayground;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.*;

public class UberPlayground extends ApplicationAdapter {
	private SpriteBatch batch;
	private TextureAtlas textureAtlas;
	private Animation animation;
	private float elapsedTime = 0;
	//private Sprite sprite;
	//private int currentFrame = 1;
	//private String currentAtlasKey = new String("0001");
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		textureAtlas = new TextureAtlas(Gdx.files.internal("agent/agent.pack"));

		TextureRegion[] idleAnim = new TextureRegion[5];

		idleAnim[0] = (textureAtlas.findRegion("0001"));
		idleAnim[1] = (textureAtlas.findRegion("0002"));
		idleAnim[2] = (textureAtlas.findRegion("0003"));
		idleAnim[3] = (textureAtlas.findRegion("0004"));
		idleAnim[4] = (textureAtlas.findRegion("0005"));

		animation = new Animation(1 / 5f, idleAnim);
	}


	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.begin();
		//sprite.draw(batch);
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
