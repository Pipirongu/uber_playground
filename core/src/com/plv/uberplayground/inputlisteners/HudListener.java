package com.plv.uberplayground.inputlisteners;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

public class HudListener extends InputListener {
    private World world;
    private Vector3 point = new Vector3();
    // Ask the world for bodies within the bounding box.
    private Body bodyThatWasHit = null;

    public HudListener(final World world){
        this.world = world;
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

        if(bodyThatWasHit != null) {
            Gdx.app.log("HUDAgent: ", "Touch Down!");
            bodyThatWasHit = null;
            return true;
        }

        return false;
    }

    @Override
    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        Gdx.app.log("HUDAgent: ", "Touch Released!");
    }

}
