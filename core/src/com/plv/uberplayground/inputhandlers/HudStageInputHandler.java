package com.plv.uberplayground.inputhandlers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

public class HudStageInputHandler extends InputListener {

    public HudStageInputHandler(){
    }

    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        //return false and let actors handle the events
        return false;
    }

    @Override
    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

    }

}
