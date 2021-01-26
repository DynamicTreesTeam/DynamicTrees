package com.ferreusveritas.dynamictrees.util;

import net.minecraft.util.Direction;

public class Connections {
	
	protected int[] radii;
	
	public Connections (){
		radii = new int[] {0,0,0,0,0,0};
	}
	
	public Connections(int[] radii) {
		this.radii = radii;
	}
	
	public void setRadius (Direction dir, int radius){
		radii[dir.getIndex()] = radius;
	}

	public int[] getAllRadii (){
		return radii;
	}

	public Connections setAllRadii (int[] radii){
		this.radii = radii;
		return this;
	}

}
