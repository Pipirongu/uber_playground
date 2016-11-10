package com.plv.uberplayground;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Agent extends Actor {
    private TextureAtlas textureAtlas;
    private Animation animation;
    private float elapsedTime = 0;

    Agent(){
        textureAtlas = new TextureAtlas(Gdx.files.internal("agent/agent.pack"));
        animation = new Animation(1 / 30f, textureAtlas.getRegions());
    }

    @Override
    public void draw(Batch batch, float alpha) {
        elapsedTime += Gdx.graphics.getDeltaTime();
        batch.draw(animation.getKeyFrame(elapsedTime, true), 0, 0);
    }


}
