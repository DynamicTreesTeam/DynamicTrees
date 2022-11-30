package com.ferreusveritas.dynamictrees.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;

@Deprecated(forRemoval = true)
public class SafeChunkBounds {

    public static final SafeChunkBounds ANY = new SafeChunkBounds() {
        @Override
        public boolean inBounds(BlockPos pos, boolean gap) {
            return true;
        }
    };


    /**
     * Used to disable safe chunk bounds during world gen. This must be separate from {@link #ANY} so the
     * {@code safeChunkBounds == SafeChunkBounds.ANY} comparison can still be used to determine if we are currently in
     * world gen.
     */
    public static final SafeChunkBounds ANY_WG = new SafeChunkBounds() {
        @Override
        public boolean inBounds(BlockPos pos, boolean gap) {
            return true;
        }
    };

    protected static final Tile[] TILES = new Tile[16];

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
        for (int z = 0; z < 4; z++) {
            for (int x = 0; x < 4; x++) {
                final int index = z * 4 + x;
                TILES[index] = new Tile(index, new ChunkPos(x - 1, z - 1), (z != 0 ? 4 : 0) | (z != 3 ? 8 : 0) | (x != 0 ? 16 : 0) | (x != 3 ? 32 : 0));
            }
        }
    }

    private final ChunkPos center;
    private final BlockBounds[] chunkBounds = new BlockBounds[16];

    protected SafeChunkBounds() {
        center = null;
    }

    public SafeChunkBounds(WorldGenLevel world, ChunkPos pos) {
        this.center = pos;

        for (final Tile tile : TILES) {
            ChunkPos cp = new ChunkPos(pos.x + tile.pos.x, pos.z + tile.pos.z);
            final boolean loaded = world.getChunkSource().getChunk(cp.x, cp.z, ChunkStatus.EMPTY, false) != null;
            this.chunkBounds[tile.index] = loaded ? new BlockBounds(world, cp) : BlockBounds.INVALID;
        }

        for (Tile tile : TILES) {
            final BlockBounds curr = this.chunkBounds[tile.index];
            if (curr != BlockBounds.INVALID) {
                for (final Direction dir : CoordUtils.HORIZONTALS) {
                    boolean validDir = false;
                    if ((tile.borders & (1 << dir.get3DDataValue())) != 0) {
                        final BlockBounds adjTile = this.chunkBounds[tile.index + dir.getStepX() + dir.getStepZ() * 4];
                        validDir = adjTile != BlockBounds.INVALID;
                    }
                    if (!validDir) {
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
     * @param gap Set to true if the edge blocks of the chunk are also off limits. (Prevents neighbor involvement in
     *            adjacent chunk)
     * @return True if it's safe to place a block at the position, False otherwise.
     */
    public boolean inBounds(BlockPos pos, boolean gap) {
        final int chunkX = pos.getX() >> 4;
        final int chunkZ = pos.getZ() >> 4;
        final int tileX = (chunkX - center.x + 1);
        final int tileZ = (chunkZ - center.z + 1);

        if (((tileX | tileZ) & 0xFFFFFFFC) == 0) { // Quick way to test if tileX and tileZ are both 0 - 3
            final int index = tileX + (tileZ * 4);
            return !gap && chunkBounds[index] != BlockBounds.INVALID || chunkBounds[index].inBounds(pos);
        }
        return false;
    }

    public boolean inBounds(BlockBounds bounds, boolean gap) {
        final BlockPos min = bounds.getMin();
        final BlockPos max = bounds.getMax();

        return this.inBounds(min, gap) && inBounds(max, gap) &&
                this.inBounds(new BlockPos(min.getX(), 0, max.getZ()), gap) &&
                this.inBounds(new BlockPos(max.getX(), 0, min.getZ()), gap);
    }

    public void setBlockState(LevelAccessor world, BlockPos pos, BlockState state, boolean gap) {
        this.setBlockState(world, pos, state, 3, gap);
    }

    public void setBlockState(LevelAccessor world, BlockPos pos, BlockState state, int flags, boolean gap) {
        if (this.inBounds(pos, gap)) {
            world.setBlock(pos, state, flags);
        }
    }

}