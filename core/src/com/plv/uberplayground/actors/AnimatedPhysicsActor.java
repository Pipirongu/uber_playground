package com.plv.uberplayground.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class AnimatedPhysicsActor extends Actor {
    private Animation<TextureRegion> idleAnimation; // Must declare frame type (TextureRegion)
    private int textureWidth;
    private int textureHeight;

    private ParticleEffect pe = null;
    private ParticleEmitter.ScaledNumericValue emitter_angle;
    private float particlesOffsetX = 0.f;
    private float particlesOffsetY = 0.f;
    private Boolean isParticlesActorRotated = false;

    private float elapsedTime = 0;
    private World world;

    private Body body;

    //TODO - might need later
    private static final int VIRTUAL_WIDTH = 16;
    private static final int VIRTUAL_HEIGHT = 9;
    private final static float PPM = 32f;

    public enum ActorType {
        PLAYER,
        SPAWNUNIT
    }
    private ActorType actorType;

    public AnimatedPhysicsActor(ActorType actorType, String animationName, World world, float x, float y, int frameCols, int frameRows){
        this.world = world;
        this.actorType = actorType;

        // Load the sprite sheet as a Texture
        Texture idleSheet;
        idleSheet = new Texture(Gdx.files.internal("actors/"+animationName+".png"));
        // Use the split utility method to create a 2D array of TextureRegions. This is
        // possible because this sprite sheet contains frames of equal size and they are
        // all aligned.
        this.textureWidth = idleSheet.getWidth() / frameCols;
        this.textureHeight = idleSheet.getHeight() / frameRows;
        TextureRegion[][] tmp = TextureRegion.split(idleSheet,
                this.textureWidth,
                this.textureHeight);

        // Place the regions into a 1D array in the correct order, starting from the top
        // left, going across first. The Animation constructor requires a 1D array.
        TextureRegion[] idleFrames = new TextureRegion[frameCols * frameRows];
        int index = 0;
        for (int i = 0; i < frameRows; i++) {
            for (int j = 0; j < frameCols; j++) {
                idleFrames[index++] = tmp[i][j];
            }
        }

        // Initialize the Animation with the frame interval and array of frames
        this.idleAnimation = new Animation<TextureRegion>(1/30f, idleFrames);
        //this.setBounds(VIRTUAL_WIDTH/2, VIRTUAL_HEIGHT/2, this.textureAtlas.findRegion("0001").getRegionWidth()/PPM, this.textureAtlas.findRegion("0001").getRegionHeight()/PPM);

        //body def
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);

        //body shape
        PolygonShape shape = new PolygonShape();
        shape.setAsBox((this.textureWidth/2.f)/PPM, (this.textureHeight/2.f)/PPM);

        //body fixture
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0.1f;

        this.body = this.world.createBody(bodyDef);
        this.body.createFixture(fixtureDef);
        this.body.setUserData(this);

        //this.body.setLinearVelocity(0.f, 0.1f);
        //this.body.applyTorque(2*meters_per_pixels,true);
        //this.body.setAngularVelocity(0.5f);
        shape.dispose();
    }

    @Override
    public void draw(Batch batch, float alpha) {
        elapsedTime += Gdx.graphics.getDeltaTime();
        //this.body.applyForceToCenter(0f, -180f, true);
        //this.body.applyTorque(0.00000002f,true);

        if(this.pe != null) {
            this.pe.draw(batch);
        }


        //sprite and body have different origins, so we draw the animation at the right place
        batch.draw(this.idleAnimation.getKeyFrame(elapsedTime, true),
                this.body.getPosition().x - (this.textureWidth/2.f)/PPM,
                this.body.getPosition().y - (this.textureHeight/2.f)/PPM,
                (this.textureWidth/2.f)/PPM,
                (this.textureHeight/2.f)/PPM,
                this.textureWidth/PPM,
                this.textureHeight/PPM,
                1,
                1,
                (float)Math.toDegrees(this.body.getAngle()));

    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if(this.pe != null) {
            this.pe.setPosition(this.body.getPosition().x + this.particlesOffsetX, this.body.getPosition().y + this.particlesOffsetY);
            //this.setBounds(this.body.getPosition().x - (this.textureRegion.getRegionWidth()/2.f), this.body.getPosition().y - (this.textureRegion.getRegionHeight()/2.f)/PPM, this.textureAtlas.findRegion("0001").getRegionWidth()/PPM, this.textureAtlas.findRegion("0001").getRegionHeight()/PPM);

            if(this.isParticlesActorRotated) {
                for (int i = 0; i < this.pe.getEmitters().size; i++) { //get the list of emitters - things that emit particles
                    ParticleEmitter current_emitter = this.pe.getEmitters().get(i);
                    current_emitter.getAngle().setLowMin(this.emitter_angle.getLowMin() + (float) Math.toDegrees(this.body.getAngle())); //low is the minimum rotation
                    current_emitter.getAngle().setLowMax(this.emitter_angle.getLowMax() + (float) Math.toDegrees(this.body.getAngle()));
                    current_emitter.getAngle().setHighMin(this.emitter_angle.getHighMin() + (float) Math.toDegrees(this.body.getAngle()));
                    current_emitter.getAngle().setHighMax(this.emitter_angle.getHighMax() + (float) Math.toDegrees(this.body.getAngle())); //high is the max rotation
                }
            }
            this.pe.update(delta);
        }
    }

    public void setParticleEmitter(String particlesName, float offsetX, float offsetY, boolean isParticlesActorRotated, boolean isAutoStart) {
        this.particlesOffsetX = offsetX;
        this.particlesOffsetY = offsetX;
        this.isParticlesActorRotated = isParticlesActorRotated;

        this.pe = new ParticleEffect();
        this.pe.load(Gdx.files.internal("particles/"+particlesName+".p"), Gdx.files.internal("particles"));
        this.emitter_angle = new ParticleEmitter.ScaledNumericValue();
        this.emitter_angle.load(this.pe.findEmitter(particlesName).getAngle());
        if(isAutoStart) {
            this.pe.start();
        }
    }

    public void setLinearVelocity(float vX, float vY) {
        this.body.setLinearVelocity(vX, vY);
    }

    public Vector2 getLinearVelocity(){
        return this.body.getLinearVelocity();
    }

    public void setIdleAnimFrameDuration(float frameDuration) {
        this.idleAnimation.setFrameDuration(frameDuration);
    }

    public ActorType getActorType() {
        return this.actorType;
    }

    public Body getBody() {
        return body;
    }
}
