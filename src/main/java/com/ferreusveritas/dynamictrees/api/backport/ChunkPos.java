package com.ferreusveritas.dynamictrees.api.backport;

public class ChunkPos {
	
	/** The X position of this Chunk Coordinate Pair */
    public final int chunkXPos;
    /** The Z position of this Chunk Coordinate Pair */
    public final int chunkZPos;

    public ChunkPos(int x, int z) {
        this.chunkXPos = x;
        this.chunkZPos = z;
    }

    public ChunkPos(BlockPos pos) {
        this.chunkXPos = pos.getX() >> 4;
        this.chunkZPos = pos.getZ() >> 4;
    }

    /** Get the first world X coordinate that belongs to this Chunk */
    public int getXStart() {
        return this.chunkXPos << 4;
    }

    /** Get the first world Z coordinate that belongs to this Chunk */
    public int getZStart() {
        return this.chunkZPos << 4;
    }

    /** Get the last world X coordinate that belongs to this Chunk */
    public int getXEnd() {
        return (this.chunkXPos << 4) + 15;
    }

    /** Get the last world Z coordinate that belongs to this Chunk */
    public int getZEnd() {
        return (this.chunkZPos << 4) + 15;
    }

    
}
