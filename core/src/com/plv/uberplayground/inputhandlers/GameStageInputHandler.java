package com.plv.uberplayground.inputhandlers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.plv.uberplayground.actors.Agent;

public class GameStageInputHandler extends InputListener {
    private World world;
    private Stage gameStage;
    private Vector3 point = new Vector3();
    // used to store the body which collides with touch point
    private Body bodyThatWasHit = null;

    public GameStageInputHandler(final World world, final Stage gameStage){
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
        //check bodyThatWasHit, use it and set it to null else spawn unit
        if(this.bodyThatWasHit != null){
            //do something
            this.bodyThatWasHit.setLinearVelocity(0.f,this.bodyThatWasHit.getLinearVelocity().y+0.1f);
            this.bodyThatWasHit = null;
            Gdx.app.log("GameStage: ", "Handle Body");
        }else{
            //spawn someting
            Agent spawnUnit = new Agent(this.world,this.point.x,this.point.y);
            this.gameStage.addActor(spawnUnit);
            Gdx.app.log("GameStage: ", "Handle Unit");
        }
    }

}
