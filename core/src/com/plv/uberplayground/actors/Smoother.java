package com.plv.uberplayground.actors;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Smoother {
	private Vector2[] history;
	private int nextUpdateIndex;
	private Vector2 zeroValue = new Vector2();
	
	public Smoother(int sampleSize, Vector2 zeroValue){
		this.zeroValue = zeroValue;
		this.nextUpdateIndex = 0;
		
		this.history = new Vector2[sampleSize];
		for(int i = 0; i < sampleSize; i++){
			this.history[i] = this.zeroValue;
		}
	}
	
	//each time you want to get a new average, feed it the most recent value
	//and this method will return an average over the last SampleSize updates
	public Vector2 Update(Vector2 mostRecentHeading){
		//overwrite the oldest value with the newest
		this.history[this.nextUpdateIndex++] = mostRecentHeading.cpy();
		
	    //make sure m_iNextUpdateSlot wraps around. 
	    if (this.nextUpdateIndex == this.history.length){
	    	this.nextUpdateIndex = 0;
		}	
		
	    //now to calculate the average of the history list
	    Vector2 sum = this.zeroValue.cpy();

	    int size = this.history.length;
	    for (int i = 0; i < this.history.length; i++)
	    {
	    	sum.add(this.history[i].cpy());
	    }

	    return sum.scl(1f/(float)this.history.length);
		
	}
}
