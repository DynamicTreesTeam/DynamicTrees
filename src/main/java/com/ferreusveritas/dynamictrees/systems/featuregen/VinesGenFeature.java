package com.ferreusveritas.dynamictrees.systems.featuregen;

import com.ferreusveritas.dynamictrees.api.IPostGenFeature;
import com.ferreusveritas.dynamictrees.blocks.leaves.DynamicLeavesBlock;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.VineBlock;
import net.minecraft.state.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;

import java.util.List;

public class VinesGenFeature implements IPostGenFeature {

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
	protected int qty = 4;
	protected int maxLength = 8;
	protected float verSpread = 60;
	protected float rayDistance = 5;
	protected Block vineBlock;
	protected Block tipBlock = null;
	protected VineType vineType;

	public VinesGenFeature() {
		this(Blocks.VINE, VineType.SIDE);
	}

	public VinesGenFeature(Block vineBlock, VineType vineType) {
		this.vineBlock = vineBlock;
		this.vineType = vineType;
	}

	public VinesGenFeature setQuantity(int qty) {
		this.qty = qty;
		return this;
	}

	public VinesGenFeature setMaxLength(int length) {
		this.maxLength = length;
		return this;
	}

	public VinesGenFeature setVerSpread(float verSpread) {
		this.verSpread = verSpread;
		return this;
	}

	public VinesGenFeature setRayDistance(float rayDistance) {
		this.rayDistance = rayDistance;
		return this;
	}

	public VinesGenFeature setVineBlock(Block vineBlock) {
		this.vineBlock = vineBlock;
		return this;
	}

	/**
	 * If a vine has a tip block, set it here and it will be generated on the end of the vine.
	 *
	 * @param tipBlock The tip block to set.
	 * @return This vine feature.
	 */
	public VinesGenFeature setTipBlock(Block tipBlock) {
		this.tipBlock = tipBlock;
		return this;
	}

	/**
	 * Sets the current {@link VineType}.
	 *
	 * @param vineType The vine type to set.
	 * @return This vine feature.
	 */
	public VinesGenFeature setVineType(VineType vineType) {
		this.vineType = vineType;
		return this;
	}

	@Override
	public boolean postGeneration(IWorld world, BlockPos rootPos, Species species, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, BlockState initialDirtState, Float seasonValue, Float seasonFruitProductionFactor) {
		if(safeBounds != SafeChunkBounds.ANY) { //worldgen
			if(!endPoints.isEmpty()) {
				for(int i = 0; i < qty; i++) {
					BlockPos endPoint = endPoints.get(world.getRandom().nextInt(endPoints.size()));

					if (this.vineType == VineType.SIDE)
						this.addSideVines(world, species, rootPos, endPoint, safeBounds);
					else this.addVerticalVines(world, species, rootPos, endPoint, safeBounds);
				}
				return true;
			}
		}

		return false;
	}

	protected void addSideVines(IWorld world, Species species, BlockPos rootPos, BlockPos branchPos, SafeChunkBounds safeBounds) {
		BlockRayTraceResult result = null;
		{
			// Uses branch ray tracing to find a place on the side of the tree to begin generating vines.
			RayTraceResult ray = CoordUtils.branchRayTrace(world, species, rootPos, branchPos, 90, this.verSpread, this.rayDistance, safeBounds);
			if (ray instanceof BlockRayTraceResult) {
				result = (BlockRayTraceResult) ray;
			}
		}

		if (result == null) return;

		BlockPos vinePos = result.getPos().offset(result.getFace());
		if (vinePos == BlockPos.ZERO || !safeBounds.inBounds(vinePos, true)) return;

		BooleanProperty vineSide = sideVineStates[result.getFace().getOpposite().getIndex()];
		if(vineSide == null) return;

		BlockState vineState = vineBlock.getDefaultState().with(vineSide, true);
		this.placeVines(world, vinePos, vineState);
	}

    protected void addVerticalVines(IWorld world, Species species, BlockPos rootPos, BlockPos branchPos, SafeChunkBounds safeBounds) {
		// Uses fruit ray trace method to grab a position under the tree's leaves.
		BlockPos vinePos = CoordUtils.getRayTraceFruitPos(world, species, rootPos, branchPos, safeBounds);

		if (!safeBounds.inBounds(vinePos, true)) return;

		if (this.vineType == VineType.FLOOR) {
			vinePos = this.findTreeTop(world, vinePos);
		}

		if (vinePos == BlockPos.ZERO) return;

		this.placeVines(world, vinePos, vineBlock.getDefaultState());
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

	protected void placeVines(IWorld world, BlockPos vinePos, BlockState vinesState) {
		// Generate a random length for the vine.
		int len = MathHelper.clamp(world.getRandom().nextInt(this.maxLength) + 3, 3, this.maxLength);
		BlockPos.Mutable mPos = new BlockPos.Mutable(vinePos.getX(), vinePos.getY(), vinePos.getZ());

		for (int i = 0; i < len; i++) {
			if (world.isAirBlock(mPos)) {
				// Set the current block either to a vine block or a tip block if it's set.
				world.setBlockState(mPos, this.tipBlock != null && i == len - 1 ? this.tipBlock.getDefaultState() : vinesState, 3);
				// Move current position down/up depending on vine type.
				mPos.setY(mPos.getY() + (this.vineType != VineType.FLOOR ? - 1 : 1));
			} else {
				break;
			}
		}
	}

}
