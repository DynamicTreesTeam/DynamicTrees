package com.dtteam.dynamictrees.deserialization.math.noise;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;

import java.util.List;

public class PerlinSimplexNoiseModel implements NoiseModel {
	
	private final PerlinSimplexNoise noise;
	
	public PerlinSimplexNoiseModel(
		RandomSource randomSource,
		List<Integer> octaves
	) {
		this.noise = new PerlinSimplexNoise(randomSource, octaves);
	}
	
	@Override
	public double sample(double x, double y, double z) {
		return noise.getValue(x, z, false);
	}
	
}
