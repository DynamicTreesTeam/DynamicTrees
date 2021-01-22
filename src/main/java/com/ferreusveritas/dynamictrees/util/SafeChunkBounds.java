package com.ferreusveritas.dynamictrees.util;

import com.ferreusveritas.dynamictrees.event.SafeChunkEvents;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;

public class SafeChunkBounds {
	
	public static final SafeChunkBounds ANY = new SafeChunkBounds() {
		@Override
		public boolean inBounds(BlockPos pos, boolean gap) { return true; }
	};
	
	protected static final Tile[] tiles = new Tile[16];
	
	protected static class Tile {
		public final ChunkPos pos;
		public final int borders;
		public final int index;
		
		public Tile(int index, ChunkPos pos, int borderFlags) {
			this.index = index;
			this.pos = pos;
			this.borders = borderFlags;
		}
	}
	
	static {
		for(int z = 0; z < 4; z++) {
			for(int x = 0; x < 4; x++) {
				int index = z * 4 + x;
				tiles[index] = new Tile(index, new ChunkPos(x - 1, z - 1), (z != 0 ? 4 : 0) | (z != 3 ? 8 : 0) | (x != 0 ? 16 : 0) | (x != 3 ? 32 : 0));
			}
		}
	}
	
	private final ChunkPos center;
	private final BlockBounds[] chunkBounds = new BlockBounds[16];
	
	protected SafeChunkBounds() {
		center = null;
	}
	
	public SafeChunkBounds(ISeedReader world, ChunkPos pos) {
		center = pos;

		for(Tile t : tiles) {
			ChunkPos cp = new ChunkPos(pos.x + t.pos.x, pos.z + t.pos.z);
			boolean c = world.getChunkProvider().getChunk(cp.x, cp.z, ChunkStatus.EMPTY, false) != null;
			chunkBounds[t.index] = c ? new BlockBounds(cp) : BlockBounds.INVALID;
		}

		for(Tile t : tiles) {
			BlockBounds curr = chunkBounds[t.index];
			if(curr != BlockBounds.INVALID) {
				for(Direction dir : CoordUtils.HORIZONTALS) {
					boolean validDir = false;
					if((t.borders & (1 << dir.getIndex())) != 0) {
						BlockBounds adjTile = chunkBounds[t.index + dir.getXOffset() + dir.getZOffset() * 4];
						validDir = adjTile != BlockBounds.INVALID;
					}
					if(!validDir) {
						curr.shrink(dir, 1);
					}
				}
			}
		}
	}
	
	/**
	 * Tests if the position is in a valid area.
	 * 
	 * @param pos The position to test
	 * @param gap Set to true if the edge blocks of the chunk are also off limits. (Prevents neighbor involvement in adjacent chunk)
	 * @return True if it's safe to place a block at the position, False otherwise.
	 */
	public boolean inBounds(BlockPos pos, boolean gap) {
		int chunkX = pos.getX() >> 4;
		int chunkZ = pos.getZ() >> 4;
		int tileX = (chunkX - center.x + 1);
		int tileZ = (chunkZ - center.z + 1);
		if ( ((tileX | tileZ) & 0xFFFFFFFC) == 0 ) {//Quick way to test if tileX and tileZ are both 0 - 3
			int index = tileX + (tileZ * 4);
			return !gap && chunkBounds[index] != BlockBounds.INVALID || chunkBounds[index].inBounds(pos);
		}
		return false;
	}
	
	public boolean inBounds(BlockBounds bounds, boolean gap) {
		BlockPos min = bounds.getMin();
		BlockPos max = bounds.getMax();
		return inBounds(min, gap) && inBounds(max, gap) && inBounds(new BlockPos(min.getX(), 0, max.getZ()), gap) && inBounds(new BlockPos(max.getX(), 0, min.getZ()), gap);
	}
	
	public void setBlockState(IWorld world, BlockPos pos, BlockState state, boolean gap) {
		setBlockState(world, pos, state, 3, gap);
	}
	
	public void setBlockState(IWorld world, BlockPos pos, BlockState state, int flags, boolean gap) {
		if(inBounds(pos, gap)) {
			world.setBlockState(pos, state, flags);
		}
	}
	
}