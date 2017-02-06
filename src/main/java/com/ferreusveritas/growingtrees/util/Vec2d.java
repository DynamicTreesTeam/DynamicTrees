package com.ferreusveritas.growingtrees.util;

public class Vec2d {
	
	public int x;
	public int z;
	public boolean loose;//for use with 2nd and 3rd circle finder algorithm
	
	public Vec2d(){}
	
	public Vec2d(int x, int z){
		this();
		set(x, z);
	}

	public Vec2d(Vec2d aCoord){
		this(aCoord.x, aCoord.z);
		setLoose(aCoord.loose);
	}
	
	public Vec2d set(int x, int z){
		this.x = x;
		this.z = z;
		return this;
	}

	public Vec2d set(Vec2d other){
		this.x = other.x;
		this.z = other.z;
		this.loose = other.loose;
		return this;
	}
	
	public Vec2d setLoose(boolean state){
		loose = state;
		return this;
	}
	
	public boolean isLoose(){
		return loose;
	}
	
	public Vec2d add(int x, int z){
		this.x += x;
		this.z += z;
		return this;
	}
	
	public Vec2d sub(int x, int z){
		this.x -= x;
		this.z -= z;
		return this;
	}
	
	public Vec2d add(Vec2d other){
		return add(other.x, other.z);
	}
	
	public Vec2d sub(Vec2d other){
		return sub(other.x, other.z);
	}
	
	public double len(){
		return Math.sqrt(x * x + z * z);//Pythagoras winks
	}
	
	public double angle(){
		return Math.atan2(z, x);
	}
	
	public static int crossProduct(Vec2d c1, Vec2d c2){
	    return (c1.x * c2.z) - (c1.z * c2.x);
	}
	
    @Override
	public boolean equals(Object o) {
        Vec2d v = (Vec2d) o;
        return this.x == v.x && this.z == v.z;
    }

    @Override
	public int hashCode() {
        return x ^ (z * 98764313);
    }
    
	@Override
	public String toString(){
		return "Coord " + x + "," + z + "," + (loose ? "L" : "T");
	}
}
