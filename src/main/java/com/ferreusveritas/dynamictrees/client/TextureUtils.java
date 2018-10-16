package com.ferreusveritas.dynamictrees.client;

import java.util.ArrayList;
import java.util.Arrays;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class TextureUtils {
	
	public static class PixelBuffer {
		public final int[] pixels;
		public final int w;
		public final int h;
		
		public PixelBuffer(int w, int h) {
			this.w = w;
			this.h = h;
			pixels = new int[w * h];
		}
		
		public PixelBuffer(TextureAtlasSprite sprite) {
			this.w = sprite.getIconWidth();
			this.h = sprite.getIconHeight();
			int data[][] = sprite.getFrameTextureData(0);
			pixels = data[0];
		}
		
		public PixelBuffer(TextureAtlasSprite sprite, boolean copy) {
			this.w = sprite.getIconWidth();
			this.h = sprite.getIconHeight();
			int[][] original = sprite.getFrameTextureData(0);
			pixels = Arrays.copyOf(original[0], original[0].length);
		}
		
		public PixelBuffer(PixelBuffer other) {
			this.w = other.w;
			this.h = other.h;
			this.pixels = Arrays.copyOf(other.pixels, other.pixels.length);
		}
		
		public void apply(TextureAtlasSprite sprite) {
			if(w == sprite.getIconWidth() && h == sprite.getIconHeight()) {
				int[][] original = sprite.getFrameTextureData(0);
				int[][] data = new int[original.length][];
				data[0] = pixels;
				ArrayList<int[][]> ftd = new ArrayList<>();
				ftd.add(data);
				sprite.setFramesTextureData(ftd);
			}
		}
		
		public int calcPos(int offX, int offY) {
			return offY * w + offX;
		}
		
		public int getPixel(int offX, int offY) {
			int pos = calcPos(offX, offY);
			return pos < 0 ? 0 : (pos >= pixels.length ? 0 : pixels[pos]);
		}

		public void setPixel(int offX, int offY, int pixel) {
			int pos = calcPos(offX, offY);
			if(pos >= 0 && pos < pixels.length) {
				pixels[pos] = pixel;
			}
		}
		
		public void blit(PixelBuffer dst, int offX, int offY) {
			blit(dst, offX, offY, 0);
		}
		
		//A very very inefficient and simple blitter.
		public void blit(PixelBuffer dst, int offX, int offY, int rotCW90) {
			switch(rotCW90 & 3) {
				case 0:
					for(int y = 0; y < h; y++) {
						for(int x = 0; x < w; x++) {
							dst.setPixel(x + offX, y + offY, getPixel(x, y));
						}
					};
					return;
				case 1: 
					for(int y = 0; y < h; y++) {
						for(int x = 0; x < w; x++) {
							int destX = h - y - 1;
							int destY = x;
							dst.setPixel(destX + offX, destY + offY, getPixel(x, y));
						}
					}
					return;
				case 2:
					for(int y = 0; y < h; y++) {
						for(int x = 0; x < w; x++) {
							int destX = w - x - 1;
							int destY = h - y - 1;
							dst.setPixel(destX + offX, destY + offY, getPixel(x, y));
						}
					}
					return;
				case 3:
					for(int y = 0; y < h; y++) {
						for(int x = 0; x < w; x++) {
							int destX = y;
							int destY = w - x - 1;
							dst.setPixel(destX + offX, destY + offY, getPixel(x, y));
						}
					}
					return;
			}
		}
		
		public void grayScale() {
			for(int i = 0; i < pixels.length; i++) {
				int a = (pixels[i] >> 24) & 0xff;
				int r = (pixels[i] >> 16) & 0xff;
				int g = (pixels[i] >> 8) & 0xff;
				int b = (pixels[i] >> 0) & 0xff;
				
				int gray = ((r * 30) + (g * 59) + (b * 11)) / 100; 
				
				pixels[i] = (a << 24) + (gray << 16) + (gray << 8) + gray;
			}
		}
		
		public void fill(int color) {
			for(int i = 0; i < pixels.length; i++) {
				pixels[i] = color;
			}
		}
		
	}
	
	public static int compose(int r, int g, int b, int a) {
		int rgb = a;
		rgb = (rgb << 8) + r;
		rgb = (rgb << 8) + g;
		rgb = (rgb << 8) + b;
		return rgb;
	}
	
	public static int alpha(int c) {
		return (c >> 24) & 0xFF;
	}
	
	public static int red(int c) {
		return (c >> 16) & 0xFF;
	}
	
	public static int green(int c) {
		return (c >> 8) & 0xFF;
	}
	
	public static int blue(int c) {
		return (c) & 0xFF;
	}
	
}
