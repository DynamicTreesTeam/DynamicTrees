package com.ferreusveritas.dynamictrees.util;

/**
 * A variant of Vec2i that recomputes radial angle
 * 
 * @author ferreusveritas
 *
 */
public class Vec2iPCA extends Vec2i {
	
	double radians;
	
	public Vec2iPCA(int x, int z, boolean tight) {
		super(x, z, tight);
	}
	
	@Override
	public Vec2i set(int x, int z) {
		super.set(x, z);
		radians = super.angle();
		return this;
	}
	
	@Override
	public double angle() {
		return radians;
	}
	
}
