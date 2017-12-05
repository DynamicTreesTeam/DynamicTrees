package com.ferreusveritas.dynamictrees.util;

/**
 * Maintaining code between Minecraft versions can be a pain.
 * This is a compatibility layer to abstract the finer points
 * of what has recently changed from MC 1.7.10 to MC 1.12.2
 * 
 * @author ferreusveritas
 *
 */
public class MathHelper {

	public static int clamp(int num, int min, int max) {
		return net.minecraft.util.math.MathHelper.clamp(num, min, max);
	}

	public static float clamp(float num, float min, float max) {
		return net.minecraft.util.math.MathHelper.clamp(num, min, max);
	}

	public static double clamp(double num, double min, double max) {
		return net.minecraft.util.math.MathHelper.clamp(num, min, max);
	}

    public static int floor(double value) {
    	return net.minecraft.util.math.MathHelper.floor(value);    }
	
}