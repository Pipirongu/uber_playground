package com.plv.uberplayground.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.plv.uberplayground.UberPlayground;

public class MainMenuScreen implements Screen {
    private Texture start_button;
    private Stage stage;
    private final UberPlayground app;

    public MainMenuScreen(final UberPlayground app) {
        this.app = app;
        this.stage = new Stage(new FillViewport(this.app.VIRTUAL_WIDTH, this.app.VIRTUAL_HEIGHT, this.app.mainMenuCamera));
        Gdx.input.setInputProcessor(stage);

        //
        this.start_button = new Texture( Gdx.files.internal("ui/btn_play.png") );
        ImageButton btnPlay = new ImageButton(new TextureRegionDrawable(new TextureRegion(this.start_button)));
        btnPlay.setPosition(this.app.VIRTUAL_WIDTH / 2, this.app.VIRTUAL_HEIGHT/2, Align.center);
        this.stage.addActor(btnPlay);

        // Setting listeners
        btnPlay.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                //change to game screen
                app.setScreen(new GameScreen(app));
                return false;
            }
        });
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        //clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Calling to Stage methods
        this.stage.act(delta);
        this.stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        // calculate new viewport
        float aspectRatio = (float)width/(float)height;
        float scale = 1f;
        Vector2 crop = new Vector2(0f, 0f);

        if(aspectRatio > this.app.ASPECT_RATIO)
        {
            scale = (float)height/(float)this.app.VIRTUAL_HEIGHT;
            crop.x = (width - this.app.VIRTUAL_WIDTH*scale)/2f;
        }
        else if(aspectRatio < this.app.ASPECT_RATIO)
        {
            scale = (float)width/(float)this.app.VIRTUAL_WIDTH;
            crop.y = (height - this.app.VIRTUAL_HEIGHT*scale)/2f;
        }
        else
        {
            scale = (float)width/(float)this.app.VIRTUAL_WIDTH;
        }

        float w = (float)this.app.VIRTUAL_WIDTH*scale;
        float h = (float)this.app.VIRTUAL_HEIGHT*scale;
        Rectangle viewport = new Rectangle(crop.x, crop.y, w, h);

        Gdx.gl.glViewport((int) viewport.x, (int) viewport.y,
                (int) viewport.width, (int) viewport.height);

        this.stage.getViewport().update((int)w, (int)h, true);
        //update camera
        //this.getViewport().setCamera(new OrthographicCamera(VIRTUAL_WIDTH, VIRTUAL_HEIGHT));
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        this.start_button.dispose();
        this.stage.dispose();
    }
}
