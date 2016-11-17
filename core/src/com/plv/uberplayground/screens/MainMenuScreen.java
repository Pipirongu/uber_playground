package com.plv.uberplayground.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.utils.Align;
import com.plv.uberplayground.util.UtilFactory;

public class MainMenuScreen extends AbstractScreen{
    private static final int VIRTUAL_WIDTH = 1280;
    private static final int VIRTUAL_HEIGHT = 720;
    private static final float ASPECT_RATIO = (float)VIRTUAL_WIDTH/(float)VIRTUAL_HEIGHT;
    private Texture start_button;

    public MainMenuScreen() {
        super();
        this.start_button = new Texture( Gdx.files.internal("ui/btn_play.png") );
        this.getViewport().setCamera(new OrthographicCamera(VIRTUAL_WIDTH, VIRTUAL_HEIGHT));
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
    public void resize(int width, int height) {
        // calculate new viewport
        float aspectRatio = (float)width/(float)height;
        float scale = 1f;
        Vector2 crop = new Vector2(0f, 0f);

        if(aspectRatio > ASPECT_RATIO)
        {
            scale = (float)height/(float)VIRTUAL_HEIGHT;
            crop.x = (width - VIRTUAL_WIDTH*scale)/2f;
        }
        else if(aspectRatio < ASPECT_RATIO)
        {
            scale = (float)width/(float)VIRTUAL_WIDTH;
            crop.y = (height - VIRTUAL_HEIGHT*scale)/2f;
        }
        else
        {
            scale = (float)width/(float)VIRTUAL_WIDTH;
        }

        float w = (float)VIRTUAL_WIDTH*scale;
        float h = (float)VIRTUAL_HEIGHT*scale;
        Rectangle viewport = new Rectangle(crop.x, crop.y, w, h);

        Gdx.gl.glViewport((int) viewport.x, (int) viewport.y,
                (int) viewport.width, (int) viewport.height);

        //update camera
        //this.getViewport().setCamera(new OrthographicCamera(VIRTUAL_WIDTH, VIRTUAL_HEIGHT));
    }

    @Override
    public void dispose() {
        super.dispose();
        this.start_button.dispose();
    }
}
