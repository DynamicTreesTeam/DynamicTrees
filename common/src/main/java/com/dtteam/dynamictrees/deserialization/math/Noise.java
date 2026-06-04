package com.dtteam.dynamictrees.deserialization.math;

import com.dtteam.dynamictrees.deserialization.math.noise.*;
import net.minecraft.world.level.levelgen.WorldgenRandom;

import java.util.ArrayList;

public class Noise implements MathOperator {
	
	private static final long BASE_SEED = 96L; // Classic noise seed. Completely arbitrary.
	private static final double DEFAULT_SCALE = 1.0 / 128.0; // Classic noise scale.
	
	private final MathOperator xFunc;
	private final MathOperator yFunc;
	private final MathOperator zFunc;
	private final NoiseModel noise;
	
	private Noise(MathOperator xFunc, MathOperator yFunc, MathOperator zFunc, NoiseModel noise) {
		this.xFunc = xFunc;
		this.yFunc = yFunc;
		this.zFunc = zFunc;
		this.noise = noise;
	}
	
	@Override
	public double apply(MathContext mc) {
		double x = xFunc.apply(mc);
		double y = yFunc.apply(mc);
		double z = zFunc.apply(mc);
		return normalize(noise.sample(x, y, z));
	}
	
	private static double normalize(double val) {
		return (val + 1.0) / 2.0;
	}
	
	public static Noise build(NoiseType noiseType) {
		return build(noiseType, new MathOperator[]{});
	}
	
	public static Noise build(NoiseType noiseType, MathOperator[] functions) {
		int numArgs = functions.length;
		
		MathOperator xFunc = numArgs > 0 ? functions[0] : mc -> mc.pos().getX() * DEFAULT_SCALE;
		MathOperator yFunc = numArgs > 1 ? functions[1] : mc -> mc.pos().getY() * DEFAULT_SCALE;
		MathOperator zFunc = numArgs > 2 ? functions[2] : mc -> mc.pos().getZ() * DEFAULT_SCALE;
		
		WorldgenRandom random = new WorldgenRandom(WorldgenRandom.Algorithm.LEGACY.newInstance(BASE_SEED));
		MathContext mc = new MathContext(random);
		
		if (numArgs > 3) {
			long seed = (long) functions[3].apply(mc);
			random = new WorldgenRandom(WorldgenRandom.Algorithm.LEGACY.newInstance(seed));
			mc = new MathContext(random);
		}
		
		java.util.List<Integer> octaves = new ArrayList<>();
		for (int i = 4; i < numArgs; i++) {
			octaves.add((int) functions[i].apply(mc));
		}
		
		if (octaves.isEmpty()) {
			int singleVal = noiseType == NoiseType.LEGACY ? 1 : 0;
			octaves.add(singleVal); // Default to 1 octave for classic noise for backwards compatibility
		}
		
		NoiseModel model = switch (noiseType) {
			case PERLIN -> new PerlinNoiseModel(new WorldgenRandom(WorldgenRandom.Algorithm.LEGACY.newInstance(BASE_SEED)), octaves);
			case SIMPLEX -> new SimplexNoiseModel(new WorldgenRandom(WorldgenRandom.Algorithm.LEGACY.newInstance(BASE_SEED)));
			case LEGACY -> new PerlinSimplexNoiseModel(new WorldgenRandom(WorldgenRandom.Algorithm.LEGACY.newInstance(BASE_SEED)), octaves);
		};
		
		return new Noise(xFunc, yFunc, zFunc, model);
	}
	
}
