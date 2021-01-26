package com.ferreusveritas.dynamictrees.util;

import net.minecraft.util.Direction;

public class Connections {
	
	protected int[] radii;
	protected boolean rootyBlockBelow = false;
	protected boolean stripped = false;
	
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

	public boolean isRootyBlockBelow() {
		return rootyBlockBelow;
	}

	public void setRootyBlockBelow(boolean rootyBlockBelow) {
		this.rootyBlockBelow = rootyBlockBelow;
	}


	public boolean isStripped() {
		return stripped;
	}

	public Connections setStripped(boolean stripped) {
		this.stripped = stripped;
		return this;
	}
}
