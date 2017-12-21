package com.ferreusveritas.dynamictrees.api.backport;

import java.util.Iterator;

import com.google.common.collect.AbstractIterator;

import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockPos implements Comparable<BlockPos> {

	public static final BlockPos ORIGIN = new BlockPos(0, 0, 0);
	/** X coordinate */
	private final int x;
	/** Y coordinate */
	private final int y;
	/** Z coordinate */
	private final int z;

	public BlockPos(int xIn, int yIn, int zIn) {		
		x = xIn;
		y = yIn;
		z = zIn;
	}

	public AxisAlignedBB getAxisAlignedBB() {
		return AxisAlignedBB.getBoundingBox(getX(), getY(), getZ(), getX() + 1, getY() + 1, getZ() + 1);
	}

	public BlockPos(Entity source) {
		this((int)Math.floor(source.posX), (int)Math.floor(source.posY),(int)Math.floor(source.posZ));
	}

	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}
	
	public BlockPos add(BlockPos pos) {
		return pos.getX() == 0 && pos.getY() == 0 && pos.getZ() == 0 ? this : new BlockPos(this.getX() + pos.getX(), this.getY() + pos.getY(), this.getZ() + pos.getZ());
	}

	public BlockPos add(int x, int y, int z) {
		return x == 0 && y == 0 && z == 0 ? this : new BlockPos(getX() + x, getY() + y, getZ() + z); 
	}

	public BlockPos subtract(BlockPos pos) {
		return pos.getX() == 0 && pos.getY() == 0 && pos.getZ() == 0 ? this : new BlockPos(this.getX() - pos.getX(), this.getY() - pos.getY(), this.getZ() - pos.getZ());
	}

	public BlockPos offset(EnumFacing facing) {
		return offset(facing, 1);
	}

	public BlockPos offset(EnumFacing facing, int n) {
		return n == 0 ? this : new BlockPos(this.getX() + facing.getFrontOffsetX() * n, this.getY() + facing.getFrontOffsetY() * n, this.getZ() + facing.getFrontOffsetZ() * n);
	}

	/** Offset this BlockPos 1 block in the given direction */
	public BlockPos offset(ForgeDirection facing) {
		return this.offset(facing, 1);
	}

	/** Offsets this BlockPos n blocks in the given direction */
	public BlockPos offset(ForgeDirection facing, int n) {
		return n == 0 ? this : new BlockPos(this.getX() + facing.offsetX * n, this.getY() + facing.offsetY * n, this.getZ() + facing.offsetZ * n);
	}

	/** Offset this BlockPos 1 block down */
	public BlockPos up() {
		return this.up(1);
	}

	/** Offset this BlockPos n blocks down */
	public BlockPos up(int n) {
		return this.offset(EnumFacing.UP, n);
	}

	/** Offset this BlockPos 1 block down */
	public BlockPos down() {
		return this.down(1);
	}

	/** Offset this BlockPos n blocks down */
	public BlockPos down(int n) {
		return this.offset(EnumFacing.DOWN, n);
	}

	@Override
	public String toString() {
		return "pos: " + getX() + "," + getY() + "," + getZ();
	}
	
	/** Create an Iterable that returns all positions in the box specified by the given corners */
	public static Iterable<BlockPos> getAllInBox(BlockPos from, BlockPos to) {
		final BlockPos blockmin = new BlockPos(Math.min(from.getX(), to.getX()), Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()));
		final BlockPos blockmax = new BlockPos(Math.max(from.getX(), to.getX()), Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()));
		return new Iterable<BlockPos>() {
			@Override
			public Iterator<BlockPos> iterator() {
				return new AbstractIterator<BlockPos>() {
					private BlockPos lastReturned;
					@Override
					protected BlockPos computeNext() {
						if (this.lastReturned == null) {
							this.lastReturned = blockmin;
							return this.lastReturned;
						}
						else if (this.lastReturned.equals(blockmax)) {
							return this.endOfData();
						}
						else {
							int x = this.lastReturned.getX();
							int y = this.lastReturned.getY();
							int z = this.lastReturned.getZ();

							if (x < blockmax.getX()) {
								++x;
							}
							else if (y < blockmax.getY()) {
								x = blockmin.getX();
								++y;
							}
							else if (z < blockmax.getZ()) {
								x = blockmin.getX();
								y = blockmin.getY();
								++z;
							}

							this.lastReturned = new BlockPos(x, y, z);
							return this.lastReturned;
						}
					}
				};
			}
		};
	}

    @Override
	public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        else if (!(object instanceof BlockPos)) {
            return false;
        }
        else {
            BlockPos other = (BlockPos)object;
            return this.getX() != other.getX() ? false : (this.getY() != other.getY() ? false : this.getZ() == other.getZ());
        }
    }
	
    @Override
    public int compareTo(BlockPos compareTo) {
        return this.getY() == compareTo.getY() ? (this.getZ() == compareTo.getZ() ? this.getX() - compareTo.getX() : this.getZ() - compareTo.getZ()) : this.getY() - compareTo.getY();
    }

    /**
     * Calculate squared distance to the given Vector
     */
    public double distanceSq(BlockPos to) {
        return this.distanceSq((double)to.getX(), (double)to.getY(), (double)to.getZ());
    }

    /**
     * Calculate squared distance to the given coordinates
     */
    public double distanceSq(double toX, double toY, double toZ) {
        double d0 = (double)this.getX() - toX;
        double d1 = (double)this.getY() - toY;
        double d2 = (double)this.getZ() - toZ;
        return d0 * d0 + d1 * d1 + d2 * d2;
    }
    
}
