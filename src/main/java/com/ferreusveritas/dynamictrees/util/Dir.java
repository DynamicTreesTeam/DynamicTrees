package com.ferreusveritas.dynamictrees.util;

import net.minecraftforge.common.util.ForgeDirection;

public enum Dir {
	C ( 0, 0),
	N ( 0,-1),
	S ( 0, 1),
	E ( 1, 0),
	W (-1, 0),
	NE( 1,-1),
	NW(-1,-1),
	SE( 1, 1),
	SW(-1, 1);

	Dir(int x, int z){
		xOffset = x;
		zOffset = z;
	}

	public final int xOffset;
	public final int zOffset;

	public static final Dir[] SURROUND = {N, S, E, W, NE, NW, SE, SW};
	public static final Dir[] CARDINAL = {N, S, E, W};
	public static ForgeDirection HORIZONTALS[] = {ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.EAST, ForgeDirection.WEST};
}