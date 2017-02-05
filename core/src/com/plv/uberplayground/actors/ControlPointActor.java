package com.plv.uberplayground.actors;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.World;
import com.plv.uberplayground.config.Configuration;

public class ControlPointActor extends AnimatedPhysicsActor {
	public ControlPointActor(String animationName, World world, float x, float y, int frameCols, int frameRows) {
		super(animationName, world, x, y, frameCols, frameRows);

		this.body.setType(BodyDef.BodyType.StaticBody);
		//this.body.getFixtureList().get(0).setSensor(true);

		Filter filter = new Filter();
		filter.categoryBits = Configuration.EntityCategory.CONTROL_POINT.getValue();
		filter.maskBits = (short) (Configuration.EntityCategory.PLAYER.getValue() | Configuration.EntityCategory.BOUNDARY.getValue());
		this.body.getFixtureList().get(0).setFilterData(filter);
	}
}
