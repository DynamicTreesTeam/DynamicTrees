package com.ferreusveritas.dynamictrees.util;

import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.ChunkPos;
import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;

public class BlockBounds {

	public static final BlockBounds INVALID = new BlockBounds();
	
	private int minX, minY, minZ;
	private int maxX, maxY, maxZ;
	private boolean valid = false;
	
	public BlockBounds() {
		valid = false;
	}
	
	public BlockBounds(BlockPos pos) {
		init(pos);
	}

	public BlockBounds(ChunkPos cPos) {
		init(cPos);
	}
	
	public void init(ChunkPos cPos) {
		minX = cPos.getXStart();
		minY = 0;
		minZ = cPos.getZStart();

		maxX = cPos.getXEnd();
		maxY = 255;
		maxZ = cPos.getZEnd();
		valid = true;
	}
	
	public void init(BlockPos pos) {
		minX = maxX = pos.getX();
		minY = maxY = pos.getY();
		minZ = maxZ = pos.getZ();
		valid = true;
	}
	
	public void union(BlockPos pos) {
		
		if(!valid) {
			init(pos);
			return;
		}
		
		if(pos.getX() < minX) {
			minX = pos.getX();
		}
		else
		if(pos.getX() > maxX) {
			maxX = pos.getX();
		}
		
		if(pos.getY() < minY) {
			minY = pos.getY();
		}
		else
		if(pos.getY() > maxY) {
			maxY = pos.getY();
		}

		if(pos.getZ() < minZ) {
			minZ = pos.getZ();
		}
		else
		if(pos.getZ() > maxZ) {
			maxZ = pos.getZ();
		}
		
	}
	
	public boolean inBounds(BlockPos pos) {
		return valid && !(pos.getX() < minX || pos.getX() > maxX || pos.getZ() < minZ || pos.getZ() > maxZ);
	}
	
	public BlockPos getMin() {
		return new BlockPos(minX, minY, minZ);
	}
	
	public BlockPos getMax() {
		return new BlockPos(maxX, maxY, maxZ);
	}

	public BlockBounds shrink(EnumFacing dir, int amount) {
		switch(dir) {
			case DOWN: minY += amount; break;
			case UP: maxY -= amount; break;
			case NORTH: minZ += amount; break;
			case SOUTH: maxZ -= amount; break;
			case WEST: minX += amount; break;
			case EAST: maxX -= amount; break;
		}
		return this;
	}
	
	public BlockBounds shrinkAll() {
		return shrinkAll(1);
	}
	
	public BlockBounds shrinkAll(int amount) {
		for(EnumFacing dir : EnumFacing.VALUES) {
			shrink(dir, amount);
		}
		return this;
	}
	
	public BlockBounds shrinkHorizontal() {
		return shrinkHorizontal(1);
	}
	
	public BlockBounds shrinkHorizontal(int amount) {
		for(EnumFacing dir : EnumFacing.HORIZONTALS) {
			shrink(dir, amount);
		}
		return this;
	}
	
	
}
