package com.ferreusveritas.dynamictrees.api.backport;

import net.minecraft.util.MovingObjectPosition;

public class RayTraceResult {

	MovingObjectPosition mop;
	public Type typeOfHit;
	public EnumFacing sideHit;
	private BlockPos blockPos;
	
	public RayTraceResult(MovingObjectPosition mop) {
		this.mop = mop;
		
		switch(mop.typeOfHit) {
			case BLOCK: typeOfHit = Type.BLOCK;	break;
			case ENTITY:typeOfHit = Type.ENTITY; break;
			case MISS: typeOfHit = Type.MISS; break;
		}
		
		switch(mop.sideHit) {
			case 0: sideHit = EnumFacing.DOWN; break;//Bottom
			case 1: sideHit = EnumFacing.UP; break;//Top
			case 2: sideHit = EnumFacing.EAST; break;//East
			case 3: sideHit = EnumFacing.WEST; break;//West
			case 4: sideHit = EnumFacing.NORTH; break;//North
			case 5: sideHit = EnumFacing.SOUTH; break;//South
			default: sideHit = null; break;
		}
		
		blockPos = sideHit != null ? new BlockPos(mop.blockX, mop.blockY, mop.blockZ) : BlockPos.ORIGIN;
	}
	
	public BlockPos getBlockPos() {
		return blockPos;
	}
	
    public static enum Type {
        MISS,
        BLOCK,
        ENTITY;
    }
}
