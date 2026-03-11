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
	private float maxSpeed;
	private float maxForce;
	private float slowingDistance;

	//Wander vars
	private float circleDistance;
	private float circleRadius;
	private static final float ANGLE_CHANGE = 15f * MathUtils.PI / 180f;
	private float wanderAngle = 0;
	private Vector2 vWanderTargert = new Vector2();

	private ShapeRenderer shapeRenderer = new ShapeRenderer();

	//Wall avoidance vars
	private Array<Vector2> feelers;
	private float wallDetectionFeelerLength;
	
	private Vector2 steeringForce = new Vector2();
	
	private float heading;
	private float wanderOrientation = 0;
	private float accumulatedRotation = 0; // Track total rotation to prevent full circles
	private float previousHeading = 0; // Track previous heading to measure rotation
	private Smoother smoother = new Smoother(1, new Vector2(0f, 0f));
	
	// Enemy-specific properties
	private boolean isEnemy = false;
	private com.plv.uberplayground.enemies.EnemyHealthBar healthBar = null;
	private com.plv.uberplayground.enemies.EnemyBehaviorLabel behaviorLabel = null;
	private com.plv.uberplayground.enemies.HealthComponent healthComponent = null;
	private com.plv.uberplayground.enemies.BehaviorState behaviorState = null;
	private Array<Vector2> patrolWaypoints = null;
	private int currentWaypointIndex = 0;
	private float orbitAngle = 0.0f; // For orbit behavior

	public PlayerActor(String animationName, World world, float x, float y, int frameCols, int frameRows){
		super(animationName, world, x,  y, frameCols, frameRows);
		
		// Initialize with default scale of 1.0
		this.maxSpeed = 200f;
		this.maxForce = 20.0f;
		this.slowingDistance = 2.3f;
		this.wallDetectionFeelerLength = 1.5f; // Increased from 1f for better warning distance
		this.circleDistance = 0.5f;
		this.circleRadius = 0.6f;

		Filter filter = new Filter();
		filter.categoryBits = Configuration.EntityCategory.PLAYER.getValue();
		filter.maskBits = Configuration.EntityCategory.BOUNDARY.getValue();
		this.body.getFixtureList().get(0).setFilterData(filter);

		// Reduce damping to make the body more responsive to direction changes
		this.body.setLinearDamping(0.01f);
		this.body.setAngularDamping(0.01f);

		this.feelers = new Array<Vector2>(3);
	}

	public PlayerActor(String animationName, World world, float x, float y, int frameCols, int frameRows, float scale){
		super(animationName, world, x,  y, frameCols, frameRows, scale);
		
		// Scale the forces proportionally to the body mass
		// Mass = density × area, and area scales with scale^2
		// So mass scales with scale^2, and forces need to scale accordingly
		float massScale = scale * scale; // Scale^2 for area/mass compensation
		this.maxSpeed = 200f * scale;
		this.maxForce = 20.0f * massScale;
		this.slowingDistance = 2.3f * scale;
		this.wallDetectionFeelerLength = 1.5f * scale; // Increased from 1f for better warning distance
		this.circleDistance = 0.5f * scale;
		this.circleRadius = 0.6f * scale;

		Filter filter = new Filter();
		filter.categoryBits = Configuration.EntityCategory.PLAYER.getValue();
		filter.maskBits = Configuration.EntityCategory.BOUNDARY.getValue();
		this.body.getFixtureList().get(0).setFilterData(filter);

		// Reduce damping to make the body more responsive to direction changes
		this.body.setLinearDamping(0.01f);
		this.body.setAngularDamping(0.01f);

		this.feelers = new Array<Vector2>(3);
	}

	@Override
	public void act(float delta){
		super.act(delta);

		// Add null check to prevent crashes
		if (this.body == null) {
			Gdx.app.error("PlayerActor", "Cannot act: physics body is null");
			return;
		}

		// Update behavior state for enemies
		if (this.isEnemy && this.behaviorState != null) {
			this.behaviorState.update(delta);
		}

		Vector2 steeringForce = this.CalculateSteeringForce();
		
		// Only render walls if this actor is visible and on stage and not at pooled position
		Vector2 currentPosition = this.body.getPosition();
		boolean isPooledPosition = (currentPosition.x == 0 && currentPosition.y == 0);
		if (this.isVisible() && this.getStage() != null && !isPooledPosition) {
			for(int i = 0;i<Configuration.Walls.size;i++){
				Configuration.Walls.get(i).Render(this.shapeRenderer, this.getStage().getCamera(), true);
			}
		}
		this.body.applyForceToCenter(this.steeringForce, true);
		Vector2 smoothedHeading = this.smoother.Update(this.body.getLinearVelocity().cpy());
		this.CalculateHeading(smoothedHeading);
		this.body.setTransform(this.body.getPosition(), this.heading);
		
		// Render health bar for enemies with performance optimization
		if (this.isEnemy && this.healthBar != null && this.healthComponent != null && this.isVisible() && this.getStage() != null) {
			Vector2 enemyPosition = this.body.getPosition();
			
			// Skip rendering if enemy is at pooled position (0,0)
			boolean isHealthBarPooledPosition = (enemyPosition.x == 0 && enemyPosition.y == 0);
			if (!isHealthBarPooledPosition) {
				float healthPercentage = this.healthComponent.getHealthPercentage();
				
				// Always render health bar for debugging - remove performance optimization temporarily
				this.healthBar.render(this.shapeRenderer, this.getStage().getCamera(), enemyPosition, healthPercentage, true);
			}
		}
		
		// Render behavior label for enemies
		if (this.isEnemy && this.behaviorLabel != null && this.behaviorState != null && this.isVisible() && this.getStage() != null) {
			Vector2 enemyPosition = this.body.getPosition();
			
			// Skip rendering if enemy is at pooled position (0,0)
			boolean isBehaviorLabelPooledPosition = (enemyPosition.x == 0 && enemyPosition.y == 0);
			if (!isBehaviorLabelPooledPosition) {
				com.plv.uberplayground.enemies.SteeringBehaviorType currentBehavior = this.behaviorState.getCurrentType();
				this.behaviorLabel.render(this.shapeRenderer, this.getStage().getCamera(), enemyPosition, currentBehavior, true);
			}
		}
	}

	public float getActorScale() {
		return this.scale;
	}

	public ControlPointActor getControlPoint() {
		return this.controlPoint;
	}

	public void setControlPoint(ControlPointActor controlPoint) {
		this.controlPoint = controlPoint;
	}

	public void CalculateHeading(Vector2 smoothedHeading){
		// Add null check to prevent crashes
		if (smoothedHeading == null) {
			Gdx.app.error("PlayerActor", "Cannot calculate heading: smoothed heading is null");
			return;
		}
		
		//if(this.body.getLinearVelocity().len2() > 0.00000001f){
			this.heading = smoothedHeading.angleRad() - MathUtils.PI / 2f;
			while (this.heading < -MathUtils.PI) {
				this.heading += MathUtils.PI2;
			}
			while (this.heading > MathUtils.PI) {
				this.heading -= MathUtils.PI2;
			}
		//}
	}
	
	public Vector2 CalculateSteeringForce(){
		this.steeringForce.setZero();
		Vector2 force = new Vector2();
		
		//if wall avoidance on
		force = this.WallAvoidance(Configuration.Walls);
		
		// Apply proper force scaling instead of hardcoded 10f multiplier
		if (force.len2() > 0) { // Only scale if there's actually a wall avoidance force
			// Calculate scale-aware force multiplier
			float scaleFactor = this.getActorScale();
			float massScale = scaleFactor * scaleFactor; // Mass scales with scale²
			
			// Base force multiplier proportional to maxForce scaling
			// This maintains consistent steering strength relative to actor size
			float baseMultiplier = this.maxForce / 20.0f; // 20.0f is the base maxForce at scale 1.0
			
			// Velocity-based force modulation to prevent over-reaction at low speeds
			float currentVelocity = this.body.getLinearVelocity().len();
			float maxVelocity = this.maxSpeed;
			float velocityRatio = Math.min(currentVelocity / maxVelocity, 1.0f); // Clamp to [0,1]
			
			// Minimum velocity factor to ensure some response even at very low speeds
			float minVelocityFactor = 0.3f;
			float velocityFactor = minVelocityFactor + (1.0f - minVelocityFactor) * velocityRatio;
			
			// Calculate final force multiplier
			// - baseMultiplier ensures proportional scaling with maxForce
			// - velocityFactor prevents over-reaction at low speeds
			// - Additional scaling factor for wall avoidance strength (equivalent to old 10f but scale-aware)
			float forceMultiplier = baseMultiplier * velocityFactor * 10.0f;
			
			force.scl(forceMultiplier);
		}
		if (!AccumulateSteeringForce(this.steeringForce, force)){
			return force;
		}
		
		// Enemy behavior handling
		if (this.isEnemy && this.behaviorState != null && this.playerTarget != null) {
			com.plv.uberplayground.enemies.SteeringBehaviorType currentBehavior = this.behaviorState.getCurrentType();
			
			switch (currentBehavior) {
				case WANDER:
					force = this.Wander().scl(1f);
					break;
				case SEEK:
					force = this.Seek(this.playerTarget.cpy()).scl(1f);
					break;
				case FLEE:
					force = com.plv.uberplayground.enemies.SteeringBehaviorLibrary.flee(this, this.playerTarget.cpy()).scl(1f);
					break;
				case PATROL:
					force = com.plv.uberplayground.enemies.SteeringBehaviorLibrary.patrol(this, this.patrolWaypoints).scl(1f);
					break;
				case ORBIT:
					float orbitRadius = com.plv.uberplayground.enemies.SpawnConfiguration.DEFAULT_ORBIT_RADIUS;
					force = com.plv.uberplayground.enemies.SteeringBehaviorLibrary.orbit(this, this.playerTarget.cpy(), orbitRadius).scl(1f);
					break;
				case INTERCEPT:
					// For intercept, we need PlayerShip reference - fall back to seek for now
					force = this.Seek(this.playerTarget.cpy()).scl(1f);
					break;
				case FLOCK:
					// Get neighbors for flocking behavior (use cached if available)
					Array<PlayerActor> neighbors = getNearbyEnemies();
					if (neighbors.size > 0) {
						force = com.plv.uberplayground.enemies.SteeringBehaviorLibrary.flock(this, neighbors).scl(1f);
					} else {
						// Fall back to seek if no neighbors
						force = this.Seek(this.playerTarget.cpy()).scl(1f);
					}
					break;
				default:
					force = this.Wander().scl(1f);
					break;
			}
			
			if (!AccumulateSteeringForce(this.steeringForce, force)){
				return force;
			}
		} else {
			// Player behavior (original logic)
			//if control point exists, arrive at it, otherwise wander
			if (this.controlPoint != null) {
				force = this.Arrive().scl(1f);
				if (!AccumulateSteeringForce(this.steeringForce, force)){
					return force;
				}
			} else {
				//if wander on
				force = this.Wander().scl(1f);
				if (!AccumulateSteeringForce(this.steeringForce, force)){
					return force;
				}
			}
		}
		
		return this.steeringForce;
	}

	private Boolean AccumulateSteeringForce(Vector2 totalSteering, Vector2 forceToAdd){	
		//calculate how much steering force the vehicle has used so far
		float MagnitudeSoFar = totalSteering.len();
		//calculate how much steering force remains to be used by this vehicle
		float MagnitudeRemaining = this.maxForce - MagnitudeSoFar;
		//return false if there is no more force left to use
		if (MagnitudeRemaining <= 0.000000001f){
			return false;
		}
		//calculate the magnitude of the force we want to add
		float MagnitudeToAdd = forceToAdd.len();
		//if the magnitude of the sum of ForceToAdd and the running total
		//does not exceed the maximum force available to this vehicle, just
		//add together. Otherwise add as much of the ForceToAdd vector as
		//possible without going over the max.
		if (MagnitudeToAdd < MagnitudeRemaining)
		{
			totalSteering.add(forceToAdd);
		}
		else
		{
			//add it to the steering force
			totalSteering.add(((forceToAdd.cpy().nor()).scl(MagnitudeRemaining)));
		}
		return true;
	}
	
	//returns steering force
	public Vector2 Arrive(){
		// Add null checks to prevent crashes
		if (this.body == null || this.controlPoint == null || this.controlPoint.getBody() == null) {
			Gdx.app.error("PlayerActor", "Cannot perform arrive: body or control point is null");
			return new Vector2(); // Return zero vector
		}
		
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
		// Add null check to prevent crashes
		if (this.body == null || target == null) {
			Gdx.app.error("PlayerActor", "Cannot perform seek: body or target is null");
			return new Vector2(); // Return zero vector
		}
		
		// First we get the direction we need to travel in
		Vector2 desiredVelocity = target.sub(this.body.getPosition());
		desiredVelocity.nor();
		desiredVelocity.scl(this.maxSpeed); // Scale by maxSpeed to get proper force magnitude

		// Subtract the current velocity. This is the calibration force
		Vector2 steeringForce = new Vector2(0,0);
		steeringForce = desiredVelocity.cpy().sub(this.body.getLinearVelocity());
		steeringForce.limit(this.maxForce); // Limit by maxForce to prevent excessive forces

		return steeringForce;
	}

	public Vector2 Wander(){
		// Add null check to prevent crashes
		if (this.body == null) {
			Gdx.app.error("PlayerActor", "Cannot perform wander: body is null");
			return new Vector2(); // Return zero vector
		}
		
		// Track rotation to prevent full circles
		float headingDelta = this.heading - this.previousHeading;
		
		// Normalize the delta to [-π, π]
		while (headingDelta > MathUtils.PI) headingDelta -= MathUtils.PI2;
		while (headingDelta < -MathUtils.PI) headingDelta += MathUtils.PI2;
		
		// Accumulate rotation in the same direction
		if (Math.abs(headingDelta) > 0.01f) {
			if ((accumulatedRotation > 0 && headingDelta > 0) || (accumulatedRotation < 0 && headingDelta < 0)) {
				// Same direction - accumulate
				accumulatedRotation += headingDelta;
			} else {
				// Direction changed - reset accumulator
				accumulatedRotation = headingDelta;
			}
		}
		
		// Calculate bias to gradually push away from continued rotation
		float maxRotation = MathUtils.PI * 0.66f; // About 120 degrees
		float bias = 0;
		
		if (Math.abs(accumulatedRotation) > maxRotation * 0.5f) {
			// Start applying bias when halfway to max rotation
			float excessRotation = Math.abs(accumulatedRotation) - (maxRotation * 0.5f);
			float biasStrength = excessRotation / (maxRotation * 0.5f); // 0 to 1
			// Apply smooth bias in opposite direction
			bias = -Math.signum(accumulatedRotation) * biasStrength * 0.15f;
		}
		
		// Normal wander behavior with bias applied
		wanderOrientation += RandomBinomial() * 1.5f + bias;
		
		// Reset accumulator if we've successfully changed direction
		if (Math.abs(accumulatedRotation) > maxRotation && 
		    Math.signum(accumulatedRotation) != Math.signum(wanderOrientation)) {
			accumulatedRotation = 0;
		}
		
		// Normalize to [-π, π] range to prevent unbounded accumulation
		wanderOrientation = ((wanderOrientation + MathUtils.PI) % MathUtils.PI2 + MathUtils.PI2) % MathUtils.PI2 - MathUtils.PI;
		
		// Store current heading for next frame
		this.previousHeading = this.heading;
		
		// Use the body's current heading for stable forward direction
		float angle = this.heading + MathUtils.PI / 2f; // Add PI/2 because heading is offset
		Vector2 forwardDirection = new Vector2(MathUtils.cos(angle), MathUtils.sin(angle));
		
		// Calculate circle center in front of the actor
		Vector2 circleCenter = this.body.getPosition().cpy().add(forwardDirection.cpy().scl(this.circleDistance));
		
		// Calculate wander target on the circle
		Vector2 wanderDirection = new Vector2(MathUtils.cos(angle + wanderOrientation), MathUtils.sin(angle + wanderOrientation));
		Vector2 targetPosition = circleCenter.cpy().add(wanderDirection.cpy().scl(this.circleRadius));
		
		if(Configuration.isDebug){
			// Skip debug rendering if enemy is at pooled position (0,0)
			Vector2 currentPosition = this.body.getPosition();
			boolean isPooledPosition = (currentPosition.x == 0 && currentPosition.y == 0);
			
			if (!isPooledPosition) {
				//debug draw - red circle shows wander circle, green dot shows target
				this.shapeRenderer.setColor(Color.RED);
				this.shapeRenderer.begin(ShapeType.Line);
				Matrix4 mat = this.getStage().getCamera().combined.cpy();
				this.shapeRenderer.setProjectionMatrix(mat);
				this.shapeRenderer.circle(circleCenter.x, circleCenter.y, this.circleRadius, 20);
				this.shapeRenderer.end();

				this.shapeRenderer.setColor(Color.GREEN);
				this.shapeRenderer.begin(ShapeType.Filled);
				this.shapeRenderer.setProjectionMatrix(mat);
				this.shapeRenderer.circle(targetPosition.x, targetPosition.y, 0.1f * scale, 20);
				this.shapeRenderer.end();
			}
		}
		
		return this.Seek(targetPosition);

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

	public Vector2 WallAvoidance(Array<Wall> walls){ //input wall array
		//create feelers 3x
		this.CreateFeelers();
		
		// Add null check to prevent crashes if body is null
		if (this.body == null || this.feelers.size == 0) {
			Gdx.app.error("PlayerActor", "Cannot perform wall avoidance: physics body is null or no feelers created");
			return new Vector2(); // Return zero vector
		}
	
		//debug draw feelers
		// Debug draw feelers - only if not at pooled position
		Vector2 currentPosition = this.body.getPosition();
		boolean isPooledPosition = (currentPosition.x == 0 && currentPosition.y == 0);
		
		if (!isPooledPosition) {
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
		}
	
	
		//wall avoidance code starts here
		float DistToThisIP    = 0.0f;
		float DistToClosestIP = Float.MAX_VALUE;
	
		//this will hold an index into the vector of walls
		int ClosestWall = -1;
		int ClosestFeeler = -1; // Track which feeler found the closest intersection
	
		Vector2 SteeringForce = new Vector2();
		Vector2 point = new Vector2();      //used for storing temporary info
		Vector2 ClosestPoint = new Vector2();  //holds the closest intersection point
	
		//examine each feeler in turn to find the closest intersection across ALL feelers
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
						ClosestFeeler = flr;
						ClosestPoint = point.cpy(); // Make a copy to avoid reference issues
					}
				}
			}//next wall
		}//next feeler
	
		//NOW calculate steering force ONCE using the closest intersection found across all feelers
		if (ClosestWall >= 0 && ClosestFeeler >= 0)
		{
			//calculate by what distance the projected position of the agent
			//will overshoot the wall (using the feeler that found the closest intersection)
			Vector2 OverShoot = this.feelers.get(ClosestFeeler).cpy().sub(ClosestPoint);
	
			//create a force in the direction of the wall normal, with a
			//magnitude of the overshoot
			SteeringForce = walls.get(ClosestWall).Normal().cpy().scl(OverShoot.len());
		}
	
		return SteeringForce;
	}

	/*
	 * Create feelers for Wall Avoidance
	 *
	 * */
	private void CreateFeelers(){
		this.feelers.clear();
		
		// Add null check to prevent crashes
		if (this.body == null) {
			Gdx.app.error("PlayerActor", "Cannot create feelers: physics body is null");
			return;
		}
	
		Vector2 agent_position = this.body.getPosition().cpy();
		Vector2 agent_heading = this.body.getLinearVelocity().cpy().nor();
		
		//feeler pointing straight in front (0° relative to heading)
		this.feelers.insert(0, agent_position.cpy().add(agent_heading.cpy().scl(this.wallDetectionFeelerLength)));
	
		//feeler to left (-25° from heading for proper forward path sampling)
		Vector2 temp = agent_heading.cpy();
		this.Vec2DRotateAroundOrigin(temp, -MathUtils.PI / 7.2f); // -25° = -π/7.2
		this.feelers.insert(1, agent_position.cpy().add(temp.cpy().scl(this.wallDetectionFeelerLength)));
	
		//feeler to right (+25° from heading for proper forward path sampling)
		temp = agent_heading.cpy();
		this.Vec2DRotateAroundOrigin(temp, MathUtils.PI / 7.2f); // +25° = +π/7.2
		this.feelers.insert(2, agent_position.cpy().add(temp.cpy().scl(this.wallDetectionFeelerLength)));
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
	
	/* Returns a random number between -1 and 1. Values around zero are more likely. */
	private float RandomBinomial() {
		return MathUtils.random() - MathUtils.random();
	}
	
	/**
	 * Sets this PlayerActor as an enemy and initializes all enemy-specific components.
	 * @param isEnemy true to mark as enemy, false for player
	 */
	public void setIsEnemy(boolean isEnemy) {
		this.isEnemy = isEnemy;
		if (isEnemy) {
			// Initialize enemy-specific components
			if (this.healthBar == null) {
				this.healthBar = new com.plv.uberplayground.enemies.EnemyHealthBar();
			}
			if (this.behaviorLabel == null) {
				this.behaviorLabel = new com.plv.uberplayground.enemies.EnemyBehaviorLabel();
			}
			if (this.healthComponent == null) {
				this.healthComponent = new com.plv.uberplayground.enemies.HealthComponent(
					com.plv.uberplayground.enemies.SpawnConfiguration.ENEMY_HEALTH);
			}
			if (this.behaviorState == null) {
				this.behaviorState = new com.plv.uberplayground.enemies.BehaviorState();
			}
			if (this.patrolWaypoints == null) {
				this.patrolWaypoints = new Array<Vector2>();
			}
			
			// Configure collision category for enemy
			if (this.body != null && this.body.getFixtureList().size > 0) {
				Filter filter = new Filter();
				filter.categoryBits = Configuration.EntityCategory.ENEMY_SHIP.getValue();
				filter.maskBits = (short) (Configuration.EntityCategory.BOUNDARY.getValue() | 
										   Configuration.EntityCategory.PLAYER_SHIP.getValue() | 
										   Configuration.EntityCategory.PROJECTILE.getValue());
				this.body.getFixtureList().get(0).setFilterData(filter);
				
				// Set user data for collision detection
				this.body.setUserData(this);
			}
		} else {
			// Clear enemy components for player
			this.healthBar = null;
			this.behaviorLabel = null;
			this.healthComponent = null;
			this.behaviorState = null;
			this.patrolWaypoints = null;
			
			// Reset collision category to player
			if (this.body != null && this.body.getFixtureList().size > 0) {
				Filter filter = new Filter();
				filter.categoryBits = Configuration.EntityCategory.PLAYER.getValue();
				filter.maskBits = Configuration.EntityCategory.BOUNDARY.getValue();
				this.body.getFixtureList().get(0).setFilterData(filter);
			}
		}
	}
	
	/**
	 * Returns whether this PlayerActor is an enemy.
	 * @return true if this is an enemy, false if it's a player
	 */
	public boolean isEnemy() {
		return this.isEnemy;
	}
	
	/**
	 * Gets the health bar instance for this enemy.
	 * @return EnemyHealthBar instance, or null if not an enemy
	 */
	public com.plv.uberplayground.enemies.EnemyHealthBar getHealthBar() {
		return this.healthBar;
	}
	
	/**
	 * Gets the behavior label instance for this enemy.
	 * @return EnemyBehaviorLabel instance, or null if not an enemy
	 */
	public com.plv.uberplayground.enemies.EnemyBehaviorLabel getBehaviorLabel() {
		return this.behaviorLabel;
	}
	
	/**
	 * Gets the health component for this enemy.
	 * @return HealthComponent instance, or null if not an enemy
	 */
	public com.plv.uberplayground.enemies.HealthComponent getHealthComponent() {
		return this.healthComponent;
	}
	
	/**
	 * Gets the behavior state for this enemy.
	 * @return BehaviorState instance, or null if not an enemy
	 */
	public com.plv.uberplayground.enemies.BehaviorState getBehaviorState() {
		return this.behaviorState;
	}
	
	/**
	 * Gets the patrol waypoints for this enemy.
	 * @return Array of waypoints, or null if not an enemy
	 */
	public Array<Vector2> getPatrolWaypoints() {
		return this.patrolWaypoints;
	}
	
	/**
	 * Sets the patrol waypoints for this enemy.
	 * @param waypoints Array of Vector2 waypoints
	 */
	public void setPatrolWaypoints(Array<Vector2> waypoints) {
		if (this.isEnemy) {
			this.patrolWaypoints = waypoints;
			this.currentWaypointIndex = 0;
		}
	}
	
	/**
	 * Gets the current waypoint index for patrol behavior.
	 * @return Current waypoint index
	 */
	public int getCurrentWaypointIndex() {
		return this.currentWaypointIndex;
	}
	
	/**
	 * Sets the current waypoint index for patrol behavior.
	 * @param index The waypoint index to set
	 */
	public void setCurrentWaypointIndex(int index) {
		this.currentWaypointIndex = index;
	}
	
	/**
	 * Gets the current orbit angle for orbit behavior.
	 * @return Current orbit angle in radians
	 */
	public float getOrbitAngle() {
		return this.orbitAngle;
	}
	
	/**
	 * Sets the orbit angle for orbit behavior.
	 * @param angle The orbit angle in radians
	 */
	public void setOrbitAngle(float angle) {
		this.orbitAngle = angle;
	}
	/**
	 * Gets the maximum speed for this actor.
	 * @return Maximum speed value
	 */
	public float getMaxSpeed() {
		return this.maxSpeed;
	}

	/**
	 * Gets the maximum force for this actor.
	 * @return Maximum force value
	 */
	public float getMaxForce() {
		return this.maxForce;
	}

	/**
	 * Gets the Box2D body for this actor.
	 * @return The Box2D body
	 */
	public com.badlogic.gdx.physics.box2d.Body getBody() {
		return this.body;
	}


	// Player target for enemy behaviors
	private Vector2 playerTarget = null;

	/**
	 * Sets the player target position for enemy behaviors.
	 * @param playerPosition The current player position
	 */
	public void setPlayerTarget(Vector2 playerPosition) {
		if (this.isEnemy) {
			this.playerTarget = playerPosition != null ? playerPosition.cpy() : null;
		}
	}

	/**
	 * Gets the current player target position.
	 * @return Player target position, or null if not set
	 */
	public Vector2 getPlayerTarget() {
		return this.playerTarget;
	}

	/**
	 * Gets nearby enemy neighbors for flocking behavior.
	 * Searches through all actors in the stage to find other enemies within flocking range.
	 * @return Array of nearby enemy PlayerActors for flocking coordination
	 */
	private Array<PlayerActor> getFlockingNeighbors() {
		Array<PlayerActor> neighbors = new Array<>();

		if (this.getStage() == null || !this.isEnemy) {
			return neighbors;
		}

		float flockingRange = 200.0f; // Maximum distance for flocking coordination
		Vector2 myPosition = this.getBody().getPosition();

		// Search through all actors in the stage
		for (com.badlogic.gdx.scenes.scene2d.Actor actor : this.getStage().getActors()) {
			if (actor instanceof PlayerActor) {
				PlayerActor otherActor = (PlayerActor) actor;

				// Skip self and non-enemies
				if (otherActor == this || !otherActor.isEnemy()) {
					continue;
				}

				// Check if within flocking range
				Vector2 otherPosition = otherActor.getBody().getPosition();
				float distance = myPosition.dst(otherPosition);

				if (distance <= flockingRange) {
					neighbors.add(otherActor);
				}
			}
		}

		return neighbors;
	}
	
	// Nearby enemies cache for performance optimization
	private Array<PlayerActor> nearbyEnemiesCache = null;
	
	/**
	 * Sets the nearby enemies cache for flocking behavior optimization.
	 * This is used by the EnemyManager's spatial partitioning system.
	 * @param nearbyEnemies Array of nearby enemies
	 */
	public void setNearbyEnemies(Array<PlayerActor> nearbyEnemies) {
		this.nearbyEnemiesCache = nearbyEnemies;
	}
	
	/**
	 * Gets the cached nearby enemies or falls back to stage search.
	 * @return Array of nearby enemies for flocking behavior
	 */
	public Array<PlayerActor> getNearbyEnemies() {
		if (this.nearbyEnemiesCache != null) {
			return this.nearbyEnemiesCache;
		} else {
			return getFlockingNeighbors();
		}
	}
	
	/**
	 * Sets the health percentage from an external health system.
	 * This is used to synchronize the health bar display with the centralized health system.
	 * @param healthPercentage The current health percentage (0.0 to 1.0)
	 */
	public void setHealthPercentage(float healthPercentage) {
		if (this.healthComponent != null) {
			// Update the local health component to match the external health system
			int newHealth = Math.round(healthPercentage * this.healthComponent.getMaxHealth());
			int currentHealth = this.healthComponent.getCurrentHealth();
			
			// Apply damage to sync with external system
			if (newHealth < currentHealth) {
				int damageAmount = currentHealth - newHealth;
				this.healthComponent.takeDamage(damageAmount, 0.0f);
			}
		}
	}
	
	/**
	 * Override remove to ensure proper cleanup of physics body and resources.
	 * This is called when the actor is removed from the stage.
	 */
	@Override
	public boolean remove() {
		// Debug logging for enemy removal
		if (this.isEnemy) {
			Gdx.app.log("EnemyCleanup", "Removing enemy PlayerActor - deferring physics body cleanup");
		}
		
		// Don't dispose physics body immediately - let it be handled by the world disposal
		// This prevents Box2D crashes during collision processing
		// The physics body will be cleaned up when the world is disposed
		
		// Call parent remove method (removes from stage)
		return super.remove();
	}
	
	/**
	 * Manual disposal method for when we need to force cleanup.
	 * Only call this when we're sure no collision processing is happening.
	 */
	public void forceDispose() {
		if (this.isEnemy) {
			Gdx.app.log("EnemyCleanup", "Force disposing enemy physics body");
		}
		this.dispose();
	}
	
	/**
	 * Recreates the physics body for this actor.
	 * This is used when reusing pooled enemies that had their physics bodies disposed.
	 */
	public void recreatePhysicsBody(com.badlogic.gdx.physics.box2d.World world, float x, float y) {
		// Only recreate if body is null
		if (this.body != null) {
			return;
		}
		
		com.badlogic.gdx.Gdx.app.log("PlayerActor", "Recreating physics body at position (" + x + ", " + y + ")");
		
		// Recreate the physics body using the same logic as AnimatedPhysicsActor constructor
		com.badlogic.gdx.physics.box2d.BodyDef bodyDef = new com.badlogic.gdx.physics.box2d.BodyDef();
		bodyDef.type = com.badlogic.gdx.physics.box2d.BodyDef.BodyType.DynamicBody;
		bodyDef.position.set(x, y);

		// Body shape - use the actual texture dimensions and scale from AnimatedPhysicsActor
		com.badlogic.gdx.physics.box2d.PolygonShape shape = new com.badlogic.gdx.physics.box2d.PolygonShape();
		// Use the same calculation as AnimatedPhysicsActor constructor: (textureWidth/2.f * scale)/PPM
		shape.setAsBox((this.textureWidth/2.f * this.scale)/com.plv.uberplayground.config.Configuration.PPM, 
					   (this.textureHeight/2.f * this.scale)/com.plv.uberplayground.config.Configuration.PPM);

		// Body fixture
		com.badlogic.gdx.physics.box2d.FixtureDef fixtureDef = new com.badlogic.gdx.physics.box2d.FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = 0.1f;

		this.body = world.createBody(bodyDef);
		this.body.createFixture(fixtureDef);
		this.body.setUserData(this);

		// Set collision category for enemy if this is an enemy
		if (this.isEnemy) {
			com.badlogic.gdx.physics.box2d.Filter filter = new com.badlogic.gdx.physics.box2d.Filter();
			filter.categoryBits = com.plv.uberplayground.config.Configuration.EntityCategory.ENEMY_SHIP.getValue();
			filter.maskBits = (short) (com.plv.uberplayground.config.Configuration.EntityCategory.BOUNDARY.getValue() | 
									   com.plv.uberplayground.config.Configuration.EntityCategory.PLAYER_SHIP.getValue() | 
									   com.plv.uberplayground.config.Configuration.EntityCategory.PROJECTILE.getValue());
			this.body.getFixtureList().get(0).setFilterData(filter);
		}

		shape.dispose();
	}
	
	/**
	 * Sets the physics body to null after it has been destroyed.
	 * This is used by the enemy pool system when destroying bodies for pooling.
	 * IMPORTANT: Only call this AFTER the body has been destroyed with world.destroyBody()
	 */
	public void clearPhysicsBodyReference() {
		this.body = null;
	}
}
