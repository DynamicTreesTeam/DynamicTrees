package com.ferreusveritas.dynamictrees.util;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public class SafeChunkBounds {
	
	public static final SafeChunkBounds ANY = new SafeChunkBounds() {
		@Override
		public boolean inBounds(BlockPos pos, boolean gap) { return true; }
	};
	
	protected static final Tile tiles[] = new Tile[16];
	
	static class Tile {
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
	
	protected SafeChunkBounds() {
		center = null;
	}
	
	public SafeChunkBounds(World world, ChunkPos pos) {
		center = pos;
		
		for(Tile t : tiles) {
			ChunkPos cp = new ChunkPos(pos.x + t.pos.x, pos.z + t.pos.z);
			chunkBounds[t.index] = world.isChunkGeneratedAt(cp.x, cp.z) ? new BlockBounds(cp) : BlockBounds.INVALID;
		}
		
		for(Tile t : tiles) {
			BlockBounds curr = chunkBounds[t.index];
			int border = t.borders;
			if(curr != BlockBounds.INVALID) {
				for(EnumFacing dir : EnumFacing.HORIZONTALS) {
					boolean validDir = false;
					if((border & (1 << dir.getIndex())) != 0) {
						BlockBounds inv = chunkBounds[t.index + dir.getFrontOffsetX() + dir.getFrontOffsetZ() * 4];
						validDir = inv != BlockBounds.INVALID;
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
		int index = 5 + (chunkX - center.x) + ((chunkZ - center.z) * 4);
		return (!gap && chunkBounds[index] != BlockBounds.INVALID) ? true : chunkBounds[index].inBounds(pos);
	}
	
}