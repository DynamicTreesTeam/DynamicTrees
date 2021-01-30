package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.DynamicTrees;
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
        this.setRegistryName(new ResourceLocation(DynamicTrees.MODID, "tree"));
    }

    @Override
    public boolean generate(ISeedReader world, ChunkGenerator generator, Random rand, BlockPos pos, NoFeatureConfig config) {
		final TreeGenerator treeGenerator = TreeGenerator.getTreeGenerator();
		final ServerWorld serverWorld = world.getWorld();

    	// Do not generate if the current dimension is blacklisted.
        if (treeGenerator.isDimensionBlacklisted(serverWorld.getDimensionKey().getLocation()))
            return false;

        // Grab biome data base for dimension.
        final BiomeDataBase biomeDataBase = treeGenerator.getBiomeDataBase(serverWorld);

        // Get chunk pos and create safe bounds, which ensure we do not try to generate in an unloaded chunk.
		final ChunkPos chunkPos = world.getChunk(pos).getPos();
		final SafeChunkBounds chunkBounds = new SafeChunkBounds(world, chunkPos);

		// Generate trees.
		treeGenerator.getCircleProvider().getPoissonDiscs(world.getWorld(), chunkPos)
				.forEach(c -> treeGenerator.makeTree(world, biomeDataBase, c, new GroundFinder(), chunkBounds));

        return true;
    }

}
