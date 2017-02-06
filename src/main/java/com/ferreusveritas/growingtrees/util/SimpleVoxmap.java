package com.ferreusveritas.growingtrees.util;

import java.util.Arrays;

import com.ferreusveritas.growingtrees.GrowingTrees;


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
	
	Vec3d center = new Vec3d(0, 0, 0);
	
	private static int blitX;
	private static int blitY;
	private static int blitZ;
	
	public SimpleVoxmap(int lenX, int lenY, int lenZ){
		data = new byte[lenX * lenY * lenZ];
		touched = new boolean[lenY];
		this.lenX = lenX;
		this.lenY = lenY;
		this.lenZ = lenZ;
	}
	
	public SimpleVoxmap(int lenX, int lenY, int lenZ, byte[] extData){
		data = Arrays.copyOf(extData, lenX * lenY * lenZ);
		touched = new boolean[lenY];
		for(int y = 0; y < lenY; y++){
			touched[y] = true;
		}
		this.lenX = lenX;
		this.lenY = lenY;
		this.lenZ = lenZ;
	}
	
	public SimpleVoxmap(SimpleVoxmap vmp){
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
	public SimpleVoxmap setMapAndCenter(Vec3d mapPos, Vec3d centerPos){
		setCenter(centerPos);
		center.sub(mapPos);
		return this;
	}
	
	public SimpleVoxmap setCenter(Vec3d centerPos){
		center.set(centerPos);
		return this;
	}
	
	public Vec3d getCenter(){
		return center;
	}
	
	public byte[] getData(){
		return data;
	}
	
	/** @return Size along X-Axis */
	public int getLenX(){
		return lenX;
	}

	/** @return Size along Y-Axis */
	public int getLenY(){
		return lenY;
	}

	/** @return Size along Z-Axis */
	public int getLenZ(){
		return lenZ;
	}
	
	public SimpleVoxmap clear(){
		data = new byte[data.length];
		return this;
	}
	
    public static final byte max(byte a, byte b) {
        return (a >= b) ? a : b;
    }
	
    public void BlitMax(int x, int y, int z, SimpleVoxmap src){
    	for(int iy = 0; iy < src.getLenY(); iy++){
    		int srcY = iy - src.center.y;
    		int dstY = y + srcY;
			setYTouched(dstY);
    		for(int iz = 0; iz < src.getLenZ(); iz++){
        		int srcZ = iz - src.center.z;
        		int dstZ = z + srcZ;
    			for(int ix = 0; ix < src.getLenX(); ix++){
            		int srcX = ix - src.center.x;
            		int dstX = x + srcX;
    				byte srcValue = src.getVoxel(srcX, srcY, srcZ);
    				byte dstValue = getVoxel(dstX, dstY, dstZ);
    				setVoxel(dstX, dstY, dstZ, max(srcValue, dstValue));
    			}
    		}
    	}
    }

	public void BlitClear(int x, int y, int z, SimpleVoxmap src){
    	for(int iy = 0; iy < src.getLenY(); iy++){
    		int srcY = iy - src.center.y;
    		int dstY = y + srcY;
			setYTouched(dstY);
    		for(int iz = 0; iz < src.getLenZ(); iz++){
        		int srcZ = iz - src.center.z;
        		int dstZ = z + srcZ;
    			for(int ix = 0; ix < src.getLenX(); ix++){
            		int srcX = ix - src.center.x;
            		int dstX = x + srcX;
    				byte srcValue = src.getVoxel(srcX, srcY, srcZ);
    				if(srcValue > 0){
    					byte dstValue = getVoxel(dstX, dstY, dstZ);
    					setVoxel(dstX, dstY, dstZ, (byte)0);
    				}
    			}
    		}
    	}

	}
	
	private boolean prepBlit(int x, int y, int z, SimpleVoxmap src){
		if(x <= -src.getLenX() || x >= this.getLenX() || z <= -src.getLenZ() || z >= this.getLenZ() || y <= -src.getLenY() || y >= this.getLenY()){
			return false;
		}
		return true;
	}
	
	private int calcPos(int x, int y, int z){
		return y * lenX * lenZ + z * lenX + x;
	}
	
	public void setVoxelUnsafe(int x, int y, int z, byte value){
		data[calcPos(x, y, z)] = value;
	}
	
	public void setVoxel(int x, int y, int z, byte value){
		x += center.x;
		y += center.y;
		z += center.z;
		if(testBounds(x, y, z)) {
			data[calcPos(x, y, z)] = value;
		}
	}
	
	public byte getVoxel(int x, int y, int z){
		x += center.x;
		y += center.y;
		z += center.z;
		return testBounds(x, y, z) ? data[calcPos(x, y, z)] : 0;
	}
	
	private boolean testBounds(int x, int y, int z){
		return x >= 0 && x < lenX && y >= 0 && y < lenY && z >= 0 && z < lenZ;
	}
	
	public boolean isYTouched(int y){
		y += center.y;
		return y >= 0 && y < lenY && touched[y];
	}

	public void setYTouched(int y){
		y += center.y;
		if(y >= 0 && y < lenY){
			touched[y] = true;
		}
	}
	
	public void print(){
		String buffer;
		for(int y = 0; y < lenY; y++){
			for(int z = 0; z < lenZ; z++){
				buffer = "";
				for(int x = 0; x < lenX; x++){
					buffer += Integer.toHexString(getVoxel(x - center.x, y - center.y, z - center.z) & 0xF);
				}
				System.out.println(buffer);
			}
			buffer = "";
			for(int k = 0; k < lenX; k++){
				buffer += "-";
			}
			System.out.println(buffer);
		}
	}
	
}
