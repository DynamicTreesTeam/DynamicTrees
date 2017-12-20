package com.ferreusveritas.dynamictrees.util;

import java.util.Random;

/**
 * This class was made to help cope with version changes from MC 1.7.10 to MC 1.12.2
 * 
 * @author ferreusveritas
 *
 */
public class MathHelper {
	
	public static int clamp(int num, int min, int max) {
		return net.minecraft.util.MathHelper.clamp_int(num, min, max);
	}

	public static float clamp(float num, float min, float max) {
		return net.minecraft.util.MathHelper.clamp_float(num, min, max);
	}

	public static double clamp(double num, double min, double max) {
		return net.minecraft.util.MathHelper.clamp_double(num, min, max);
	}

    public static int floor(double value) {
    	return net.minecraft.util.MathHelper.floor_double(value);
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