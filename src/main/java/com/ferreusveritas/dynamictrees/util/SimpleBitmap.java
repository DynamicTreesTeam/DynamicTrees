package com.ferreusveritas.dynamictrees.util;

import javax.annotation.Nullable;

/**
 * A simple bitmap that favors speed over safety.  Consider yourself disclaimed.
 *
 * @author ferreusveritas
 */
public class SimpleBitmap {
	
	private final int h;
	private final int w;
	private int[] bits;
	
	public boolean touched; // Useful for ruling out entire layers for the VoxelMap.

	/**
	* @param w Width not to exceed 32
	* @param h Height 
	*/
	public SimpleBitmap(int w, int h) {
		this.w = net.minecraft.util.math.MathHelper.clamp(w, 1, 32);
		this.h = Math.max(1, h);
		this.bits = new int[this.h];
		this.touched = false;
	}
	
	/**
	* @param w Width not to exceed 32
	* @param h Height
	* @param bits The pixels 
	*/
	public SimpleBitmap(final int w, final int h, @Nullable final int[] bits) {
		this(w, h);
		if (bits != null) {
			int size = Math.min(bits.length, this.h);
			if (size >= 0)
				System.arraycopy(bits, 0, this.bits, 0, size);
		}
		this.touched = true;
	}
	
	public SimpleBitmap(SimpleBitmap bmp) {
		this(bmp.w, bmp.h);
		System.arraycopy(bmp.bits, 0, this.bits, 0, this.bits.length);
		this.touched = bmp.touched;
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
		this.bits = new int[h];
		this.touched = false;
		return this;
	}
	
	public boolean isColliding(int relX, int relY, SimpleBitmap src) {
		final BlitPreparationResult result = this.prepBlit(relX, relY, src);
		if (!result.success) {
			return false;
		}

		if (relX < 0) {
			relX = -relX;
			while (result.runH-- > 0) {
				if (((this.bits[result.dstOffsetY++] << relX) & src.getBits()[result.srcOffsetY++]) != 0) {
					return true;
				}
			}
		} else {
			while (result.runH-- > 0) {
				if ((this.bits[result.dstOffsetY++] & (src.getBits()[result.srcOffsetY++] << relX)) != 0) {
					return true;
				}
			}
		}
		return false;
	}
	
	public void blitOr(int relX, int relY, SimpleBitmap src) {
		final BlitPreparationResult result = this.prepBlit(relX, relY, src);
		if (!result.success) {
			return;
		}

		if (relX < 0) {
			relX = -relX;
			while (result.runH-- > 0) {
				this.bits[result.dstOffsetY++] |= (src.getBits()[result.srcOffsetY++] >>> relX);
			}
		} else {
			while (result.runH-- > 0) {
				this.bits[result.dstOffsetY++] |= (src.getBits()[result.srcOffsetY++] << relX);
			}
		}
		touched = true;
	}
	
	public void blitSub(int relX, int relY, SimpleBitmap src) {
		final BlitPreparationResult result = this.prepBlit(relX, relY, src);
		if (!result.success) {
			return;
		}

		if (relX < 0) {
			relX = -relX;
			while (result.runH-- > 0) {
				this.bits[result.dstOffsetY++] &= ~(src.getBits()[result.srcOffsetY++] >>> relX);
			}
		} else {
			while (result.runH-- > 0) {
				this.bits[result.dstOffsetY++] &= ~(src.getBits()[result.srcOffsetY++] << relX);
			}
		}
		touched = true;
	}
	
	public void blitAnd(int relX, int relY, SimpleBitmap src) {
		final BlitPreparationResult result = this.prepBlit(relX, relY, src);
		if (!result.success) {
			return;
		}

		if (relX < 0) {
			relX = -relX;
			while (result.runH-- > 0) {
				this.bits[result.dstOffsetY++] &= (src.getBits()[result.srcOffsetY++] >>> relX);
			}
		} else {
			while (result.runH-- > 0) {
				this.bits[result.dstOffsetY++] &= ~(src.getBits()[result.srcOffsetY++] << relX);
			}
		}
		touched = true;
	}
	
	private BlitPreparationResult prepBlit(int relX, int relY, SimpleBitmap src) {

		if (relX <= -src.w || relX >= this.w || relY <= -src.h || relY >= this.h || (!touched && !src.touched)) {
			return BlitPreparationResult.failure();
		}

		final int dstOffsetY;
		final int srcOffsetY;
		
		if (relY >= 0) {
			dstOffsetY = relY;
			srcOffsetY = 0;
		} else {
			dstOffsetY = 0;
			srcOffsetY = -relY;
		}
		
		final int runH = Math.min(this.h - dstOffsetY, Math.min(src.h, this.h - relY) - srcOffsetY);
		
		return BlitPreparationResult.success(dstOffsetY, srcOffsetY, runH);
	}

	private static final class BlitPreparationResult {
		private static final BlitPreparationResult FAILURE = new BlitPreparationResult(false, 0, 0, 0);

		private final boolean success;
		private int dstOffsetY;
		private int srcOffsetY;
		private int runH;

		public BlitPreparationResult(boolean success, int dstOffsetY, int srcOffsetY, int runH) {
			this.success = success;
			this.dstOffsetY = dstOffsetY;
			this.srcOffsetY = srcOffsetY;
			this.runH = runH;
		}

		private static BlitPreparationResult failure() {
			return FAILURE;
		}

		private static BlitPreparationResult success(int dstOffsetY, int srcOffsetY, int runH) {
			return new BlitPreparationResult(true, dstOffsetY, srcOffsetY, runH);
		}

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
		StringBuilder buff;
		for(int y = 0; y < h; y++) {
			buff = new StringBuilder();
			for(int x = 0; x < w; x++) {
				buff.append(isPixelOn(x, y) ? "█" : "░");
			}
			System.out.println(buff);
		}
	}
	
}