package com.ferreusveritas.dynamictrees.util;

import java.util.Arrays;
import java.util.Iterator;

import com.google.common.collect.AbstractIterator;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;

/**
* A simple implementation of a voxel map
* 
* @author ferreusveritas
*/
public class SimpleVoxmap {

	private final byte data[];
	private final boolean touched[];
	
	private final int lenX;
	private final int lenY;
	private final int lenZ;
	private final int layerSize;
	
	BlockPos center = new BlockPos(0, 0, 0);
	
	public SimpleVoxmap(int lenX, int lenY, int lenZ) {
		data = new byte[lenX * lenY * lenZ];
		touched = new boolean[lenY];
		this.lenX = lenX;
		this.lenY = lenY;
		this.lenZ = lenZ;
		this.layerSize = lenX * lenZ;
	}
	
	public SimpleVoxmap(int lenX, int lenY, int lenZ, byte[] extData) {
		data = Arrays.copyOf(extData, lenX * lenY * lenZ);
		touched = new boolean[lenY];
		for(int y = 0; y < lenY; y++) {
			touched[y] = true;
		}
		this.lenX = lenX;
		this.lenY = lenY;
		this.lenZ = lenZ;
		this.layerSize = lenX * lenZ;
	}
	
	public SimpleVoxmap(SimpleVoxmap vmp) {
		this(vmp.getLenX(), vmp.getLenY(), vmp.getLenZ(), vmp.data);
	}
	
	/** 
	* Convenience function to take the guessing and remembering out of how to convert local to world coordinates.
	* 
	* @param mapPos
	* @param centerPos
	* @return
	*/
	public SimpleVoxmap setMapAndCenter(BlockPos mapPos, BlockPos centerPos) {
		setCenter(centerPos);
		center = center.subtract(mapPos);
		return this;
	}
	
	public SimpleVoxmap setMap(BlockPos mapPos) {
		center = center.subtract(mapPos);
		return this;
	}
	
	public SimpleVoxmap setCenter(BlockPos centerPos) {
		center = centerPos;
		return this;
	}
	
	public BlockPos getCenter() {
		return center;
	}
	
	public byte[] getData() {
		return data;
	}
	
	/** @return Size along X-Axis */
	public int getLenX() {
		return lenX;
	}
	
	/** @return Size along Y-Axis */
	public int getLenY() {
		return lenY;
	}
	
	/** @return Size along Z-Axis */
	public int getLenZ() {
		return lenZ;
	}
	
	public interface IBlitOp {
		byte getOp(byte srcValue, byte dstValue);
	}
	
	public SimpleVoxmap blitOp(BlockPos pos, SimpleVoxmap src, IBlitOp op) {
		for(int iy = 0; iy < src.getLenY(); iy++) {
			int srcY = iy - src.center.getY();
			int dstY = pos.getY() + srcY;
			setYTouched(dstY);
			for(int iz = 0; iz < src.getLenZ(); iz++) {
				int srcZ = iz - src.center.getZ();
				int dstZ = pos.getZ() + srcZ;
				for(int ix = 0; ix < src.getLenX(); ix++) {
					int srcX = ix - src.center.getX();
					int dstX = pos.getX() + srcX;
					byte srcValue = src.getVoxel(srcX, srcY, srcZ);
					byte dstValue = getVoxel(dstX, dstY, dstZ);
					setVoxel(dstX, dstY, dstZ, op.getOp(srcValue, dstValue));
				}
			}
		}
		return this;
	}
	
	public SimpleVoxmap blitReplace(BlockPos pos, SimpleVoxmap src) {
		return blitOp(pos, src, (s, d) -> { return s; } );
	}
	
	public SimpleVoxmap blitMax(BlockPos pos, SimpleVoxmap src) {
		return blitOp(pos, src, (s, d) -> { return (s >= d) ? s : d; } );
	}
	
	public SimpleVoxmap blitClear(BlockPos pos, SimpleVoxmap src) {
		return blitOp(pos, src, (s, d) -> { return (s >= 0) ? 0 : d; } );
	}
	
	public interface IFilterOp {
		byte getOp(byte data);
	}
	
	public SimpleVoxmap filter(IFilterOp op) {
		for(int i = 0; i < data.length; i++) {
			data[i] = op.getOp(data[i]);
		}
		return this;
	}
	
	public SimpleVoxmap crop(BlockPos from, BlockPos to) {
		for(MutableBlockPos pos : getAllNonZero()) {
			if( pos.getX() < from.getX() ||
				pos.getY() < from.getY() ||
				pos.getZ() < from.getZ() ||
				pos.getX() > to.getX() ||
				pos.getY() > to.getY() ||
				pos.getZ() > to.getZ() ) {
				setVoxel(pos, (byte) 0);
			}
		}
		return this;
	}
	
	public SimpleVoxmap filter(BlockPos from, BlockPos to, IFilterOp op) {
		for(MutableBlockPos pos : BlockPos.getAllInBoxMutable(from, to) ) {
			setVoxel(pos, op.getOp(getVoxel(pos)));
		}
		return this;
	}
	
	public SimpleVoxmap fill(byte value) {
		return filter((v) -> { return value; } );
	}

	public SimpleVoxmap fill(BlockPos from, BlockPos to, byte value) {
		return filter(from, to, (v) -> { return value; } );
	}
	
	private int calcPos(int x, int y, int z) {
		return y * lenX * lenZ + z * lenX + x;
	}
	
	public void setVoxel(BlockPos pos, byte value) {
		setVoxel(pos.getX(), pos.getY(), pos.getZ(), value);
	}
	
	public void setVoxel(int x, int y, int z, byte value) {
		x += center.getX();
		y += center.getY();
		z += center.getZ();
		if(testBounds(x, y, z)) {
			if(value != 0) {
				setYTouched(y - center.getY());
			}
			data[calcPos(x, y, z)] = value;
		}
	}
	
	/**
	 * Get voxel data relative to world coords
	 * 
	 * @param relPos The position of the center in the world
	 * @param pos The world position of the data request
	 * @return voxel data at coordinates
	 */
	public byte getVoxel(BlockPos relPos, BlockPos pos) {
		return getVoxel(
				pos.getX() - relPos.getX(),
				pos.getY() - relPos.getY(),
				pos.getZ() - relPos.getZ());
	}
	
	public byte getVoxel(BlockPos pos) {
		return getVoxel(pos.getX(), pos.getY(), pos.getZ());
	}
	
	public byte getVoxel(int x, int y, int z) {
		if(isYTouched(y)) {
			x += center.getX();
			y += center.getY();
			z += center.getZ();
			return testBounds(x, y, z) ? data[calcPos(x, y, z)] : 0;
		}
		
		return 0;
	}
	
	private boolean testBounds(int x, int y, int z) {
		return x >= 0 && x < lenX && y >= 0 && y < lenY && z >= 0 && z < lenZ;
	}
	
	public boolean isYTouched(int y) {
		y += center.getY();
		return y >= 0 && y < lenY && touched[y];
	}
	
	public void setYTouched(int y) {
		y += center.getY();
		if(y >= 0 && y < lenY) {
			touched[y] = true;
		}
	}
	
	
	public class Cell {
		private byte value;
		private MutableBlockPos pos;

		public Cell() {
			pos = new MutableBlockPos();
		}
		
		public Cell setValue(byte value) {
			this.value = value;
			return this;
		}
		
		public byte getValue() {
			return value;
		}
		
		public MutableBlockPos getPos() {
			return pos;
		}
		
	}
	
	
	public Iterable<Cell> getAllNonZeroCells() {
		return getAllNonZeroCells((byte) 0xFF);
	}
	
	/** Create an Iterable that returns all cells(value and position) in the map whose value is non-zero */
	public Iterable<Cell> getAllNonZeroCells(final byte mask) {
		
		return new Iterable<Cell>() {
			@Override
			public Iterator<Cell> iterator() {
				return new AbstractIterator<Cell>() {
					private int x = -1;
					private int y = 0;
					private int z = 0;
					private int dataPos = -1;
					private final Cell workingCell = new Cell();
					private final MutableBlockPos dPos = workingCell.getPos();
					
					@Override
					protected Cell computeNext() {

						main:
						while(true) {
							
							if (x < lenX - 1) { 
								x++;
							}
							else if (z < lenZ - 1) {
								x = 0;
								z++;
							}
							else {
								x = -1;
								z = 0;
								y++;
								
								while(y < lenY) {
									if(touched[y]) {
										continue main;
									}
									dataPos += layerSize;
									y++;
								}
								
								return this.endOfData();
							} 
							
							byte value = (byte) (data[++dataPos] & mask);
							if(value > 0) {
								dPos.setPos(x - center.getX(), y - center.getY(), z - center.getZ());
								return workingCell.setValue(value);
							}
						}
					}
				};
			}
		};
	}
	
	
	/** Create an Iterable that returns all positions in the map whose value is non-zero */
	public Iterable<MutableBlockPos> getAllNonZero() {
		return getAllNonZero((byte) 0xFF);
	}
	
	/** Create an Iterable that returns all positions in the map whose value is non-zero */
	public Iterable<MutableBlockPos> getAllNonZero(final byte mask) {
		
		return new Iterable<MutableBlockPos>() {
			@Override
			public Iterator<MutableBlockPos> iterator() {
				return new AbstractIterator<MutableBlockPos>() {
					private int x = -1;
					private int y = 0;
					private int z = 0;
					private int dataPos = -1;
					private final BlockPos.MutableBlockPos dPos = new BlockPos.MutableBlockPos();
					
					@Override
					protected MutableBlockPos computeNext() {
						
						main:
						while(true) {
							
							if (x < lenX - 1) {
								x++;
							}
							else if (z < lenZ - 1) {
								x = 0;
								z++;
							}
							else {
								x = -1;
								z = 0;
								y++;
								
								while(y < lenY) {
									if(touched[y]) {
										continue main;
									}
									dataPos += layerSize;
									y++;
								}
								
								return this.endOfData();
							}
							
							if((data[++dataPos] & mask) > 0) {
								return dPos.setPos(x - center.getX(), y - center.getY(), z - center.getZ());
							}
						}
						
					}
					
				};
			}
		};
	}
	
	/** Create an Iterable that returns all top(Y-axis) positions in the map whose value is non-zero */
	public Iterable<MutableBlockPos> getTops() {
		
		return new Iterable<MutableBlockPos>() {
			@Override
			public Iterator<MutableBlockPos> iterator() {
				return new AbstractIterator<MutableBlockPos>() {
					private int x = -1;
					private int y = 0;
					private int z = 0;
					private int yStart = getStartY();
					private final BlockPos.MutableBlockPos dPos = new BlockPos.MutableBlockPos();
					
					protected int getStartY() {
						int yi;
						for(yi = lenY - 1; yi >= 0 && !touched[yi]; yi--) {}
						return yi;
					}
					
					@Override
					protected MutableBlockPos computeNext() {
						
						while(true) {
							if (x < lenX - 1) {
								x++;
							}
							else if (z < lenZ - 1) {
								x = 0;
								z++;
							} else {
								return this.endOfData();
							}
							
							y = yStart;
							int dataPos = calcPos(x, y, z);
							
							while(y >= 0) {
								if(data[dataPos] != 0) {
									return dPos.setPos(x - center.getX(), y - center.getY(), z - center.getZ());
								}
								dataPos -= layerSize;
								y--;
							}
						}
						
					}
					
				};
			}
		};
	}
	
	public void print() {
				
		String buffer;
		for(int y = 0; y < lenY; y++) {
			for(int z = 0; z < lenZ; z++) {
				buffer = "";
				for(int x = 0; x < lenX; x++) {
					byte b = getVoxel(x - center.getX(), y - center.getY(), z - center.getZ());
					if((b & 32) != 0) {
						buffer += "B";
					}
					else if((b & 16) != 0) {
						buffer += "T";
					} else {
						buffer += Integer.toHexString(b & 0xF);
					}
				}
				System.out.println(buffer);
			}
			buffer = "";
			for(int k = 0; k < lenX; k++) {
				buffer += "-";
			}
			System.out.println(buffer);
		}
	}

}
