package com.ferreusveritas.growingtrees.util;

import net.minecraftforge.common.util.ForgeDirection;

/**
 * A bastardized 3d vector class that only handles integers.  Useful for voxel coordinate manipulation.
 * 
 * @author ferreusveritas
 *
 */
public class Vec3d {

	public int x;
	public int y;
	public int z;

	public Vec3d(){
		x = y = z = 0;
	}
	
	public Vec3d(int x, int y, int z){
		set(x, y, z);
	}

	public Vec3d(Vec3d o){
		set(o);
	}
	
	public Vec3d set(int x, int y, int z){
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}
	
	public Vec3d set(Vec3d o){
		set(o.x, o.y, o.z);
		return this;
	}
	
	public boolean equals(Vec3d other){
		return x == other.x && y == other.y && z == other.z;
	}
	
	public Vec3d add(Vec3d o){
		this.x += o.x;
		this.y += o.y;
		this.z += o.z;
		return this;
	}
	
	public Vec3d add(ForgeDirection dir){
		this.x += dir.offsetX;
		this.y += dir.offsetY;
		this.z += dir.offsetZ;
		return this;
	}
	
	public Vec3d sub(Vec3d o){
		this.x -= o.x;
		this.y -= o.y;
		this.z -= o.z;
		return this;
	}
}
