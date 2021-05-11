package com.ferreusveritas.dynamictrees.systems.genfeatures;

import com.ferreusveritas.dynamictrees.api.IPostGenFeature;
import com.ferreusveritas.dynamictrees.api.IPostGrowFeature;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.blocks.leaves.DynamicLeavesBlock;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.GenFeatureProperty;
import com.ferreusveritas.dynamictrees.systems.nodemappers.FindEndsNode;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import net.minecraft.block.*;
import net.minecraft.state.BooleanProperty;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nullable;
import java.util.List;

public class VinesGenFeature extends GenFeature implements IPostGenFeature, IPostGrowFeature {

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
	public static final GenFeatureProperty<Block> BLOCK = GenFeatureProperty.createBlockProperty("block");
	public static final GenFeatureProperty<Block> TIP_BLOCK = GenFeatureProperty.createBlockProperty("tip_block");
	public static final GenFeatureProperty<VineType> VINE_TYPE = GenFeatureProperty.createProperty("vine_type", VineType.class);
	public static final GenFeatureProperty<Integer> FRUITING_RADIUS = GenFeatureProperty.createIntegerProperty("fruiting_radius");

	public VinesGenFeature(ResourceLocation registryName) {
		super(registryName, QUANTITY, MAX_LENGTH, VERTICAL_SPREAD, RAY_DISTANCE, BLOCK, TIP_BLOCK, VINE_TYPE, FRUITING_RADIUS);
	}

	@Override
	public ConfiguredGenFeature<GenFeature> createDefaultConfiguration() {
		return super.createDefaultConfiguration().with(QUANTITY, 4).with(MAX_LENGTH, 8).with(VERTICAL_SPREAD, 60f)
				.with(RAY_DISTANCE, 5f).with(BLOCK, Blocks.VINE).with(TIP_BLOCK, null).with(VINE_TYPE, VineType.SIDE).with(FRUITING_RADIUS, -1);
	}

	@Override
	public boolean postGeneration(ConfiguredGenFeature<?> configuredGenFeature, IWorld world, BlockPos rootPos, Species species, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, BlockState initialDirtState, Float seasonValue, Float seasonFruitProductionFactor) {
		if(safeBounds != SafeChunkBounds.ANY) { //worldgen
			if(!endPoints.isEmpty()) {
				for(int i = 0; i < configuredGenFeature.get(QUANTITY); i++) {
					BlockPos endPoint = endPoints.get(world.getRandom().nextInt(endPoints.size()));

					if (configuredGenFeature.get(VINE_TYPE) == VineType.SIDE)
						this.addSideVines(configuredGenFeature, world, species, rootPos, endPoint, safeBounds, true);
					else this.addVerticalVines(configuredGenFeature, world, species, rootPos, endPoint, safeBounds, true);
				}
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean postGrow(ConfiguredGenFeature<?> configuredGenFeature, World world, BlockPos rootPos, BlockPos treePos, Species species, int fertility, boolean natural) {
		int fruitingRadius = configuredGenFeature.get(FRUITING_RADIUS);
		if (fruitingRadius < 0) return false;
		BlockState blockState = world.getBlockState(treePos);
		BranchBlock branch = TreeHelper.getBranch(blockState);

		if(branch != null && branch.getRadius(blockState) >= fruitingRadius && natural) {
			if (species.seasonalFruitProductionFactor(world, rootPos) > world.random.nextFloat()) {
				FindEndsNode endFinder = new FindEndsNode();
				TreeHelper.startAnalysisFromRoot(world, rootPos, new MapSignal(endFinder));
				List<BlockPos> endPoints = endFinder.getEnds();
				int qty = configuredGenFeature.get(QUANTITY);
				if (!endPoints.isEmpty()) {
					for(int i = 0; i < qty; i++) {
						BlockPos endPoint = endPoints.get(world.getRandom().nextInt(endPoints.size()));
						if (configuredGenFeature.get(VINE_TYPE) == VineType.SIDE)
							this.addSideVines(configuredGenFeature, world, species, rootPos, endPoint, SafeChunkBounds.ANY, false);
						else this.addVerticalVines(configuredGenFeature, world, species, rootPos, endPoint, SafeChunkBounds.ANY, false);
					}
					return true;
				}
			}
		}

		return true;
	}


	protected void addSideVines(ConfiguredGenFeature<?> configuredGenFeature, IWorld world, Species species, BlockPos rootPos, BlockPos branchPos, SafeChunkBounds safeBounds, boolean worldgen) {
		BlockRayTraceResult result = null;
		{
			// Uses branch ray tracing to find a place on the side of the tree to begin generating vines.
			RayTraceResult ray = CoordUtils.branchRayTrace(world, species, rootPos, branchPos, 90, configuredGenFeature.get(VERTICAL_SPREAD), configuredGenFeature.get(RAY_DISTANCE), safeBounds);
			if (ray instanceof BlockRayTraceResult) {
				result = (BlockRayTraceResult) ray;
			}
		}

		if (result == null) return;

		BlockPos vinePos = result.getBlockPos().relative(result.getDirection());
		if (vinePos == BlockPos.ZERO || !safeBounds.inBounds(vinePos, true)) return;

		BooleanProperty vineSide = sideVineStates[result.getDirection().getOpposite().get3DDataValue()];
		if(vineSide == null) return;

		BlockState vineState = configuredGenFeature.get(BLOCK).defaultBlockState().setValue(vineSide, true);
		this.placeVines(world, vinePos, vineState, configuredGenFeature.get(MAX_LENGTH), null, configuredGenFeature.get(VINE_TYPE), worldgen);
	}

    protected void addVerticalVines(ConfiguredGenFeature<?> configuredGenFeature, IWorld world, Species species, BlockPos rootPos, BlockPos branchPos, SafeChunkBounds safeBounds, boolean worldgen) {
		// Uses fruit ray trace method to grab a position under the tree's leaves.
		BlockPos vinePos = CoordUtils.getRayTraceFruitPos(world, species, rootPos, branchPos, safeBounds);

		if (!safeBounds.inBounds(vinePos, true)) return;

		if (configuredGenFeature.get(VINE_TYPE) == VineType.FLOOR) {
			vinePos = this.findGround(world, vinePos);
		}

		if (vinePos == BlockPos.ZERO) return;

		this.placeVines(world, vinePos, configuredGenFeature.get(BLOCK).defaultBlockState(), configuredGenFeature.get(MAX_LENGTH),
				configuredGenFeature.get(TIP_BLOCK).defaultBlockState().setValue(AbstractTopPlantBlock.AGE, worldgen?25:0), configuredGenFeature.get(VINE_TYPE), worldgen);
	}

	// This is WIP (and isn't needed in the base mod anyway, as well as the fact that there's almost certainly a better way of doing this).
	private BlockPos findGround(IWorld world, BlockPos vinePos) {
		BlockPos.Mutable mPos = new BlockPos.Mutable(vinePos.getX(), vinePos.getY(), vinePos.getZ());
		do {
			mPos.move(Direction.DOWN);
			if (mPos.getY() <= 0) return BlockPos.ZERO;
		} while (world.isEmptyBlock(vinePos) || world.getBlockState(vinePos).getBlock() instanceof DynamicLeavesBlock);

		return mPos.above();
	}

	protected void placeVines(IWorld world, BlockPos vinePos, BlockState vinesState, int maxLength, @Nullable BlockState tipBlock, VineType vineType, boolean worldgen) {
		// Generate a random length for the vine.
		int len = 1;
		if (worldgen) len = MathHelper.clamp(world.getRandom().nextInt(maxLength) + 3, 3, maxLength);
		BlockPos.Mutable mPos = new BlockPos.Mutable(vinePos.getX(), vinePos.getY(), vinePos.getZ());

		for (int i = 0; i < len; i++) {
			if (world.isEmptyBlock(mPos)) {
				// Set the current block either to a vine block or a tip block if it's set.
				world.setBlock(mPos, (tipBlock != null && i == len - 1)?tipBlock:vinesState, 3);
				// Move current position down/up depending on vine type.
				mPos.setY(mPos.getY() + (vineType != VineType.FLOOR ? - 1 : 1));
			} else {
				break;
			}
		}
	}

}
