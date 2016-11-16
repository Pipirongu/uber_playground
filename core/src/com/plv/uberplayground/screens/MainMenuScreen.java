package com.plv.uberplayground.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.utils.Align;
import com.plv.uberplayground.util.UtilFactory;

public class MainMenuScreen extends AbstractScreen{

    private Texture start_button;

    public MainMenuScreen() {
        super();
        this.start_button = new Texture( Gdx.files.internal("ui/btn_play.png") );
    }

    @Override
    public void buildStage() {
        ImageButton btnPlay = UtilFactory.createButton(this.start_button);
        btnPlay.setPosition(getWidth() / 2, getHeight()/2, Align.center);
        addActor(btnPlay);

        // Setting listeners
        btnPlay.addListener( UtilFactory.createListener(new GameScreen()) );
    }

    @Override
    public void dispose() {
        super.dispose();
        this.start_button.dispose();
    }
}
