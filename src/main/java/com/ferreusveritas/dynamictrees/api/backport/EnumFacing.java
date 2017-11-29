package com.ferreusveritas.dynamictrees.api.backport;

import net.minecraftforge.common.util.ForgeDirection;

public enum EnumFacing {
	
	    /** -Y */
	    DOWN(0, 0, -1, 0),

	    /** +Y */
	    UP(1, 0, 1, 0),

	    /** -Z */
	    NORTH(2, 0, 0, -1),

	    /** +Z */
	    SOUTH(3, 0, 0, 1),

	    /** -X */
	    WEST(4, -1, 0, 0),

	    /** +X */
	    EAST(5, 1, 0, 0);

    	/** Ordering index for D-U-N-S-W-E */
    	private final int index;
    	
	    private final int offsetX;
	    private final int offsetY;
	    private final int offsetZ;
	    public static final EnumFacing[] VALUES = {DOWN, UP, NORTH, SOUTH, WEST, EAST};
	    public static final EnumFacing[] HORIZONTALS = {NORTH, SOUTH, WEST, EAST};
	    
	    public static final int[] OPPOSITES = {1, 0, 3, 2, 5, 4, 6};
	    // Left hand rule rotation matrix for all possible axes of rotation
	    public static final int[][] ROTATION_MATRIX = {
	        {0, 1, 4, 5, 3, 2, 6},
	        {0, 1, 5, 4, 2, 3, 6},
	    	{5, 4, 2, 3, 0, 1, 6},
	    	{4, 5, 2, 3, 1, 0, 6},
	    	{2, 3, 1, 0, 4, 5, 6},
	    	{3, 2, 0, 1, 4, 5, 6},
	    	{0, 1, 2, 3, 4, 5, 6},
	    };

	    private EnumFacing(int i, int x, int y, int z) {
	    	index = i;
	        offsetX = x;
	        offsetY = y;
	        offsetZ = z;
	    }

	    public int getIndex() {
	        return index;
	    }
	    
	    public static EnumFacing fromForgeDirection(ForgeDirection dir) {
	    	return getFront(dir.ordinal());
	    }
	    
	    public ForgeDirection toForgeDirection() {
	    	return ForgeDirection.getOrientation(ordinal());
	    }
	    
	    public int getFrontOffsetX() {
	        return this.offsetX;
	    }

	    public int getFrontOffsetY() {
	        return this.offsetY;
	    }

	    public int getFrontOffsetZ() {
	        return this.offsetZ;
	    }

	    public static EnumFacing getFront(int id) {
	        if (id >= 0 && id < VALUES.length) {
	            return VALUES[id];
	        }
	        return null;
	    }

	    public EnumFacing getOpposite() {
	        return getFront(OPPOSITES[ordinal()]);
	    }

	    public EnumFacing getRotation(ForgeDirection axis) {
	    	return getFront(ROTATION_MATRIX[axis.ordinal()][ordinal()]);
	    }
}
