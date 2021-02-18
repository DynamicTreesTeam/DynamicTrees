package com.ferreusveritas.dynamictrees.systems.genfeatures;

import com.ferreusveritas.dynamictrees.api.IPostGenFeature;
import com.ferreusveritas.dynamictrees.blocks.leaves.DynamicLeavesBlock;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.GenFeatureBlockProperty;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.GenFeatureProperty;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.VineBlock;
import net.minecraft.state.BooleanProperty;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nullable;
import java.util.List;

public class VinesGenFeature extends GenFeature implements IPostGenFeature {

	/**
	 * Vine Type - this tells the generator which side the vines generate on - ceiling for vines that grow from the ceiling
	 * downward like weeping vines, floor for vines that grow from the ground upward like twisting vines, and side for vines
	 * that grow on the side of blocks likes regular vines.
	 */
	public enum VineType {
		CEILING,
		FLOOR, // This has not been tested properly and may need changes.
		SIDE // Note that side vines will assume the vine uses the same blockstates as a vanilla vine.
	}

	protected final BooleanProperty[] sideVineStates = new BooleanProperty[] {null, null, VineBlock.NORTH, VineBlock.SOUTH, VineBlock.WEST, VineBlock.EAST};

	public static final GenFeatureProperty<Integer> MAX_LENGTH = GenFeatureProperty.createIntegerProperty("max_length");
	public static final GenFeatureProperty<Block> VINE_BLOCK = new GenFeatureBlockProperty("vine");
	public static final GenFeatureProperty<Block> TIP_BLOCK = new GenFeatureBlockProperty("vine_tip");
	public static final GenFeatureProperty<VineType> VINE_TYPE = GenFeatureProperty.createProperty("vine_type", VineType.class);

	public VinesGenFeature(ResourceLocation registryName) {
		super(registryName, QUANTITY, MAX_LENGTH, VERTICAL_SPREAD, RAY_DISTANCE, VINE_BLOCK, TIP_BLOCK, VINE_TYPE);
	}

	@Override
	public ConfiguredGenFeature<?> createDefaultConfiguration() {
		return super.createDefaultConfiguration().with(QUANTITY, 4).with(MAX_LENGTH, 8).with(VERTICAL_SPREAD, 60f)
				.with(RAY_DISTANCE, 5f).with(VINE_BLOCK, Blocks.VINE).with(TIP_BLOCK, null).with(VINE_TYPE, VineType.SIDE);
	}

	@Override
	public boolean postGeneration(ConfiguredGenFeature<?> configuredGenFeature, IWorld world, BlockPos rootPos, Species species, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, BlockState initialDirtState, Float seasonValue, Float seasonFruitProductionFactor) {
		if(safeBounds != SafeChunkBounds.ANY) { //worldgen
			if(!endPoints.isEmpty()) {
				for(int i = 0; i < configuredGenFeature.get(QUANTITY); i++) {
					BlockPos endPoint = endPoints.get(world.getRandom().nextInt(endPoints.size()));

					if (configuredGenFeature.get(VINE_TYPE) == VineType.SIDE)
						this.addSideVines(configuredGenFeature, world, species, rootPos, endPoint, safeBounds);
					else this.addVerticalVines(configuredGenFeature, world, species, rootPos, endPoint, safeBounds);
				}
				return true;
			}
		}

		return false;
	}

	protected void addSideVines(ConfiguredGenFeature<?> configuredGenFeature, IWorld world, Species species, BlockPos rootPos, BlockPos branchPos, SafeChunkBounds safeBounds) {
		BlockRayTraceResult result = null;
		{
			// Uses branch ray tracing to find a place on the side of the tree to begin generating vines.
			RayTraceResult ray = CoordUtils.branchRayTrace(world, species, rootPos, branchPos, 90, configuredGenFeature.get(VERTICAL_SPREAD), configuredGenFeature.get(RAY_DISTANCE), safeBounds);
			if (ray instanceof BlockRayTraceResult) {
				result = (BlockRayTraceResult) ray;
			}
		}

		if (result == null) return;

		BlockPos vinePos = result.getPos().offset(result.getFace());
		if (vinePos == BlockPos.ZERO || !safeBounds.inBounds(vinePos, true)) return;

		BooleanProperty vineSide = sideVineStates[result.getFace().getOpposite().getIndex()];
		if(vineSide == null) return;

		BlockState vineState = configuredGenFeature.get(VINE_BLOCK).getDefaultState().with(vineSide, true);
		this.placeVines(world, vinePos, vineState, configuredGenFeature.get(MAX_LENGTH),
				configuredGenFeature.get(TIP_BLOCK), configuredGenFeature.get(VINE_TYPE));
	}

    protected void addVerticalVines(ConfiguredGenFeature<?> configuredGenFeature, IWorld world, Species species, BlockPos rootPos, BlockPos branchPos, SafeChunkBounds safeBounds) {
		// Uses fruit ray trace method to grab a position under the tree's leaves.
		BlockPos vinePos = CoordUtils.getRayTraceFruitPos(world, species, rootPos, branchPos, safeBounds);

		if (!safeBounds.inBounds(vinePos, true)) return;

		if (configuredGenFeature.get(VINE_TYPE) == VineType.FLOOR) {
			vinePos = this.findTreeTop(world, vinePos);
		}

		if (vinePos == BlockPos.ZERO) return;

		this.placeVines(world, vinePos, configuredGenFeature.get(VINE_BLOCK).getDefaultState(), configuredGenFeature.get(MAX_LENGTH),
				configuredGenFeature.get(TIP_BLOCK), configuredGenFeature.get(VINE_TYPE));
	}

	// This is WIP (and isn't needed in the base mod anyway, as well as the fact that there's almost certainly a better way of doing this).
	private BlockPos findTreeTop(IWorld world, BlockPos vinePos) {
		BlockPos.Mutable mPos = new BlockPos.Mutable(vinePos.getX(), vinePos.getY(), vinePos.getZ());
		do {
			mPos.setY(mPos.getY() + 1);
		} while (world.getBlockState(vinePos).getBlock() instanceof DynamicLeavesBlock);
		vinePos = mPos;

		if (!world.getBlockState(vinePos).isAir()) {
			return BlockPos.ZERO;
		}
		return vinePos;
	}

	protected void placeVines(IWorld world, BlockPos vinePos, BlockState vinesState, int maxLength, @Nullable Block tipBlock, VineType vineType) {
		// Generate a random length for the vine.
		int len = MathHelper.clamp(world.getRandom().nextInt(maxLength) + 3, 3, maxLength);
		BlockPos.Mutable mPos = new BlockPos.Mutable(vinePos.getX(), vinePos.getY(), vinePos.getZ());

		for (int i = 0; i < len; i++) {
			if (world.isAirBlock(mPos)) {
				// Set the current block either to a vine block or a tip block if it's set.
				world.setBlockState(mPos, tipBlock != null && i == len - 1 ? tipBlock.getDefaultState() : vinesState, 3);
				// Move current position down/up depending on vine type.
				mPos.setY(mPos.getY() + (vineType != VineType.FLOOR ? - 1 : 1));
			} else {
				break;
			}
		}
	}

}
