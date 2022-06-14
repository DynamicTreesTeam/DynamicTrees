package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.api.worldgen.RadiusCoordinator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.RandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Function;

public class BiomeRadiusCoordinator implements RadiusCoordinator {

    public PerlinSimplexNoise noiseGenerator;
    protected final TreeGenerator treeGenerator;
    protected final LevelAccessor world;
    protected final ResourceLocation dimRegName;
    protected int pass;
    protected Function<Integer, Integer> chunkMultipass;

    public BiomeRadiusCoordinator(TreeGenerator treeGenerator, ResourceLocation dimRegName, LevelAccessor world) {
        this.noiseGenerator = new PerlinSimplexNoise(new WorldgenRandom(WorldgenRandom.Algorithm.LEGACY.newInstance(96)), new ArrayList<>(Collections.singletonList(1)));
        this.world = world;
        this.dimRegName = dimRegName;
        this.treeGenerator = treeGenerator;
    }

    @Override
    public int getRadiusAtCoords(int x, int z) {
        int rad = this.chunkMultipass.apply(pass);
        if (rad >= 2 && rad <= 8) {
            return rad;
        }

        final double scale = 128; // Effectively scales up the noisemap
        final Biome biome = this.world.getUncachedNoiseBiome((x + 8) >> 2, world.getMaxBuildHeight() >> 2, (z + 8) >> 2).value(); // Placement is offset by +8,+8

        final double noiseDensity = 1.0;//(this.noiseGenerator.getSurfaceNoiseValue(x / scale, 0, z / scale, 1.0) + 1D) / 2.0D; // Gives 0.0 to 1.0
        final double density = BiomeDatabases.getDimensionalOrDefault(this.dimRegName)
                .getDensitySelector(biome).getDensity(this.world.getRandom(), noiseDensity);
        final double size = ((1.0 - density) * 9); // Size is the inverse of density (gives 0 to 9)

        // Oh Joy. Random can potentially start with the same number for each chunk. Let's just
        // throw this large prime xor hack in there to get it to at least look like it's random.
        int kindaRandom = ((x * 674365771) ^ (z * 254326997)) >> 4;
        int shakelow = (kindaRandom & 0x3) % 3; // Produces 0,0,1 or 2
        int shakehigh = (kindaRandom & 0xc) % 3; // Produces 0,0,1 or 2

        return Mth.clamp((int) size, 2 + shakelow, 8 - shakehigh); // Clamp to tree volume radius range
    }

    @Override
    public boolean runPass(int chunkX, int chunkZ, int pass) {
        this.pass = pass;

        if (pass == 0) {
            final Biome biome = this.world.getUncachedNoiseBiome(((chunkX << 4) + 8) >> 2, world.getMaxBuildHeight() >> 2, ((chunkZ << 4) + 8) >> 2).value(); // Aim at center of chunk
            this.chunkMultipass = BiomeDatabases.getDimensionalOrDefault(this.dimRegName).getMultipass(biome);
        }

        return this.chunkMultipass.apply(pass) >= 0;
    }

}
