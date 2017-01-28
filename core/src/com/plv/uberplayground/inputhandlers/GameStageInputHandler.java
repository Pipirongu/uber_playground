package com.plv.uberplayground.inputhandlers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.plv.uberplayground.UberPlayground;
import com.plv.uberplayground.actors.AnimatedActor;
import com.plv.uberplayground.actors.AnimatedPhysicsActor;
import com.plv.uberplayground.screens.GameScreen;

public class GameStageInputHandler extends InputListener {
    private UberPlayground app;
    private World world;
    private Stage gameStage;
    private Vector3 point = new Vector3();
    // used to store the body which collides with touch point
    private Body bodyThatWasHit = null;

    public GameStageInputHandler(final UberPlayground app, final World world, final Stage gameStage){
        this.app = app;
        this.world = world;
        this.gameStage = gameStage;
    }

    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        this.point.set(x, y, 0); // Translate to world coordinates.

        QueryCallback callback = new QueryCallback() {
            @Override
            public boolean reportFixture (Fixture fixture) {
                if (fixture.testPoint(point.x, point.y)) {
                    bodyThatWasHit = fixture.getBody();
                    return false;
                } else {
                    return true;
                }
            }
        };

        float someOffset = 0.1f;
        world.QueryAABB(callback, point.x - someOffset, point.y - someOffset, point.x + someOffset, point.y + someOffset);

        //Collision return true to not  handle further events
        if(this.bodyThatWasHit != null) {
            Gdx.app.log("GameStage: ", "Body Found!");
            return true;
        }else{
            Gdx.app.log("GameStage: ", "Spawn a Unit!");
            return true;
        }
    }

    @Override
    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        //handle touching a certain unit
        if(this.bodyThatWasHit != null){
            AnimatedPhysicsActor actor = (AnimatedPhysicsActor)this.bodyThatWasHit.getUserData();
            if(actor.getActorType() == AnimatedPhysicsActor.ActorType.SPAWNUNIT) {
                this.bodyThatWasHit.setLinearVelocity(0.f, this.bodyThatWasHit.getLinearVelocity().y + 0.1f);
            }

            this.bodyThatWasHit = null;
            Gdx.app.log("GameStage: ", "Handle Body");
        }else{
            GameScreen gameScreen = (GameScreen)this.app.getScreen();
            gameScreen.spawnControlPointAtPos(point.x, point.y);
            Gdx.app.log("GameStage: ", "New Control Point");
        }
    }

}
