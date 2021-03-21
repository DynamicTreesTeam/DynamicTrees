package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.api.worldgen.IGroundFinder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;

import java.util.ArrayList;

/**
 * This class is used to find a suitable area to generate a tree on the ground. It does
 * this based on whether the biome is marked as subterranean or not, which can be done by
 * the {@link BiomeDatabase}.
 */
public class GroundFinder implements IGroundFinder {

	protected boolean inNetherRange(final BlockPos pos) {
		return pos.getY() >= 0 && pos.getY() <= 128;
	}

	protected boolean isReplaceable(final IWorld world, final BlockPos pos){
		return world.isAirBlock(pos) && !world.getBlockState(pos).getMaterial().isLiquid();
	}

	protected ArrayList<Integer> findSubterraneanLayerHeights(final IWorld world, final BlockPos start) {

		final BlockPos.Mutable pos = new BlockPos.Mutable(start.getX(), 0, start.getZ());
		final ArrayList<Integer> layers = new ArrayList<>();

		while (inNetherRange(pos)) {
			while (!isReplaceable(world, pos) && inNetherRange(pos)) { pos.move(Direction.UP, 4); } // Zip up 4 blocks at a time until we hit air
			while (isReplaceable(world, pos) && inNetherRange(pos))  { pos.move(Direction.DOWN); } // Move down 1 block at a time until we hit not-air
			layers.add(pos.getY()); //Record this position
			pos.move(Direction.UP, 16); //Move up 16 blocks
			while (isReplaceable(world, pos) && inNetherRange(pos)) { pos.move(Direction.UP, 4); } // Zip up 4 blocks at a time until we hit ground
		}

		// Discard the last result as it's just the top of the biome(bedrock for nether)
		if (layers.size() > 0) {
			layers.remove(layers.size() - 1);
		}

		return layers;
	}

	protected BlockPos findSubterraneanGround(final IWorld world, final BlockPos start) {
		final ArrayList<Integer> layers = findSubterraneanLayerHeights(world, start);
		if (layers.size() < 1) {
			return BlockPos.ZERO;
		}
		final int y = layers.get(world.getRandom().nextInt(layers.size()));

		return new BlockPos(start.getX(), y, start.getZ());
	}

	protected boolean inOverworldRange(final BlockPos pos) {
		return pos.getY() >= 0 && pos.getY() <= 255;
	}

	protected BlockPos findOverworldGround(final IWorld world, final BlockPos start) {
		final IChunk chunk = world.getChunk(start); // We'll use a chunk for the search so we don't have to keep looking up the chunk for every block
		final BlockPos.Mutable mPos = new BlockPos.Mutable(start.getX(), world.getHeight(Heightmap.Type.WORLD_SURFACE_WG, start.getX(), start.getZ()), start.getZ()).move(Direction.UP, 2); // Mutable allows us to change the test position easily

		while (inOverworldRange(mPos)) {
			final BlockState state = chunk.getBlockState(mPos);
			final Block testBlock = state.getBlock();

			if (testBlock != Blocks.AIR) {
				final Material material = state.getMaterial();

				if (material == Material.EARTH || material == Material.WATER || // These will account for > 90% of blocks in the world so we can solve this early
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
	public BlockPos findGround(final BiomeDatabase.Entry entry, final ISeedReader world, final BlockPos start) {
		return entry.isSubterraneanBiome() ? findSubterraneanGround(world, start) : findOverworldGround(world, start);
	}

}
