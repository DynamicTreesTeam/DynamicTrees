package com.ferreusveritas.dynamictrees.util;

import java.util.Arrays;

import net.minecraft.util.math.BlockPos;

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
		x += center.getX();
		y += center.getY();
		z += center.getZ();
		return testBounds(x, y, z) ? data[calcPos(x, y, z)] : 0;
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

	public void print() {
		String buffer;
		for(int y = 0; y < lenY; y++) {
			for(int z = 0; z < lenZ; z++) {
				buffer = "";
				for(int x = 0; x < lenX; x++) {
					buffer += Integer.toHexString(getVoxel(x - center.getX(), y - center.getY(), z - center.getZ()) & 0xF);
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
