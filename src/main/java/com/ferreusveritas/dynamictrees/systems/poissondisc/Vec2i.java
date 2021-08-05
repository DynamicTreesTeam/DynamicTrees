package com.ferreusveritas.dynamictrees.systems.poissondisc;

public class Vec2i {

	public int x;
	public int z;
	public boolean tight;//for use with 2nd and 3rd circle finder algorithm

	public Vec2i() {
	}

	public Vec2i(int x, int z) {
		this(x, z, false);
	}

	public Vec2i(int x, int z, boolean tight) {
		set(x, z);
		this.tight = tight;
	}

	public Vec2i(Vec2i aCoord) {
		this(aCoord.x, aCoord.z);
		setTight(aCoord.tight);
	}

	public Vec2i set(int x, int z) {
		this.x = x;
		this.z = z;
		return this;
	}

	public Vec2i set(Vec2i other) {
		set(other.x, other.z);
		this.tight = other.tight;
		return this;
	}

	public Vec2i setTight(boolean state) {
		tight = state;
		return this;
	}

	public boolean isTight() {
		return tight;
	}

	public Vec2i add(int x, int z) {
		this.x += x;
		this.z += z;
		return this;
	}

	public Vec2i sub(int x, int z) {
		this.x -= x;
		this.z -= z;
		return this;
	}

	public Vec2i add(Vec2i other) {
		return add(other.x, other.z);
	}

	public Vec2i sub(Vec2i other) {
		return sub(other.x, other.z);
	}

	public double len() {
		return Math.sqrt(x * x + z * z);//Pythagoras winks
	}

	public double angle() {
		return Math.atan2(z, x);
	}

	public static int crossProduct(Vec2i c1, Vec2i c2) {
		return (c1.x * c2.z) - (c1.z * c2.x);
	}

	@Override
	public boolean equals(Object o) {
		Vec2i v = (Vec2i) o;
		return this.x == v.x && this.z == v.z;
	}

	@Override
	public int hashCode() {
		return x ^ (z * 98764313);
	}

	@Override
	public String toString() {
		return "Coord " + x + "," + z + "," + (tight ? "T" : "L");
	}

}
