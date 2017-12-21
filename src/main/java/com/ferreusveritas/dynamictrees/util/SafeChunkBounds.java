package com.ferreusveritas.dynamictrees.util;

import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.ChunkPos;
import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;
import com.ferreusveritas.dynamictrees.api.backport.World;

public class SafeChunkBounds {
	
	private final int centerX;
	private final int centerZ;
	private BlockBounds chunkBounds[] = new BlockBounds[9];
	private int shrink = 0;
	
	private enum Tile {
		NW(0, new Vec3i(-1, 0,-1), EnumFacing.SOUTH, EnumFacing.EAST),
		N (1, new Vec3i( 0, 0,-1), EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST),
		NE(2, new Vec3i( 1, 0,-1), EnumFacing.SOUTH, EnumFacing.WEST),
		W (3, new Vec3i(-1, 0, 0), EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST),		
		M (4, new Vec3i( 0, 0, 0), EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST),
		E (5, new Vec3i( 1, 0, 0), EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST),
		SW(6, new Vec3i(-1, 0, 1), EnumFacing.NORTH, EnumFacing.EAST),
		S (7, new Vec3i( 0, 0, 1), EnumFacing.NORTH, EnumFacing.WEST, EnumFacing.EAST),
		SE(8, new Vec3i( 1, 0, 1), EnumFacing.NORTH, EnumFacing.WEST);
		
		public final Vec3i pos;
		public final int borders;
		public final int index;
		
		private Tile(int index, Vec3i pos, EnumFacing ... dirs) {
			this.index = index;
			this.pos = pos;
			
			int b = 0;
			for(EnumFacing dir : dirs) {
				b |= 1 << dir.getIndex();
			}
			
			this.borders = b;
		}
	}
	
	
	public SafeChunkBounds(World world, BlockPos pos) {
		
		centerX = pos.getX() >> 4;
		centerZ = pos.getZ() >> 4;
		
		for(Tile t : Tile.values()) {
			int chunkX = centerX + t.pos.getX();
			int chunkZ = centerZ + t.pos.getZ();
			chunkBounds[t.index] = world.real().getChunkProvider().chunkExists(chunkX, chunkZ) ? new BlockBounds(new ChunkPos(chunkX, chunkZ)) : BlockBounds.INVALID;
		}
		
		rebuildChunkBorders();
	}	
	
	private void rebuildChunkBorders() {
		for(Tile t : Tile.values()) {
			BlockBounds curr = chunkBounds[t.index];
			int border = t.borders;
			if(curr != BlockBounds.INVALID) {
				int chunkX = centerX + t.pos.getX();
				int chunkZ = centerZ + t.pos.getZ();
				curr.init(new ChunkPos(chunkX, chunkZ));//reset the tile
				for(EnumFacing dir : EnumFacing.HORIZONTALS) {
					boolean validDir = false;
					if((border & (1 << dir.getIndex())) != 0) {
						BlockBounds inv = chunkBounds[t.index + dir.getFrontOffsetX() + dir.getFrontOffsetZ() * 3];
						validDir = inv != BlockBounds.INVALID;
					}
					if(!validDir) {
						curr.shrink(dir, shrink);
					}
				}
			}
		}
	}
	
	public SafeChunkBounds setShrink(int amount) {
		shrink = amount;
		rebuildChunkBorders();
		return this;
	}
	
	public boolean inBounds(BlockPos pos) {
		int chunkX = pos.getX() >> 4;
		int chunkZ = pos.getZ() >> 4;
		int p = 4 + (chunkX - centerX) + ((chunkZ - centerZ) * 3);
		if(shrink == 0 && chunkBounds[p] != BlockBounds.INVALID) {
			return true;
		}
		return chunkBounds[p].inBounds(pos);
	}
	
}