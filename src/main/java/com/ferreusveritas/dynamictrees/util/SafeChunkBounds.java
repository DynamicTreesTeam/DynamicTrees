package com.ferreusveritas.dynamictrees.util;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class SafeChunkBounds {
	
	public static final SafeChunkBounds ANY = new SafeChunkBounds() {
		@Override
		public boolean inBounds(BlockPos pos, boolean gap) { return true; }
	};
	
	protected static final Tile tiles[] = new Tile[16];
	
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
	private BlockBounds chunkBounds[] = new BlockBounds[16];
	//private Chunk chunk[] = new Chunk[16];
	
	protected SafeChunkBounds() {
		center = null;
	}
	
	public SafeChunkBounds(World world, ChunkPos pos) {
		center = pos;

		for(Tile t : tiles) {
			ChunkPos cp = new ChunkPos(pos.x + t.pos.x, pos.z + t.pos.z);
			Chunk c = world.getChunkProvider().getLoadedChunk(cp.x, cp.z);
			chunkBounds[t.index] = c != null ? new BlockBounds(cp) : BlockBounds.INVALID;
			//chunk[t.index] = c;
		}

		for(Tile t : tiles) {
			BlockBounds curr = chunkBounds[t.index];
			if(curr != BlockBounds.INVALID) {
				for(EnumFacing dir : EnumFacing.HORIZONTALS) {
					boolean validDir = false;
					if((t.borders & (1 << dir.getIndex())) != 0) {
						BlockBounds adjTile = chunkBounds[t.index + dir.getFrontOffsetX() + dir.getFrontOffsetZ() * 4];
						validDir = adjTile != BlockBounds.INVALID;
					}
					if(!validDir) {
						curr.shrink(dir, 1);
					}
				}
			}
		}
	}
	
	public boolean inBounds(BlockPos pos, boolean gap) {
		int chunkX = pos.getX() >> 4;
		int chunkZ = pos.getZ() >> 4;
		int tileX = (chunkX - center.x + 1);
		int tileZ = (chunkZ - center.z + 1);
		if ( ((tileX | tileZ) & 0xFFFFFFFC) == 0 ) {//Quick way to test if tileX and tileZ are both 0 - 3
			int index = tileX + (tileZ * 4);
			return (!gap && chunkBounds[index] != BlockBounds.INVALID) ? true : chunkBounds[index].inBounds(pos);
		}
		return false;
	}
	
}