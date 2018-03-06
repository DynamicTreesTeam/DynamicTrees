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
		return net.minecraft.util.math.MathHelper.clamp(num, min, max);
	}

	public static float clamp(float num, float min, float max) {
		return net.minecraft.util.math.MathHelper.clamp(num, min, max);
	}

	public static double clamp(double num, double min, double max) {
		return net.minecraft.util.math.MathHelper.clamp(num, min, max);
	}

    public static int floor(double value) {
    	return net.minecraft.util.math.MathHelper.floor(value);
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

    /**
     * Wrap between 0(inclusive) and max(exclusive)
     * 
     * @param i The parameter to wrap
     * @param max The value(must be positive) that if met or exceeded will wrap around 
     * @return The wrapped value
     */
    public static int wrap(int i, int max) {
		while(i < 0) {
			i += max;
		}
    	return i % max;
    }
	
	public static double wrapAngle(double angle) {
		final double TwoPi = Math.PI * 2;
		angle %= TwoPi;//Wrap angle
		return angle + (angle < 0 ? TwoPi : 0);//Convert negative angle to positive
	}

	/**
	* Convert Range [0, PI * 2] to [0, 1]
	* @param angle The angle to convert [0, PI * 2] (angle will be wrapped to this range)
	* @return range [0, 1]
	*/
	public static float radiansToTurns(double angle) {
		return (float) (wrapAngle(angle) / (Math.PI * 2));
	}

	/**
	* Length (angular) of a shortest way between two angles.
	* 
	* @param alpha First angle in range [0, PI * 2] (input will be wrapped to range)
	* @param beta Second angle in range [0, PI * 2] (input will be wrapped to range)
	* @return Shorted Delta angle in range [0, PI]
	*/
	public static double deltaAngle(double alpha, double beta) {
		double phi = Math.abs(beta - alpha) % (Math.PI * 2);// This is either the distance or 360 - distance
		double distance = phi > Math.PI ? (Math.PI * 2) - phi : phi;
		return distance;
	}

}