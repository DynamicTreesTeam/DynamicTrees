package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * @author Harley O'Connor
 */
public final class DynamicTreeFeature extends Feature<NoneFeatureConfiguration> {

    public DynamicTreeFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> pContext) {
        // final long startTime = System.nanoTime();
        final TreeGenerator treeGenerator = TreeGenerator.getTreeGenerator();
        final ServerLevel serverWorld = pContext.level().getLevel();
        final ResourceLocation dimensionLocation = serverWorld.dimension().location();

        // Do not generate if the current dimension is blacklisted.
        if (BiomeDatabases.isBlacklisted(dimensionLocation)) {
            return false;
        }

        // Grab biome data base for dimension.
        final BiomeDatabase biomeDatabase = BiomeDatabases.getDimensionalOrDefault(dimensionLocation);

        // Get chunk pos and create safe bounds, which ensure we do not try to generate in an unloaded chunk.
        final ChunkPos chunkPos = pContext.level().getChunk(pContext.origin()).getPos();
        final SafeChunkBounds chunkBounds = new SafeChunkBounds(pContext.level(), chunkPos);

        // Generate trees.
        treeGenerator.getCircleProvider().getPoissonDiscs(serverWorld, pContext.level(), chunkPos)
                .forEach(c -> treeGenerator.makeTrees(pContext.level(), biomeDatabase, c, chunkBounds));

        // final long endTime = System.nanoTime();
        // final long duration = (endTime - startTime) / 1000000;
        // LogManager.getLogger().debug("Dynamic trees at chunk " + chunkPos + " took " + duration + " ms to generate.");
        return true;
    }

}
