package com.ferreusveritas.growingtrees.worldgen;

import net.minecraft.util.MathHelper;

/**
 * A simple bitmap that favors speed over safety.  Consider yourself disclaimed.
 * 
 * @author ferreusveritas
 *
 */
public class SimpleBitmap {

	private int h;
	private int w;
	private int bits[];
	
	/**
	 * @param w Width not to exceed 32
	 * @param h Height 
	 */
	public SimpleBitmap(int w, int h, int[] bits){
		this.w = MathHelper.clamp_int(w, 1, 32);
		this.h = Math.max(1, h);
		this.bits = bits;
	}
	
	int[] getBits(){
		return bits;
	}
	
	int getH(){
		return h;
	}
	
	int getW(){
		return w;
	}
	
	boolean isColliding(int relX, int relY, SimpleBitmap other){

		if(relX <= -other.w || relX >= this.w || relY <= -other.h || relY >= this.h){
			return false;
		}

		int aOffsety = 0;
		int bOffsety = 0;
		
		if(relY >= 0){
			aOffsety = relY;
		} else {
			bOffsety = -relY;
		}
		
		int runH = Math.min(this.h - aOffsety, other.h - bOffsety);
		 
		if(relX < 0){
			relX = -relX;
			while(runH-- > 0){
				if(((this.bits[aOffsety++] << relX) & other.getBits()[bOffsety++]) != 0){
					return true;
				}
			}
		} else {		
			while(runH-- > 0){
				if((this.bits[aOffsety++] & (other.getBits()[bOffsety++] << relX)) != 0){
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isPixelOn(int x, int y){
		return x >= 0 && y >= 0 && x < w && y < h && ((bits[y] >> x) & 1) == 1;
	}
	
}
