package com.ferreusveritas.dynamictrees.util;

/**
* A simple bitmap that favors speed over safety.  Consider yourself disclaimed.
* 
* @author ferreusveritas
*/
public class SimpleBitmap {
	
	private int h;
	private int w;
	private int bits[];
	
	public boolean touched;//useful for ruling out entire layers for the voxelmap 
	
	//Not thread safe at all but whatevers.
	private static int dstOffsety;
	private static int srcOffsety;
	private static int runH;
	
	/**
	* @param w Width not to exceed 32
	* @param h Height 
	*/
	public SimpleBitmap(int w, int h) {
		this.w = net.minecraft.util.math.MathHelper.clamp(w, 1, 32);
		this.h = Math.max(1, h);
		this.bits = new int[this.h];
		touched = false;
	}
	
	/**
	* @param w Width not to exceed 32
	* @param h Height
	* @param bits The pixels 
	*/
	public SimpleBitmap(int w, int h, int[] bits) {
		this(w, h);
		if(bits != null) {
			int size = Math.min(bits.length, this.h);
			for(int i = 0; i < size; i++) {
				this.bits[i] = bits[i];
			}
		}
		touched = true;
	}
	
	public SimpleBitmap(SimpleBitmap bmp) {
		this(bmp.w, bmp.h);
		for(int i = 0; i < this.bits.length; i++) {
			this.bits[i] = bmp.bits[i];
		}
		touched = bmp.touched;
	}
	
	public int[] getBits() {
		return bits;
	}
	
	public int getH() {
		return h;
	}
	
	public int getW() {
		return w;
	}
	
	public SimpleBitmap clear() {
		bits = new int[h];
		touched = false;
		return this;
	}
	
	public boolean isColliding(int relX, int relY, SimpleBitmap src) {
		if(prepBlit(relX, relY, src)) {
			if(relX < 0) {
				relX = -relX;
				while(runH-- > 0) {
					if(((this.bits[dstOffsety++] << relX) & src.getBits()[srcOffsety++]) != 0) {
						return true;
					}
				}
			} else {		
				while(runH-- > 0) {
					if((this.bits[dstOffsety++] & (src.getBits()[srcOffsety++] << relX)) != 0) {
						return true;
					}
				}
			}
		}		
		return false;
	}
	
	public void BlitOr(int relX, int relY, SimpleBitmap src) {
		if(prepBlit(relX, relY, src)) {
			if(relX < 0) {
				relX = -relX;
				while(runH-- > 0) {
					this.bits[dstOffsety++] |= (src.getBits()[srcOffsety++] >>> relX);
				}
			} else {		
				while(runH-- > 0) {
					this.bits[dstOffsety++] |= (src.getBits()[srcOffsety++] << relX);
				}
			}
			touched = true;
		}
	}
	
	public void BlitSub(int relX, int relY, SimpleBitmap src) {
		if(prepBlit(relX, relY, src)) {
			if(relX < 0) {
				relX = -relX;
				while(runH-- > 0) {
					this.bits[dstOffsety++] &= ~(src.getBits()[srcOffsety++] >>> relX);
				}
			} else {		
				while(runH-- > 0) {
					this.bits[dstOffsety++] &= ~(src.getBits()[srcOffsety++] << relX);
				}
			}
			touched = true;
		}
	}
	
	public void BlitAnd(int relX, int relY, SimpleBitmap src) {
		if(prepBlit(relX, relY, src)) {
			if(relX < 0) {
				relX = -relX;
				while(runH-- > 0) {
					this.bits[dstOffsety++] &= (src.getBits()[srcOffsety++] >>> relX);
				}
			} else {		
				while(runH-- > 0) {
					this.bits[dstOffsety++] &= ~(src.getBits()[srcOffsety++] << relX);
				}
			}
			touched = true;
		}
		
	}
	
	private boolean prepBlit(int relX, int relY, SimpleBitmap src) {

		if(relX <= -src.w || relX >= this.w || relY <= -src.h || relY >= this.h || (!touched && !src.touched)) {
			return false;
		}
		
		if(relY >= 0) {
			dstOffsety = relY;
			srcOffsety = 0;
		} else {
			dstOffsety = 0;
			srcOffsety = -relY;
		}
		
		runH = Math.min(this.h - dstOffsety, Math.min(src.h, this.h - relY) - srcOffsety);
		
		return true;
	}
	
	/**
	 * Set a pixel
	 * 
	 * @param x 
	 * @param y
	 * @param mode The value to set 0 or 1
	 */
	public void setPixel(int x, int y, int mode) {
		if(x >= 0 && y >= 0 && x < w && y < h) {
			mode &= 1;
			bits[y] = (bits[y] & ~(mode << x)) | mode << x;
			touched = true;
		}
	}
	
	public boolean isPixelOn(int x, int y) {
		return x >= 0 && y >= 0 && x < w && y < h && ((bits[y] >> x) & 1) == 1;
	}
	
	public boolean isRowBlank(int y) {
		return y >= 0 && y < h && bits[y] == 0;
	}
	
	public void print() {
		String buff;
		for(int y = 0; y < h; y++) {
			buff = "";
			for(int x = 0; x < w; x++) {
				buff += isPixelOn(x, y) ? "█" : "░";
			}
			System.out.println(buff);
		}
	}
	
}