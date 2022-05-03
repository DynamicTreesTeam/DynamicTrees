package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.Random;

/**
 * @author Harley O'Connor
 */
public final class DynamicTreeFeature extends Feature<NoneFeatureConfiguration> {

    public DynamicTreeFeature() {
        super(NoneFeatureConfiguration.CODEC);
        this.setRegistryName(new ResourceLocation(DynamicTrees.MOD_ID, "tree"));
    }

    @Override
    public boolean place(WorldGenLevel world, ChunkGenerator generator, Random rand, BlockPos pos, NoneFeatureConfiguration config) {
//        final long startTime = System.nanoTime();
        final TreeGenerator treeGenerator = TreeGenerator.getTreeGenerator();
        final ServerLevel serverWorld = world.getLevel();
        final ResourceLocation dimensinLocation = serverWorld.dimension().location();

        // Do not generate if the current dimension is blacklisted.
        if (BiomeDatabases.isBlacklisted(dimensinLocation)) {
            return false;
        }

        // Grab biome data base for dimension.
        final BiomeDatabase biomeDatabase = BiomeDatabases.getDimensionalOrDefault(dimensinLocation);

        // Get chunk pos and create safe bounds, which ensure we do not try to generate in an unloaded chunk.
        final ChunkPos chunkPos = world.getChunk(pos).getPos();
        final SafeChunkBounds chunkBounds = new SafeChunkBounds(world, chunkPos);

        // Generate trees.
        treeGenerator.getCircleProvider().getPoissonDiscs(serverWorld, world, chunkPos)
                .forEach(c -> treeGenerator.makeTrees(world, biomeDatabase, c, chunkBounds));

//		final long endTime = System.nanoTime();
//		final long duration = (endTime - startTime) / 1000000;
//		LogManager.getLogger().debug("Dynamic trees at chunk " + chunkPos + " took " + duration + " ms to generate.");
        return true;
    }

}
