package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.util.WorldContext;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;

import java.util.Random;

/**
 * @author Harley O'Connor
 */
public final class DynamicTreeFeature extends Feature<NoFeatureConfig> {

    public DynamicTreeFeature() {
        super(NoFeatureConfig.CODEC);
        this.setRegistryName(new ResourceLocation(DynamicTrees.MOD_ID, "tree"));
    }

    @Override
    public boolean place(ISeedReader world, ChunkGenerator generator, Random rand, BlockPos pos,
                         NoFeatureConfig config) {
//        final long startTime = System.nanoTime();
        final TreeGenerator treeGenerator = TreeGenerator.getTreeGenerator();
        final ResourceLocation dimensionName = world.getLevel().dimension().location();

        // Do not generate if the current dimension is blacklisted.
        if (BiomeDatabases.isBlacklisted(dimensionName)) {
            return false;
        }

        // Grab biome data base for dimension.
        final BiomeDatabase biomeDatabase = BiomeDatabases.getDimensionalOrDefault(dimensionName);

        // Get chunk pos and create safe bounds, which ensure we do not try to generate in an unloaded chunk.
        final ChunkPos chunkPos = world.getChunk(pos).getPos();
        final SafeChunkBounds chunkBounds = SafeChunkBounds.ANY_WG;
        final WorldContext worldContext = new WorldContext(world.getLevel().dimension(), world.getSeed(), world, world.getLevel());

        // Generate trees.
        treeGenerator.getCircleProvider().getPoissonDiscs(worldContext, chunkPos)
                .forEach(disc -> treeGenerator.makeTrees(worldContext, biomeDatabase, disc, chunkBounds));

//		final long endTime = System.nanoTime();
//		final long duration = (endTime - startTime) / 1000000;
//		LogManager.getLogger().debug("Dynamic trees at chunk " + chunkPos + " took " + duration + " ms to generate.");
        return true;
    }

}
