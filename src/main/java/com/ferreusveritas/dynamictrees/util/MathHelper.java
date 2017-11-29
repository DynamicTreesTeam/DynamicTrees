package com.ferreusveritas.dynamictrees.util;

/**
 * This class was made to help cope with version changes from MC 1.7.10 to MC 1.12.2
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