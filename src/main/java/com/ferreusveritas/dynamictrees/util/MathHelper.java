package com.ferreusveritas.dynamictrees.util;

import java.util.Random;

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
		return net.minecraft.util.math.MathHelper.clamp_int(num, min, max);
	}

	public static float clamp(float num, float min, float max) {
		return net.minecraft.util.math.MathHelper.clamp_float(num, min, max);
	}

	public static double clamp(double num, double min, double max) {
		return net.minecraft.util.math.MathHelper.clamp_double(num, min, max);
	}

    public static int floor(double value) {
    	return net.minecraft.util.math.MathHelper.floor_double(value);
    }
	
	/** Select a random direction weighted from the probability map **/ 
	public static int selectRandomFromDistribution(Random random, int distMap[]) {
		
		int distSize = 0;
		
		for(int i = 0; i < distMap.length; i++) {
			distSize += distMap[i];
		}
		
		if(distSize <= 0) {
			System.err.println("Warning: Zero sized distribution");
			return -1;
		}
		
		int rnd = random.nextInt(distSize) + 1;
		
		for(int i = 0; i < 6; i++) {
			if(rnd > distMap[i]) {
				rnd -= distMap[i];
			} else {
				return i;
			}
		}
		
		return 0;
	}
	
}