package com.plv.uberplayground;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.math.MathUtils;

public class Agent extends Actor {
    private TextureRegion textureRegion;
    private TextureAtlas textureAtlas;
    private Animation animation;
    ParticleEffect pe;
    private float elapsedTime = 0;

    private World world;
    private Body body;

    Agent(World world){
        this.world = world;
        this.textureAtlas = new TextureAtlas(Gdx.files.internal("agent/agent.pack"));
        this.textureRegion = this.textureAtlas.findRegion("0001");
        this.animation = new Animation(1 / 30f, this.textureAtlas.getRegions());

        //body def
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(this.getX(), this.getY());

        //body shape
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(this.textureAtlas.findRegion("0001").getRegionWidth()/2.f, this.textureAtlas.findRegion("0001").getRegionHeight()/2.f);

        //body fixture
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0.1f;

        this.body = this.world.createBody(bodyDef);
        this.body.createFixture(fixtureDef);

        this.body.setLinearVelocity(0.f, 10.f);
        shape.dispose();


        //particle for exhaust
        this.pe = new ParticleEffect();
        this.pe.load(Gdx.files.internal("particles/exhaust.parti"), Gdx.files.internal("particles"));
        this.pe.getEmitters().first().setPosition(this.body.getPosition().x/2.f, this.body.getPosition().y/2.f);
        this.pe.start();
    }

    @Override
    public void draw(Batch batch, float alpha) {
        elapsedTime += Gdx.graphics.getDeltaTime();
        //this.body.applyForceToCenter(0f, -180f, true);
        //this.body.applyTorque(409f,true);

        this.pe.update(Gdx.graphics.getDeltaTime());

        this.pe.draw(batch);;
        if (this.pe.isComplete()) {
            this.pe.reset();
        }

        //sprite and body have different origins, so we draw the animation at the right place
        batch.draw(animation.getKeyFrame(elapsedTime, true),
                this.body.getPosition().x - this.textureRegion.getRegionWidth()/2.f,
                this.body.getPosition().y - this.textureRegion.getRegionHeight()/2.f,
                this.textureRegion.getRegionWidth()/2.f,
                this.textureRegion.getRegionHeight()/2.f,
                this.textureRegion.getRegionWidth(),
                this.textureRegion.getRegionHeight(),
                1,
                1,
                MathUtils.radiansToDegrees*this.body.getAngle());
    }

    @Override
    public void act(float delta) {
        for (int i = 0; i < this.pe.getEmitters().size; i++) { //get the list of emitters - things that emit particles
            this.pe.getEmitters().get(i).setPosition(this.body.getPosition().x/2.f, this.body.getPosition().y/2.f);
            this.pe.getEmitters().get(i).getAngle().setLow(MathUtils.radiansToDegrees * this.body.getAngle()); //low is the minimum rotation
            this.pe.getEmitters().get(i).getAngle().setHigh(MathUtils.radiansToDegrees * this.body.getAngle()); //high is the max rotation
        }
    }


}
