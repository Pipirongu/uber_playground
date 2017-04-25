package com.plv.uberplayground.actors;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

public class Wall {
	private Vector2 pointA;
	private Vector2 pointB;
	private Vector2 normal = new Vector2();
	
	
	public Wall(Vector2 a, Vector2 b){
		this.pointA = a.cpy();
		this.pointB = b.cpy();
		this.CalculateNormal();
	}
	
	private void CalculateNormal()
	{
		Vector2 temp = (pointB.cpy().sub(pointA.cpy())).nor();
		normal.x = -temp.y;
		normal.y = temp.x;
	}

	public Vector2 Normal(){return this.normal;}
	public Vector2 Center(){return (this.pointA.add(this.pointB)).scl(0.5f);}
	public Vector2 From(){return this.pointA;}
	public Vector2 To(){return this.pointB;}
	
	public void Render(ShapeRenderer shapeRenderer, Camera camera, Boolean RenderNormals)
	{	
		Matrix4 mat = camera.combined.cpy();
		shapeRenderer.setProjectionMatrix(mat);
		
		shapeRenderer.setColor(Color.PINK);
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.line(this.pointA, this.pointB);
		shapeRenderer.end();
		
		
		//render the normals if rqd
		if (RenderNormals)
		{
			int MidX = (int)((this.pointA.x+this.pointB.x)/2);
			int MidY = (int)((this.pointA.y+this.pointB.y)/2);
	
			shapeRenderer.setColor(Color.GREEN);
			shapeRenderer.begin(ShapeType.Line);
			shapeRenderer.line(MidX, MidY, (int)(MidX+(this.normal.x)), (int)(MidY+(this.normal.y)));
			shapeRenderer.end();
		}
	}

}
