package com.plv.uberplayground.actors;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.World;
import com.plv.uberplayground.config.Configuration;

public class PlayerActor extends AnimatedPhysicsActor {
	private static final float CIRCLE_DISTANCE = 1f;
	private static final float CIRCLE_RADIUS = 3f;
	private static final float ANGLE_CHANGE = 180f * MathUtils.PI / 180f;
	private ControlPointActor controlPoint = null;
	private float maxSpeed = 8f;
	private float maxForce = 0.2f;
	private float slowingDistance = 2.3f;
	private float heading;

	//	// The distance to project the wandering circle
	//	private float wanderDistance = 0.1f;
	//	// The radius of the wandering circle
	//	private float wanderRadius = 0.01f;
	//	// Small noise added to the wander target
	//	private float wanderJitter = 0.3f;

	private float wanderRadius = 0.2f;
	private float wanderRatioDeg = 45f * MathUtils.PI / 180;
	private float wanderCurrentAngle = (float) (Math.random() * Math.PI * 2);
	private float wanderAngle = 0;

	public PlayerActor(String animationName, World world, float x, float y, int frameCols, int frameRows){
		super(animationName, world, x,  y, frameCols, frameRows);

		Filter filter = new Filter();
		filter.categoryBits = Configuration.EntityCategory.PLAYER.getValue();
		filter.maskBits = Configuration.EntityCategory.BOUNDARY.getValue();
		this.body.getFixtureList().get(0).setFilterData(filter);
	}

	@Override
	public void act(float delta){
		super.act(delta);

		if (this.controlPoint != null) {
			Vector2 steeringForce = this.Wander();
			this.body.applyForceToCenter(steeringForce, true);
			this.CalculateHeading();
			this.body.setTransform(this.body.getPosition(), this.heading);

			if(this.body.getLinearVelocity().len2() <= 0.1f){
				//Gdx.app.log("000 Vel", "warning");
				//run controlpoint counter
			}
		}
	}

	public ControlPointActor getControlPoint() {
		return this.controlPoint;
	}

	public void setControlPoint(ControlPointActor controlPoint) {
		this.controlPoint = controlPoint;
	}

	public void CalculateHeading(){
		if(this.body.getLinearVelocity().len2() > 0.00000001f){
			this.heading = this.body.getLinearVelocity().angleRad() - MathUtils.PI / 2f;
			while (this.heading < -MathUtils.PI) {
				this.heading += MathUtils.PI2;
			}
			while (this.heading > MathUtils.PI) {
				this.heading -= MathUtils.PI2;
			}
		}
	}

	//returns steering force
	public Vector2 Arrive(){
		// First we get the direction we need to travel in
		Vector2 desiredVelocity = (this.controlPoint.getBody().getPosition().cpy().sub(this.body.getPosition()));
		float distance = desiredVelocity.len();

		float rampedSpeed = this.maxSpeed * (distance / this.slowingDistance);
		rampedSpeed = Math.min(rampedSpeed, this.maxSpeed);
		//No need to normalize because distance is already calculated
		desiredVelocity.scl(rampedSpeed/distance);

		Vector2 steeringForce = new Vector2(0,0);
		// Subtract the current velocity. This is the calibration force
		steeringForce = desiredVelocity.cpy().sub(this.body.getLinearVelocity());
		steeringForce.limit(this.maxForce);

		return steeringForce;
	}

	public Vector2 Seek(){
		// First we get the direction we need to travel in
		Vector2 desiredVelocity = (this.controlPoint.getBody().getPosition().cpy().sub(this.body.getPosition()));
		desiredVelocity.nor();
		desiredVelocity.scl(this.maxSpeed);

		// Subtract the current velocity. This is the calibration force
		Vector2 steeringForce = new Vector2(0,0);
		steeringForce = desiredVelocity.cpy().sub(this.body.getLinearVelocity());
		steeringForce.limit(this.maxForce);

		return steeringForce;
	}

	public Vector2 Wander(){
		// Calculate the circle center
		Vector2 circleCenter = this.body.getLinearVelocity().cpy().nor();
		circleCenter.scl(CIRCLE_DISTANCE);
		//
		// Calculate the displacement force
		Vector2 displacement = new Vector2(0, -1);
		displacement.scl(CIRCLE_RADIUS);
		//
		// Randomly change the vector direction
		// by making it change its current angle
		float len = displacement.len();
		displacement.x = (float) Math.cos(this.wanderAngle) * len;
		displacement.y = (float) Math.sin(this.wanderAngle) * len;
		//
		// Change wanderAngle just a bit, so it
		// won't have the same value in the
		// next game frame.
		double ran = MathUtils.random();
		Gdx.app.log("Random", Double.toString(ran));
		this.wanderAngle += (ran * ANGLE_CHANGE) - (ANGLE_CHANGE * .5f);
		//
		// Finally calculate and return the wander force
		Vector2 wanderForce;
		wanderForce = circleCenter.add(displacement);
		wanderForce.limit(this.maxForce);
		return wanderForce;

		/******************************************************
		 * 
		 ******************************************************/
//		Vector2 circleCenter = this.body.getLinearVelocity().cpy().nor().scl(this.wanderRadius);
//		this.wanderCurrentAngle += (this.wanderRatioDeg * 0.5) - (Math.random() * this.wanderRatioDeg);
//		Vector2 newVector = new Vector2();
//		newVector.x = (float) Math.cos(this.wanderCurrentAngle);
//		newVector.y = (float) Math.sin(this.wanderCurrentAngle);
//		Vector2 wanderForce = newVector;
//		wanderForce.scl(0.2f);
//		circleCenter.limit(0.2f);
//		wanderForce.add(circleCenter);
//		return wanderForce;
	}
}
