package com.plv.uberplayground;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Agent extends Actor {
    private TextureRegion textureRegion;
    private TextureAtlas textureAtlas;
    private Animation animation;
    private ParticleEffect pe;
    private float elapsedTime = 0;

    private World world;
    private Body body;

    private final static float meters_per_pixels = 1/200.f;
    private ParticleEmitter.ScaledNumericValue emitter_angle;

    Agent(World world){
        this.world = world;
        this.textureAtlas = new TextureAtlas(Gdx.files.internal("agent/agent.pack"));
        this.textureRegion = this.textureAtlas.findRegion("0001");
        this.animation = new Animation(1 / 30f, this.textureAtlas.getRegions());

        //body def
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(this.getX()*meters_per_pixels, this.getY()*meters_per_pixels);

        //body shape
        PolygonShape shape = new PolygonShape();
        shape.setAsBox((this.textureAtlas.findRegion("0001").getRegionWidth()/2.f)*meters_per_pixels, (this.textureAtlas.findRegion("0001").getRegionHeight()/2.f)*meters_per_pixels);

        //body fixture
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0.1f;

        this.body = this.world.createBody(bodyDef);
        this.body.createFixture(fixtureDef);

        this.body.setLinearVelocity(0.f, 10*meters_per_pixels);
        this.body.applyTorque(2*meters_per_pixels,true);
        //this.body.setAngularVelocity(0.2f);
        shape.dispose();


        //particle for exhaust
        this.pe = new ParticleEffect();
        this.pe.load(Gdx.files.internal("particles/exhaust.p"), Gdx.files.internal("particles"));
        this.emitter_angle = new ParticleEmitter.ScaledNumericValue();
        this.emitter_angle.load(this.pe.findEmitter("exhaust").getAngle());
        //this.pe.getEmitters().first().setPosition(this.body.getPosition().x/2.f, this.body.getPosition().y/2.f);
        this.pe.start();
    }

    @Override
    public void draw(Batch batch, float alpha) {
        elapsedTime += Gdx.graphics.getDeltaTime();
        //this.body.applyForceToCenter(0f, -180f, true);
        //this.body.applyTorque(0.00000002f,true);

        this.pe.draw(batch);
//        if (this.pe.isComplete()) {
//            this.pe.dispose();
//        }


        //sprite and body have different origins, so we draw the animation at the right place
        batch.draw(animation.getKeyFrame(elapsedTime, true),
                this.body.getPosition().x - ((this.textureRegion.getRegionWidth()/2.f)*meters_per_pixels),
                this.body.getPosition().y - ((this.textureRegion.getRegionHeight()/2.f)*meters_per_pixels),
                ((this.textureRegion.getRegionWidth()/2.f)*meters_per_pixels),
                ((this.textureRegion.getRegionHeight()/2.f)*meters_per_pixels),
                this.textureRegion.getRegionWidth()*meters_per_pixels,
                this.textureRegion.getRegionHeight()*meters_per_pixels,
                1,
                1,
                MathUtils.radiansToDegrees * this.body.getAngle());

    }

    @Override
    public void act(float delta) {
        super.act(delta);

        this.pe.setPosition(this.body.getPosition().x, this.body.getPosition().y);

        for (int i = 0; i < this.pe.getEmitters().size; i++) { //get the list of emitters - things that emit particles
            ParticleEmitter current_emitter =  this.pe.getEmitters().get(i);
            current_emitter.getAngle().setLowMin(this.emitter_angle.getLowMin() + (MathUtils.radiansToDegrees * this.body.getAngle())); //low is the minimum rotation
            current_emitter.getAngle().setLowMax(this.emitter_angle.getLowMax() + (MathUtils.radiansToDegrees * this.body.getAngle()));
            current_emitter.getAngle().setHighMin(this.emitter_angle.getHighMin() + (MathUtils.radiansToDegrees * this.body.getAngle()));
            current_emitter.getAngle().setHighMax(this.emitter_angle.getHighMax() + (MathUtils.radiansToDegrees * this.body.getAngle())); //high is the max rotation
        }
        this.pe.update(delta);
        //this.pe.update(Gdx.graphics.getDeltaTime());
    }


}
