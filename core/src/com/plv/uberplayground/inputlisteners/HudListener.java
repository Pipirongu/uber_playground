package com.plv.uberplayground.inputlisteners;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

public class HudListener extends InputListener {
    public HudListener(){
    }

    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        //Gdx.app.log("HudListener: ", "Touch Down!");
        return false;
    }

    @Override
    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        //Gdx.app.log("HudListener: ", "Touch Released!");
    }

}
