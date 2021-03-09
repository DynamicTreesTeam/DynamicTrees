package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.resources.DTResourceRegistries;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.server.ServerWorld;

import java.util.Random;

/**
 * @author Harley O'Connor
 */
public final class DynamicTreeFeature extends Feature<NoFeatureConfig> {

    public DynamicTreeFeature () {
        super(NoFeatureConfig.field_236558_a_);
        this.setRegistryName(new ResourceLocation(DynamicTrees.MOD_ID, "tree"));
    }

    @Override
    public boolean generate(ISeedReader world, ChunkGenerator generator, Random rand, BlockPos pos, NoFeatureConfig config) {
		final BiomeDatabaseManager biomeDatabaseManager = DTResourceRegistries.getBiomeDatabaseManager();
        final TreeGenerator treeGenerator = TreeGenerator.getTreeGenerator();
		final ServerWorld serverWorld = world.getWorld();
		final ResourceLocation dimensionRegistryName = serverWorld.getDimensionKey().getLocation();

    	// Do not generate if the current dimension is blacklisted.
        if (biomeDatabaseManager.isDimensionBlacklisted(dimensionRegistryName))
            return false;

        // Grab biome data base for dimension.
        final BiomeDatabase biomeDatabase = biomeDatabaseManager.getDimensionDatabase(dimensionRegistryName);

        // Get chunk pos and create safe bounds, which ensure we do not try to generate in an unloaded chunk.
		final ChunkPos chunkPos = world.getChunk(pos).getPos();
		final SafeChunkBounds chunkBounds = new SafeChunkBounds(world, chunkPos);

		// Generate trees.
		treeGenerator.getCircleProvider().getPoissonDiscs(serverWorld, chunkPos)
				.forEach(c -> treeGenerator.makeTree(world, biomeDatabase, c, new GroundFinder(), chunkBounds));

        return true;
    }

}
