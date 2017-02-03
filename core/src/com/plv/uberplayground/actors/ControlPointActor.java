package com.plv.uberplayground.actors;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;

public class ControlPointActor extends AnimatedPhysicsActor {
    public ControlPointActor(ActorType actorType, String animationName, World world, float x, float y, int frameCols, int frameRows){
        super(actorType, animationName, world, x,  y, frameCols, frameRows);   
        
        this.body.setType(BodyDef.BodyType.StaticBody);
        this.body.getFixtureList().get(0).setSensor(true);
    }
}
