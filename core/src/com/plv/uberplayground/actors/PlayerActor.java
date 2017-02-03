package com.plv.uberplayground.actors;


import com.badlogic.gdx.physics.box2d.World;

public class PlayerActor extends AnimatedPhysicsActor {
    public PlayerActor(ActorType actorType, String animationName, World world, float x, float y, int frameCols, int frameRows){
        super(actorType, animationName, world, x,  y, frameCols, frameRows);
    }
}
