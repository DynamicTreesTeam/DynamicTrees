package com.ferreusveritas.dynamictrees.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import java.util.Iterator;
import java.util.List;

public class BlockBounds implements Iterable<BlockPos> {

    public static final BlockBounds INVALID = new BlockBounds() {
        @Override
        public boolean inBounds(BlockPos pos) {
            return false;
        }
    };

    private int minX, minY, minZ, maxX, maxY, maxZ;

    private BlockBounds() {
    }

    public BlockBounds(BlockPos pos) {
        this.minX = this.maxX = pos.getX();
        this.minY = this.maxY = pos.getY();
        this.minZ = this.maxZ = pos.getZ();
    }

    public BlockBounds(BlockPos min, BlockPos max) {
        this.minX = min.getX();
        this.minY = min.getY();
        this.minZ = min.getZ();
        this.maxX = max.getX();
        this.maxY = max.getY();
        this.maxZ = max.getZ();
    }

    public BlockBounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public BlockBounds(LevelAccessor level, ChunkPos cPos) {
        minX = cPos.getMinBlockX();
        minY = level.getMinBuildHeight();
        minZ = cPos.getMinBlockZ();

        maxX = cPos.getMaxBlockX();
        maxY = level.getMaxBuildHeight();
        maxZ = cPos.getMaxBlockZ();
    }

    public BlockBounds(BlockBounds other) {
        this.minX = other.minX;
        this.minY = other.minY;
        this.minZ = other.minZ;
        this.maxX = other.maxX;
        this.maxY = other.maxY;
        this.maxZ = other.maxZ;
    }

    public BlockBounds(List<BlockPos> blockPosList) {
        this(blockPosList.get(0));
        union(blockPosList);
    }

    public BlockBounds union(BlockPos pos) {
        if (pos.getX() < this.minX) {
            this.minX = pos.getX();
        } else if (pos.getX() > this.maxX) {
            this.maxX = pos.getX();
        }

        if (pos.getY() < this.minY) {
            this.minY = pos.getY();
        } else if (pos.getY() > this.maxY) {
            this.maxY = pos.getY();
        }

        if (pos.getZ() < this.minZ) {
            this.minZ = pos.getZ();
        } else if (pos.getZ() > this.maxZ) {
            this.maxZ = pos.getZ();
        }

        return this;
    }

    public BlockBounds union(List<BlockPos> blockPosList) {
        blockPosList.forEach(this::union);
        return this;
    }

    public boolean inBounds(BlockPos pos) {
        return !(pos.getX() < this.minX ||
                pos.getX() > this.maxX ||
                pos.getY() < this.minY ||
                pos.getY() > this.maxY ||
                pos.getZ() < this.minZ ||
                pos.getZ() > this.maxZ);
    }

    public BlockPos getMin() {
        return new BlockPos(this.minX, this.minY, this.minZ);
    }

    public BlockPos getMax() {
        return new BlockPos(this.maxX, this.maxY, this.maxZ);
    }

    public BlockBounds expand(Direction dir, int amount) {
        switch (dir) {
            case DOWN:
                this.minY -= amount;
                break;
            case UP:
                this.maxY += amount;
                break;
            case NORTH:
                this.minZ -= amount;
                break;
            case SOUTH:
                this.maxZ += amount;
                break;
            case WEST:
                this.minX -= amount;
                break;
            case EAST:
                this.maxX += amount;
                break;
        }
        return this;
    }

    public BlockBounds shrink(Direction dir, int amount) {
        return this.expand(dir, -amount);
    }

    public BlockBounds move(int x, int y, int z) {
        this.minX += x;
        this.minY += y;
        this.minZ += z;
        this.maxX += x;
        this.maxY += y;
        this.maxZ += z;
        return this;
    }

    public BlockBounds move(BlockPos offset) {
        return move(offset.getX(), offset.getY(), offset.getZ());
    }

    public BlockBounds expand(int amount) {
        this.minX -= amount;
        this.minY -= amount;
        this.minZ -= amount;
        this.maxX += amount;
        this.maxY += amount;
        this.maxZ += amount;
        return this;
    }

    public BlockBounds shrink(int amount) {
        return expand(-amount);
    }

    @Override
    public Iterator<BlockPos> iterator() {
        return BlockPos.betweenClosed(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ).iterator();
    }

    public int getXSize() {
        return this.maxX - this.minX + 1;
    }

    public int getYSize() {
        return this.maxY - this.minY + 1;
    }

    public int getZSize() {
        return this.maxZ - this.minZ + 1;
    }

    @Override
    public String toString() {
        return this != INVALID ? "Bounds{x1=" + this.minX + ", y1=" + this.minY + ", z1=" + this.minZ + " -> x2=" + this.maxX + ", y2=" + this.maxY + ", z2=" + this.maxZ + "}" : "Invalid";
    }
}
