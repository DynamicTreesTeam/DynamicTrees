package com.ferreusveritas.dynamictrees.worldgen;

import java.util.Random;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.NoiseGeneratorPerlin;

public class BiomeRadiusCoordinator implements IRadiusCoordinator {

	public NoiseGeneratorPerlin noiseGenerator;
	protected final TreeGenerator treeGenerator;

	
	public BiomeRadiusCoordinator(TreeGenerator treeGenerator) {
		noiseGenerator = new NoiseGeneratorPerlin(new Random(96), 1);
		this.treeGenerator = treeGenerator;
	}

	@Override
	public int getRadiusAtCoords(World world, double x, double z) {
		double scale = 128;//Effectively scales up the noisemap
		Biome biome = world.getBiome(new BlockPos((int)x, 0, (int)z));
		double noiseDensity = (noiseGenerator.getValue(x / scale, z / scale) + 1D) / 2.0D;//Gives 0.0 to 1.0
		double density = treeGenerator.getBiomeDataBase(world).getDensity(biome).getDensity(world.rand, noiseDensity);
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
