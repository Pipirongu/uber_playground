package com.plv.uberplayground.actors;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.plv.uberplayground.config.Configuration;

public class PlayerActor extends AnimatedPhysicsActor {
	private ControlPointActor controlPoint = null;
	private float maxSpeed = 8f;
	private float maxForce = 0.5f;
	private float slowingDistance = 2.3f;
	private float heading;

	//Wander vars
	private static final float CIRCLE_DISTANCE = 0.5f;
	private static final float CIRCLE_RADIUS = 0.6f;
	private static final float ANGLE_CHANGE = 15f * MathUtils.PI / 180f;
	private float wanderAngle = 0;
	private Vector2 vWanderTargert = new Vector2();

	private ShapeRenderer shapeRenderer = new ShapeRenderer();

	//Wall avoidance vars
	private Array<Vector2> feelers;
	private float wallDetectionFeelerLength = 1f;

	public PlayerActor(String animationName, World world, float x, float y, int frameCols, int frameRows){
		super(animationName, world, x,  y, frameCols, frameRows);

		Filter filter = new Filter();
		filter.categoryBits = Configuration.EntityCategory.PLAYER.getValue();
		filter.maskBits = Configuration.EntityCategory.BOUNDARY.getValue();
		this.body.getFixtureList().get(0).setFilterData(filter);

		this.feelers = new Array<Vector2>(3);
	}

	@Override
	public void act(float delta){
		super.act(delta);

		if (this.controlPoint != null) {
			Vector2 wallSteering = this.WallAvoidance(Configuration.Walls).scl(500f);
			Vector2 wanderSteering = this.Wander().scl(1f);

			Vector2 steeringForce = (wallSteering.cpy().add(wanderSteering)).limit(this.maxForce);
			Gdx.app.log("smomethign", Float.toString(steeringForce.len()));

			//this.WallAvoidance(Configuration.Walls);
			for(int i = 0;i<Configuration.Walls.size;i++){
				Configuration.Walls.get(i).Render(this.shapeRenderer, this.getStage().getCamera(), true);
			}
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

	public Vector2 Seek(Vector2 target){
		// First we get the direction we need to travel in
		Vector2 desiredVelocity = target.sub(this.body.getPosition());//(this.controlPoint.getBody().getPosition().cpy().sub(this.body.getPosition()));
		desiredVelocity.nor();
		desiredVelocity.scl(this.maxSpeed);

		// Subtract the current velocity. This is the calibration force
		Vector2 steeringForce = new Vector2(0,0);
		steeringForce = desiredVelocity.cpy().sub(this.body.getLinearVelocity());
		steeringForce.limit(this.maxForce);

		return steeringForce;
	}

	public Vector2 Wander(){

		// Add a random vector to the vWanderTarget
		this.vWanderTargert.x += (MathUtils.random() * 2 - 1) * 200f * 0.001f;
		this.vWanderTargert.y += (MathUtils.random() * 2 - 1) * 200f * 0.001f;

		// Then normalize it and scale it to the wanderRadius
		this.vWanderTargert.nor().scl(CIRCLE_RADIUS);

		// Set vForce to the current heading, scale it wanderDistance, add the vWanderTarget and subtract the velocity
		Vector2 wanderForce = ((this.body.getLinearVelocity().cpy().nor()).scl(CIRCLE_DISTANCE).add(this.vWanderTargert).sub(this.body.getLinearVelocity().cpy()));

		//move the target in front of the character
		Vector2 heading = this.body.getPosition().cpy().sub(this.vWanderTargert);
		Vector2 right = new Vector2(-heading.y, heading.x).nor();
		Vector2 targetPosition = (this.body.getPosition().cpy().add(right.cpy().scl(CIRCLE_DISTANCE))).add(this.vWanderTargert);

		if(Configuration.isDebug){
			//debug draw
			this.shapeRenderer.setColor(Color.RED);
			this.shapeRenderer.begin(ShapeType.Line);
			Matrix4 mat = this.getStage().getCamera().combined.cpy();
			this.shapeRenderer.setProjectionMatrix(mat);
			Vector2 circleCenter = (this.body.getLinearVelocity().cpy().nor()).scl(CIRCLE_DISTANCE).add(this.body.getPosition());
			this.shapeRenderer.circle(circleCenter.x, circleCenter.y, CIRCLE_RADIUS, 20);
			this.shapeRenderer.end();

			this.shapeRenderer.setColor(Color.RED);
			this.shapeRenderer.begin(ShapeType.Line);
			this.shapeRenderer.setProjectionMatrix(mat);
			this.shapeRenderer.line(circleCenter, circleCenter.cpy().add(this.vWanderTargert));
			this.shapeRenderer.end();

			this.shapeRenderer.setColor(Color.GREEN);
			this.shapeRenderer.begin(ShapeType.Filled);
			this.shapeRenderer.setProjectionMatrix(mat);
			this.shapeRenderer.circle((circleCenter.cpy().add(this.vWanderTargert)).x, (circleCenter.cpy().add(this.vWanderTargert)).y, 0.1f, 20);
			this.shapeRenderer.end();
		}
		return wanderForce;
		//return this.Seek(targetPosition);

		//		// Calculate the circle center
		//		Vector2 circleCenter = this.body.getLinearVelocity().cpy().nor();
		//		circleCenter.scl(CIRCLE_DISTANCE);
		//		circleCenter.add(this.body.getPosition());
		//
		//		shapeRenderer.setColor(Color.RED);
		//		shapeRenderer.begin(ShapeType.Line);
		//		Matrix4 mat = this.getStage().getCamera().combined.cpy();
		//		shapeRenderer.setProjectionMatrix(mat);
		//		shapeRenderer.circle(circleCenter.x, circleCenter.y, CIRCLE_RADIUS, 20);
		//		shapeRenderer.end();
		//		//
		//		// Calculate the displacement force
		//		Vector2 displacement = new Vector2(0, -1);
		//		displacement.scl(CIRCLE_RADIUS);
		//		//
		//		// Randomly change the vector direction
		//		// by making it change its current angle
		//		float len = displacement.len();
		//		displacement.x = (float) Math.cos(this.wanderAngle) * len;
		//		displacement.y = (float) Math.sin(this.wanderAngle) * len;
		//		//
		//		// Change wanderAngle just a bit, so it
		//		// won't have the same value in the
		//		// next game frame.
		//		double ran = MathUtils.random();
		//		this.wanderAngle += (ran * ANGLE_CHANGE) - (ANGLE_CHANGE * .5f);
		//		//
		//		shapeRenderer.setColor(Color.RED);
		//		shapeRenderer.begin(ShapeType.Line);
		//		shapeRenderer.setProjectionMatrix(mat);
		//		shapeRenderer.line(circleCenter, circleCenter.cpy().add(displacement));
		//		shapeRenderer.end();
		//
		//		shapeRenderer.setColor(Color.GREEN);
		//		shapeRenderer.begin(ShapeType.Filled);
		//		shapeRenderer.setProjectionMatrix(mat);
		//		shapeRenderer.circle((circleCenter.cpy().add(displacement)).x, (circleCenter.cpy().add(displacement)).y, 0.1f, 20);
		//		shapeRenderer.end();
		//
		//		// Finally calculate and return the wander force
		//		Vector2 wanderForce;
		//		wanderForce = circleCenter.cpy().add(displacement);
		//		//wanderForce.sub(this.body.getLinearVelocity().cpy());
		//		wanderForce.limit(this.maxForce);
		//		return wanderForce;

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

	//compares two real numbers. Returns true if they are equal
	private Boolean isEqual(float a, float b)
	{
		if (Math.abs((a-b)) < 1E-12)
		{
			return true;
		}
		return false;
	}

	private float Vec2DDistance(Vector2 v1, Vector2 v2)
	{
		float ySeparation = v2.y - v1.y;
		float xSeparation = v2.x - v1.x;

		return (float) Math.sqrt((ySeparation*ySeparation + xSeparation*xSeparation));
	}

	private Boolean LineIntersection2D(Vector2 A, Vector2 B, Vector2 C, Vector2 D, float dist, Vector2 point)
	{

		float rTop = (A.y-C.y)*(D.x-C.x)-(A.x-C.x)*(D.y-C.y);
		float rBot = (B.x-A.x)*(D.y-C.y)-(B.y-A.y)*(D.x-C.x);

		float sTop = (A.y-C.y)*(B.x-A.x)-(A.x-C.x)*(B.y-A.y);
		float sBot = (B.x-A.x)*(D.y-C.y)-(B.y-A.y)*(D.x-C.x);

		if ( (rBot == 0) || (sBot == 0))
		{
			//lines are parallel
			return false;
		}

		float r = rTop/rBot;
		float s = sTop/sBot;

		if( (r > 0) && (r < 1) && (s > 0) && (s < 1) )
		{
			dist = this.Vec2DDistance(A,B) * r;

			point = A.add(((B.sub(A))).scl(r));

			return true;
		}

		else
		{
			dist = 0;

			return false;
		}
	}

	public Vector2 WallAvoidance(Array<Wall> walls){ //input wall array
		//create feelers 3x
		this.CreateFeelers();

		//debug draw feelers
		this.shapeRenderer.setColor(Color.BLUE);
		this.shapeRenderer.begin(ShapeType.Line);
		Matrix4 mat = this.getStage().getCamera().combined.cpy();
		this.shapeRenderer.setProjectionMatrix(mat);
		this.shapeRenderer.line(this.body.getPosition(),this.feelers.get(0));
		this.shapeRenderer.end();

		this.shapeRenderer.setColor(Color.BLUE);
		this.shapeRenderer.begin(ShapeType.Line);
		this.shapeRenderer.setProjectionMatrix(mat);
		this.shapeRenderer.line(this.body.getPosition(),this.feelers.get(1));
		this.shapeRenderer.end();

		this.shapeRenderer.setColor(Color.BLUE);
		this.shapeRenderer.begin(ShapeType.Line);
		this.shapeRenderer.setProjectionMatrix(mat);
		this.shapeRenderer.line(this.body.getPosition(),this.feelers.get(2));
		this.shapeRenderer.end();


		//wall avoidance code starts here
		float DistToThisIP    = 0.0f;
		float DistToClosestIP = Float.MAX_VALUE;

		//this will hold an index into the vector of walls
		int ClosestWall = -1;

		Vector2 SteeringForce = new Vector2();
		Vector2 point = new Vector2();      //used for storing temporary info
		Vector2 ClosestPoint = new Vector2();  //holds the closest intersection point

		//examine each feeler in turn
		for (int flr=0; flr<this.feelers.size; ++flr)
		{
			//run through each wall checking for any intersection points
			for (int w=0; w<walls.size; ++w)
			{
				if (this.LineIntersection2D(this.body.getPosition(), this.feelers.get(flr), walls.get(w).From(), walls.get(w).To(), DistToThisIP, point))
				{
					//is this the closest found so far? If so keep a record
					if (DistToThisIP < DistToClosestIP)
					{
						DistToClosestIP = DistToThisIP;
						ClosestWall = w;
						ClosestPoint = point;
					}
				}
			}//next wall


			//if an intersection point has been detected, calculate a force
			//that will direct the agent away
			if (ClosestWall >=0)
			{
				//calculate by what distance the projected position of the agent
				//will overshoot the wall
				Vector2 OverShoot = this.feelers.get(flr).cpy().sub(ClosestPoint);

				//create a force in the direction of the wall normal, with a
				//magnitude of the overshoot
				SteeringForce = walls.get(ClosestWall).Normal().cpy().scl(OverShoot.len());
			}

		}//next feeler

		return SteeringForce;
	}

	/*
	 * Create feelers for Wall Avoidance
	 *
	 * */
	private void CreateFeelers(){
		this.feelers.clear();
		float HalfPi = MathUtils.PI/2f;

		Vector2 agent_position = this.body.getPosition().cpy();
		Vector2 agent_heading = this.body.getLinearVelocity().cpy().nor();
		//feeler pointing straight in front
		this.feelers.insert(0, agent_position.cpy().add(agent_heading.cpy().scl(this.wallDetectionFeelerLength)));

		//feeler to left
		Vector2 temp = agent_heading.cpy();
		this.Vec2DRotateAroundOrigin(temp, HalfPi * 3.5f);
		this.feelers.insert(1, agent_position.cpy().add(temp.cpy().scl(this.wallDetectionFeelerLength)));

		//feeler to right
		temp = agent_heading.cpy();
		this.Vec2DRotateAroundOrigin(temp, HalfPi * 0.5f);
		this.feelers.insert(2, agent_position.cpy().add(temp.cpy().scl(this.wallDetectionFeelerLength)));
	}

	/*
	 * Used to rotate Wall Avoidance Feelers around own axis
	 *
	 * */
	private void Vec2DRotateAroundOrigin(Vector2 v, float ang){
		Matrix3 matTransform = new Matrix3();
		matTransform.idt();
		matTransform.rotateRad(ang);

		v.mul(matTransform);

	}
}
