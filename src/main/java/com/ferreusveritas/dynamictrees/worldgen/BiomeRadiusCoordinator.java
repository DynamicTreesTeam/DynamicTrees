package com.ferreusveritas.dynamictrees.worldgen;

import java.util.Random;

import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeDensityProvider;

import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.NoiseGeneratorPerlin;

public class BiomeRadiusCoordinator implements IRadiusCoordinator {

	NoiseGeneratorPerlin noiseGenerator;
	IBiomeDensityProvider densityProvider;
	
	public BiomeRadiusCoordinator(IBiomeDensityProvider densityProvider) {
		noiseGenerator = new NoiseGeneratorPerlin(new Random(96), 1);
		this.densityProvider = densityProvider;
	}

	@Override
	public int getRadiusAtCoords(World world, double x, double z) {
		double scale = 128;//Effectively scales up the noisemap
		Biome biome = world.getBiome(new BlockPos((int)x, 0, (int)z));
		double noiseDensity = (noiseGenerator.getValue(x / scale, z / scale) + 1D) / 2.0D;//Gives 0.0 to 1.0
		double density = densityProvider.getDensity(biome, noiseDensity, world.rand);
		double size = ((1.0 - density) * 9);//Size is the inverse of density(Gives 0 to 9)

		int shake = world.rand.nextInt(4);
		shake = (shake == 2) ? 1 : (shake == 3) ? 2 : 0;

		return MathHelper.clamp_int((int) size, 2 + shake, 8 - shake);//Clamp to tree volume radius range
	}
	
}
