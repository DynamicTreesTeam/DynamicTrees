package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.api.worldgen.IGroundFinder;
import com.ferreusveritas.dynamictrees.blocks.DynamicSaplingBlock;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.Heightmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is used to find a suitable area to generate a tree on the ground. It does
 * this based on whether the biome is marked as subterranean or not, which can be done by
 * the {@link BiomeDatabase}.
 */
public class GroundFinder implements IGroundFinder {

	protected boolean isReplaceable(final IWorld world, final BlockPos pos){
		return (world.isEmptyBlock(pos) || !world.getBlockState(pos).getMaterial().blocksMotion() || world.getBlockState(pos).getBlock() instanceof DynamicSaplingBlock) && !world.getBlockState(pos).getMaterial().isLiquid();
	}

	protected boolean inRange(final BlockPos pos, final int minY, final int maxY) {
		return pos.getY() >= minY && pos.getY() <= maxY;
	}

	protected int getTopY(final IWorld world, final BlockPos pos) {
		return world.getChunk(pos).getHeight(Heightmap.Type.WORLD_SURFACE_WG, pos.getX(), pos.getZ());
	}

	protected ArrayList<Integer> findSubterraneanLayerHeights(final IWorld world, final BlockPos start) {
		final int maxY = this.getTopY(world, start);

		final BlockPos.Mutable pos = new BlockPos.Mutable(start.getX(), 0, start.getZ());
		final ArrayList<Integer> layers = new ArrayList<>();

		while (this.inRange(pos, 0, maxY)) {
			while (!isReplaceable(world, pos) && this.inRange(pos, 0, maxY)) pos.move(Direction.UP, 4); // Zip up 4 blocks at a time until we hit air
			while (isReplaceable(world, pos) && this.inRange(pos, 0, maxY)) pos.move(Direction.DOWN); // Move down 1 block at a time until we hit not-air
			layers.add(pos.getY()); // Record this position
			pos.move(Direction.UP, 16); // Move up 16 blocks
			while (isReplaceable(world, pos) && this.inRange(pos, 0, maxY)) pos.move(Direction.UP, 4); // Zip up 4 blocks at a time until we hit ground
		}

		// Discard the last result as it's just the top of the biome(bedrock for nether)
		if (layers.size() > 0) {
			layers.remove(layers.size() - 1);
		}

		return layers;
	}

	protected List<BlockPos> findSubterraneanGround(final IWorld world, final BlockPos start) {
		final ArrayList<Integer> layers = findSubterraneanLayerHeights(world, start);
		if (layers.size() < 1) {
			return Collections.singletonList(BlockPos.ZERO);
		}
		List<BlockPos> positions = new LinkedList<>();
		for (int y : layers){
			positions.add(new BlockPos(start.getX(), y, start.getZ()));
		}

		return positions;
	}

	protected List<BlockPos> findOverworldGround(final IWorld world, final BlockPos pos) {
		final int initialY = world.getHeight(Heightmap.Type.WORLD_SURFACE_WG, pos.getX(), pos.getZ()) + 1;

		// Use world surface worldgen heightmap to find the ground position.
		final BlockPos.Mutable groundPos = new BlockPos.Mutable(pos.getX(), initialY, pos.getZ());

		while (this.inRange(groundPos, 0, initialY)) {
			final BlockState state = world.getBlockState(groundPos);

			if (state.getBlock() != Blocks.AIR) {
				final Material material = state.getMaterial();

				if (material == Material.DIRT || material == Material.WATER || // These will account for > 90% of blocks in the world so we can solve this early
						(state.getMaterial().blocksMotion() &&
								material != Material.LEAVES /* && block#isFoliage? */)) {
					return Collections.singletonList(groundPos.immutable());
				}
			}

			groundPos.move(Direction.DOWN);
		}

		return Collections.singletonList(BlockPos.ZERO);
	}

	@Override
	public List<BlockPos> findGround(final BiomeDatabase.Entry entry, final ISeedReader world, final BlockPos start) {
		return entry.isSubterraneanBiome() ? findSubterraneanGround(world, start) : findOverworldGround(world, start);
	}

}
