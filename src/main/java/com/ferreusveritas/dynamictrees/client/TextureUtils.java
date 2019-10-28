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
			if(offX >= 0 && offX < w && offY >= 0 && offY < h) {
				return pixels[calcPos(offX, offY)];
			}
			return 0;
		}
		
		public void setPixel(int offX, int offY, int pixel) {
			if(offX >= 0 && offX < w && offY >= 0 && offY < h) {
				pixels[calcPos(offX, offY)] = pixel;
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
		
		public int averageColor() {
			return avgColors(pixels);
		}
		
		public void grayScale() {
			for(int i = 0; i < pixels.length; i++) {
				int a = alpha(pixels[i]);
				int r = red(pixels[i]);
				int g = green(pixels[i]);
				int b = blue(pixels[i]);
				
				int gray = ((r * 30) + (g * 59) + (b * 11)) / 100; 
				
				pixels[i] = compose(gray, gray, gray, a);
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
	
	public static int compose(int c[]) {
		return c.length >= 4 ? compose(c[0], c[1], c[2], c[3]) : 0;
	}
	
	/**
	 * @param c input color
	 * @return an array ordered r, g, b, a
	 */
	public static int[] decompose(int c) {
		return new int[] { red(c), green(c), blue(c), alpha(c) };
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
	
	public static int avgColors(int pixels[]) {
		long rAccum = 0;
		long gAccum = 0;
		long bAccum = 0;
		
		int count = 0;
		
		for(int i = 0; i < pixels.length; i++) {
			int alpha = alpha(pixels[i]); 
			if(alpha >= 128) {
				rAccum += red(pixels[i]);
				gAccum += green(pixels[i]);
				bAccum += blue(pixels[i]);
				count++;
			}
		}
		
		count = Math.min(1, count); // special thanks to ruudschouten
		
		int r = (int) (rAccum / count);
		int g = (int) (gAccum / count);
		int b = (int) (bAccum / count);
		
		return compose(r, g, b, 255);
	}
}
