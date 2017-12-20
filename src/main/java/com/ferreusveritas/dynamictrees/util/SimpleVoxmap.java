package com.ferreusveritas.dynamictrees.util;

import java.util.Arrays;
import java.util.Iterator;

import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.google.common.collect.AbstractIterator;


/**
* A simple implementation of a voxel map
* 
* @author ferreusveritas
*/
public class SimpleVoxmap {

	private byte data[];
	private boolean touched[];

	private int lenX;
	private int lenY;
	private int lenZ;

	BlockPos center = new BlockPos(0, 0, 0);

	public SimpleVoxmap(int lenX, int lenY, int lenZ) {
		data = new byte[lenX * lenY * lenZ];
		touched = new boolean[lenY];
		this.lenX = lenX;
		this.lenY = lenY;
		this.lenZ = lenZ;
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
	}

	public SimpleVoxmap(SimpleVoxmap vmp) {
		this(vmp.getLenX(), vmp.getLenY(), vmp.getLenZ(), vmp.data);
	}

	/** 
	* Convenience function to take the guessing and remembering out of how to convert local to world coordinates.
	* 
	* @param mapX
	* @param mapY
	* @param mapZ
	* @param xCenter
	* @param yCenter
	* @param zCenter
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

	public SimpleVoxmap clear() {
		data = new byte[data.length];
		touched = new boolean[touched.length];
		return this;
	}

	public static final byte max(byte a, byte b) {
		return (a >= b) ? a : b;
	}

	public void BlitMax(BlockPos pos, SimpleVoxmap src) {
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
					setVoxel(dstX, dstY, dstZ, max(srcValue, dstValue));
				}
			}
		}
	}

	public void BlitClear(BlockPos pos, SimpleVoxmap src) {
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
					if(srcValue > 0) {
						setVoxel(dstX, dstY, dstZ, (byte)0);
					}
				}
			}
		}

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
				setYTouched(y);
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
		private BlockPos pos;

		public Cell(byte value, BlockPos pos) {
			this.value = value;
			this.pos = pos;
		}

		public byte getValue() {
			return value;
		}
		
		public BlockPos getPos() {
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
					private int x = 0;
					private int y = 0;
					private int z = 0;

					@Override
					protected Cell computeNext() {
						
						while(true) {
							int pos = calcPos(x, y, z);
							BlockPos dPos = new BlockPos(x, y, z);
							
							if (x < lenX - 1) {
								++x;
							}
							else if (z < lenZ - 1) {
								x = 0;
								++z;
							}
							else {
								x = 0;
								z = 0;
								++y;
							} 
							
							if (y >= lenY) {
								return this.endOfData();
							}

							if(touched[y]) {
								byte value = (byte) (data[pos] & mask);
								if(value > 0) {
									return new Cell(value, dPos.subtract(center));
								}
							} else {
								++y;
							}
						}
						
					}
				};
			}
		};
	}


	public Iterable<Cell> getAllNonZeroCellsFromTop() {
		return getAllNonZeroCellsFromTop((byte) 0xFF);
	}
	
	/** Create an Iterable that returns all cells(value and position) in the map whose value is non-zero */
	public Iterable<Cell> getAllNonZeroCellsFromTop(final byte mask) {
		
		return new Iterable<Cell>() {
			@Override
			public Iterator<Cell> iterator() {
				return new AbstractIterator<Cell>() {
					private int x = 0;
					private int y = lenY - 1;
					private int z = 0;

					@Override
					protected Cell computeNext() {
						
						while(true) {
							int pos = calcPos(x, y, z);
							BlockPos dPos = new BlockPos(x, y, z);
							
							if (x < lenX - 1) {
								++x;
							}
							else if (z < lenZ - 1) {
								x = 0;
								++z;
							}
							else {
								x = 0;
								z = 0;
								--y;
							} 
							
							if (y < 0) {
								return this.endOfData();
							}

							if(touched[y]) {
								byte value = (byte) (data[pos] & mask);
								if(value > 0) {
									return new Cell(value, dPos.subtract(center));
								}
							} else {
								--y;
							}
						}
						
					}
				};
			}
		};
	}
	
	public Iterable<BlockPos> getAllNonZero() {
		return getAllNonZero((byte) 0xFF);
	}
	
	/** Create an Iterable that returns all positions in the map whose value is non-zero */
	public Iterable<BlockPos> getAllNonZero(final byte mask) {
		
		return new Iterable<BlockPos>() {
			@Override
			public Iterator<BlockPos> iterator() {
				return new AbstractIterator<BlockPos>() {
					private int x = 0;
					private int y = 0;
					private int z = 0;
					@Override
					protected BlockPos computeNext() {

						while(true) {
							int pos = calcPos(x, y, z);
							BlockPos dPos = new BlockPos(x, y, z);

							if (x < lenX - 1) {
								++x;
							}
							else if (z < lenZ - 1) {
								x = 0;
								++z;
							}
							else {
								x = 0;
								z = 0;
								++y;
							} 

							if (y >= lenY) {
								return this.endOfData();
							}
							
							if(touched[y]) {
								if((data[pos] & mask) > 0) {
									return dPos.subtract(center);
								}
							} else {
								++y;
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
