package com.ferreusveritas.dynamictrees.util;

public class Vec3i {
    /** An immutable vector with zero as all coordinates. */
    public static final Vec3i NULL_VECTOR = new Vec3i(0, 0, 0);
    /** X coordinate */
    private final int x;
    /** Y coordinate */
    private final int y;
    /** Z coordinate */
    private final int z;

    public Vec3i(int xIn, int yIn, int zIn) {
        this.x = xIn;
        this.y = yIn;
        this.z = zIn;
    }
 
    /** Gets the X coordinate. */
    public int getX() {
        return this.x;
    }

    /** Gets the Y coordinate. */
    public int getY() {
        return this.y;
    }

    /** Gets the Z coordinate. */
    public int getZ() {
        return this.z;
    }
    
}