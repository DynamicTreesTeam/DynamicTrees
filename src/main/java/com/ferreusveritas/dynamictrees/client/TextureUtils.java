package com.ferreusveritas.dynamictrees.client;

public class TextureUtils {
	
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
