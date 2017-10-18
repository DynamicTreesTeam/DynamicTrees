package com.ferreusveritas.dynamictrees.util;

public class SimpleTreeBlockModel {
	public int radius;
	public int hydro;
	
	public SimpleTreeBlockModel() {
		radius = 0; 
		hydro = 0;
	}
	
	boolean isBranch() {
		return radius > 0;
	}

	boolean isLeaves() {
		return hydro > 0;
	}
}