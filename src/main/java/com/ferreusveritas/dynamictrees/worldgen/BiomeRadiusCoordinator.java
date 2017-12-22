package com.ferreusveritas.dynamictrees.worldgen;

import java.util.Random;

import com.ferreusveritas.dynamictrees.api.backport.Biome;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.World;
import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeDensityProvider;
import com.ferreusveritas.dynamictrees.util.MathHelper;

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
		double noiseDensity = (noiseGenerator.func_151601_a(x / scale, z / scale) + 1D) / 2.0D;//Gives 0.0 to 1.0
		double density = densityProvider.getDensity(biome, noiseDensity, world.rand);
		double size = ((1.0 - density) * 9);//Size is the inverse of density(Gives 0 to 9)
		
		//Oh Joy.  Java Random isn't thread safe.  Which means that when minecraft creates multiple chunk generation
		//tasks they can potentially all come up with the same number.  Let's just throw this large prime xor hack in there
		//to get it to at least look like it's random.
		int kindaRandom = (((int)x * 674365771) ^ ((int)z * 254326997)) >> 4;
		
		int shakelow = kindaRandom & 0x3;
		int shakehigh = (kindaRandom >> 2) & 0x3;
		shakelow = (shakelow == 2) ? 1 : (shakelow == 3) ? 2 : 0;
		shakehigh = (shakehigh == 2) ? 1 : (shakehigh == 3) ? 2 : 0;
		
		return MathHelper.clamp((int) size, 2 + shakelow, 8 - shakehigh);//Clamp to tree volume radius range
	}
	
}
