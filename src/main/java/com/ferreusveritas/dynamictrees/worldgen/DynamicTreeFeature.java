package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.ferreusveritas.dynamictrees.api.worldgen.IGroundFinder;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.Dimension;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;

import java.util.ArrayList;
import java.util.Random;

/**
 * @author Harley O'Connor
 */
public final class DynamicTreeFeature extends Feature<NoFeatureConfig> {

    public static class GroundFinder implements IGroundFinder {

		protected boolean inNetherRange(BlockPos pos) {
			return pos.getY() >= 0 && pos.getY() <= 128;
		}

        protected boolean isReplaceable(IWorld world, BlockPos pos){
            return world.isAirBlock(pos) && !world.getBlockState(pos).getMaterial().isLiquid();
        }

        protected ArrayList<Integer> findSubterraneanLayerHeights(IWorld world, BlockPos start) {

			BlockPos.Mutable pos = new BlockPos.Mutable(start.getX(), 0, start.getZ());

			ArrayList<Integer> layers = new ArrayList();

            while(inNetherRange(pos)) {
                while(!isReplaceable(world, pos) && inNetherRange(pos)) { pos.move(Direction.UP, 4); } //Zip up 4 blocks at a time until we hit air
                while(isReplaceable(world, pos) && inNetherRange(pos))  { pos.move(Direction.DOWN); } //Move down 1 block at a time until we hit not-air
                layers.add(pos.getY()); //Record this position
                pos.move(Direction.UP, 16); //Move up 16 blocks
                while(isReplaceable(world, pos) && inNetherRange(pos)) { pos.move(Direction.UP, 4); } //Zip up 4 blocks at a time until we hit ground
            }

			//Discard the last result as it's just the top of the biome(bedrock for nether)
			if (layers.size() > 0) {
				layers.remove(layers.size() - 1);
			}

			return layers;
		}

		protected BlockPos findSubterraneanGround(IWorld world, BlockPos start) {
			ArrayList<Integer> layers = findSubterraneanLayerHeights(world, start);
			if (layers.size() < 1) {
				return BlockPos.ZERO;
			}
			int y = layers.get(world.getRandom().nextInt(layers.size()));

			return new BlockPos(start.getX(), y, start.getZ());
		}

		protected boolean inOverworldRange(BlockPos pos) {
			return pos.getY() >= 0 && pos.getY() <= 255;
		}

		protected BlockPos findOverworldGround(IWorld world, BlockPos start) {

			IChunk chunk = world.getChunk(start); //We'll use a chunk for the search so we don't have to keep looking up the chunk for every block

			BlockPos.Mutable mPos = new BlockPos.Mutable(start.getX(), world.getHeight(Heightmap.Type.WORLD_SURFACE_WG, start.getX(), start.getZ()), start.getZ()).move(Direction.UP, 2);//Mutable allows us to change the test position easily
			while(inOverworldRange(mPos)) {

				BlockState state = chunk.getBlockState(mPos);
				Block testBlock = state.getBlock();

				if(testBlock != Blocks.AIR) {
					Material material = state.getMaterial();
					if (material == Material.EARTH || material == Material.WATER || //These will account for > 90% of blocks in the world so we can solve this early
							(state.getMaterial().blocksMovement() &&
                                    material != Material.LEAVES)) {
						return mPos.toImmutable();
					}
				}

				mPos.move(Direction.DOWN);
			}

			return BlockPos.ZERO;
		}

		@Override
		public BlockPos findGround(BiomeDataBase.BiomeEntry biomeEntry, ISeedReader world, BlockPos start) {
			return biomeEntry.isSubterraneanBiome() ? findSubterraneanGround(world, start) : findOverworldGround(world, start);
		}

	}

    public DynamicTreeFeature () {
        super(NoFeatureConfig.field_236558_a_);
        this.setRegistryName(new ResourceLocation(DynamicTrees.MODID, "tree"));
    }

    @Override
    public boolean generate(ISeedReader world, ChunkGenerator generator, Random rand, BlockPos pos, NoFeatureConfig config) {
		BiomeDataBase biomeDataBase = TreeGenerator.getTreeGenerator().getDefaultBiomeDataBase();
		BiomeDataBase.BiomeEntry biomeEntry = biomeDataBase.getEntry(world.getBiome(pos));
		BiomePropertySelectors.SpeciesSelection speciesSelection = biomeEntry.getSpeciesSelector().getSpecies(pos, world.getBlockState(pos), rand);

        GroundFinder groundFinder = new GroundFinder();
        BlockPos groundPos = groundFinder.findOverworldGround(world, pos);

        speciesSelection.getSpecies().generate(world, groundPos, world.getBiome(pos), rand, 8, new SafeChunkBounds(world, world.getChunk(pos).getPos()));

        return true;
    }

}
