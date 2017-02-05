package com.plv.uberplayground.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.plv.uberplayground.config.Configuration;

public class AnimatedActor extends Actor {
	private Animation<TextureRegion> idleAnimation; // Must declare frame type (TextureRegion)
	private int textureWidth;
	private int textureHeight;

	public AnimatedActor(String animationName, float x, float y, int frameCols, int frameRows){
		this.setPosition(x, y);

		// Load the sprite sheet as a Texture
		Texture idleSheet;
		idleSheet = new Texture(Gdx.files.internal("actors/"+animationName+".png"));
		// Use the split utility method to create a 2D array of TextureRegions. This is
		// possible because this sprite sheet contains frames of equal size and they are
		// all aligned.
		this.textureWidth = idleSheet.getWidth() / frameCols;
		this.textureHeight = idleSheet.getHeight() / frameRows;
		TextureRegion[][] tmp = TextureRegion.split(idleSheet,
				this.textureWidth,
				this.textureHeight);

		// Place the regions into a 1D array in the correct order, starting from the top
		// left, going across first. The Animation constructor requires a 1D array.
		TextureRegion[] idleFrames = new TextureRegion[frameCols * frameRows];
		int index = 0;
		for (int i = 0; i < frameRows; i++) {
			for (int j = 0; j < frameCols; j++) {
				idleFrames[index++] = tmp[i][j];
			}
		}

		// Initialize the Animation with the frame interval and array of frames
		this.idleAnimation = new Animation<TextureRegion>(1/30f, idleFrames);
	}

	@Override
	public void draw(Batch batch, float alpha) {
		if(!this.idleAnimation.isAnimationFinished(Configuration.ElapsedTime)) {
			//sprite and body have different origins, so we draw the animation at the right place
			batch.draw(this.idleAnimation.getKeyFrame(Configuration.ElapsedTime, true),
					this.getX() - (this.textureWidth / 2.f) / Configuration.PPM,
					this.getY() - (this.textureHeight / 2.f) / Configuration.PPM,
					(this.textureWidth / 2.f) / Configuration.PPM,
					(this.textureHeight / 2.f) / Configuration.PPM,
					this.textureWidth / Configuration.PPM,
					this.textureHeight / Configuration.PPM,
					1,
					1,
					0);
		}else{
			//remove from stage
			this.remove();
		}

	}

	@Override
	public void act(float delta) {
		super.act(delta);
	}

	public void setIdleAnimFrameDuration(float frameDuration) {
		this.idleAnimation.setFrameDuration(frameDuration);
	}
}
