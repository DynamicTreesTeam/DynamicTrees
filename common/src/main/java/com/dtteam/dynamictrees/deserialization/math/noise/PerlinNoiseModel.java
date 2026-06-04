package com.dtteam.dynamictrees.deserialization.math.noise;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

import java.util.List;

public class PerlinNoiseModel implements NoiseModel {
	
	private final PerlinNoise noise;
	
	public PerlinNoiseModel(RandomSource randomSource, List<Integer> octaves) {
		this.noise = PerlinNoise.create(randomSource, octaves);
	}
	
	@Override
	public double sample(double x, double y, double z) {
		return noise.getValue(x, y, z);
	}
	
}
