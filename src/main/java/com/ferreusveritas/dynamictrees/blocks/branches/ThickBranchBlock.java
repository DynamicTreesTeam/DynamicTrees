package com.ferreusveritas.dynamictrees.blocks.branches;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.systems.BranchConnectables;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.CoordUtils.Surround;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BushBlock;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.Fluids;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ThickBranchBlock extends BasicBranchBlock implements IMusable {

	public static final int MAX_RADIUS_TICK = 24;

	protected static final IntegerProperty RADIUS_DOUBLE = IntegerProperty.create("radius", 1, MAX_RADIUS_TICK); //39 ?

	public ThickBranchBlock(Material material) {
		this(Properties.of(material));
	}

	public ThickBranchBlock(Properties properties) {
		super(properties, RADIUS_DOUBLE, MAX_RADIUS_TICK);
	}

	public TrunkShellBlock getTrunkShell (){
		return DTRegistries.TRUNK_SHELL;
	}

	@Override
	public void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(RADIUS_DOUBLE).add(WATERLOGGED);
	}

	///////////////////////////////////////////
	// GROWTH
	///////////////////////////////////////////

	@Override
	public int getRadius(BlockState state) {
		if (!(state.getBlock() instanceof ThickBranchBlock))
			return super.getRadius(state);
		return isSameTree(state) ? MathHelper.clamp(state.getValue(RADIUS_DOUBLE), 1, getMaxRadius()) : 0;
	}

	@Override
	public int setRadius(IWorld world, BlockPos pos, int radius, @Nullable Direction originDir, int flags) {
		if (this.updateTrunkShells(world, pos, radius, flags))
			return super.setRadius(world, pos, radius, originDir, flags);
		return super.setRadius(world, pos, MAX_RADIUS, originDir, flags);
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		updateTrunkShells(worldIn, pos, getRadius(state), 6);
		super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
	}

	private boolean updateTrunkShells (IWorld world, BlockPos pos, int radius, int flags){
		// If the radius is <= 8 then we can just set the block as normal and move on.
		if (radius <= MAX_RADIUS) {
			return true;
		}

		boolean setable = true;
		final ReplaceableState[] repStates = new ReplaceableState[8];

		for (Surround dir : Surround.values()) {
			final BlockPos dPos = pos.offset(dir.getOffset());
			final ReplaceableState rep = getReplaceability(world, dPos, pos);

			repStates[dir.ordinal()] = rep;

			if (rep == ReplaceableState.BLOCKING) {
				setable = false;
				break;
			}
		}

		if (setable) {
			for (Surround dir : Surround.values()) {
				final BlockPos dPos = pos.offset(dir.getOffset());
				final ReplaceableState rep = repStates[dir.ordinal()];
				final boolean replacingWater = world.getBlockState(dPos).getFluidState() == Fluids.WATER.getSource(false);

				if (rep == ReplaceableState.REPLACEABLE)
					world.setBlock(dPos, getTrunkShell().defaultBlockState().setValue(TrunkShellBlock.CORE_DIR, dir.getOpposite()).setValue(TrunkShellBlock.WATERLOGGED, replacingWater), flags);
			}
			return true;
		}
		return false;
	}

	@Override
	public int getRadiusForConnection(BlockState state, IBlockReader reader, BlockPos pos, BranchBlock from, Direction side, int fromRadius) {
		if (from instanceof ThickBranchBlock)
			return getRadius(state);
		return Math.min(getRadius(state), MAX_RADIUS);
	}

	@Override
	protected int getSideConnectionRadius(IBlockReader blockAccess, BlockPos pos, int radius, Direction side) {
		final BlockPos deltaPos = pos.relative(side);
		final BlockState blockState = CoordUtils.getStateSafe(blockAccess, deltaPos);

		if (blockState == null)
			return 0;

		final int connectionRadius = TreeHelper.getTreePart(blockState).getRadiusForConnection(blockState, blockAccess, deltaPos, this, side, radius);

//			if (radius > 8) {
//				if (side == Direction.DOWN) {
//					return connectionRadius >= radius ? 1 : 0;
//				} else if (side == Direction.UP) {
//					return connectionRadius >= radius ? 2 : connectionRadius > 0 ? 1 : 0;
//				}
//			}

		return Math.min(MAX_RADIUS, connectionRadius);
	}

	public ReplaceableState getReplaceability(IWorld world, BlockPos pos, BlockPos corePos) {

		final BlockState state = world.getBlockState(pos);
		final Block block = state.getBlock();

		if (block instanceof TrunkShellBlock) {
			// Determine if this shell belongs to the trunk.  Block otherwise.
			Surround surr = state.getValue(TrunkShellBlock.CORE_DIR);
			return pos.offset(surr.getOffset()).equals(corePos) ? ReplaceableState.SHELL : ReplaceableState.BLOCKING;
		}

		if (state.getMaterial().isReplaceable() || block instanceof BushBlock) {
			return ReplaceableState.REPLACEABLE;
		}

		if (TreeHelper.isTreePart(block)) {
			return ReplaceableState.TREEPART;
		}

		if (block instanceof SurfaceRootBlock) {
			return ReplaceableState.TREEPART;
		}

		if (BranchConnectables.isBlockConnectable(block)){
			return ReplaceableState.TREEPART;
		}

		if (this.getFamily().getCommonSpecies().isAcceptableSoil(world, pos, state)) {
			return ReplaceableState.REPLACEABLE;
		}

		return ReplaceableState.BLOCKING;
	}

	enum ReplaceableState {
		SHELL,			// This indicates that the block is already a shell.
		REPLACEABLE,	// This indicates that the block is truly replaceable and will be erased.
		BLOCKING,		// This indicates that the block is not replaceable, will NOT be erased, and will prevent the tree from growing.
		TREEPART		// This indicates that the block is part of a tree, will NOT be erase, and will NOT prevent the tree from growing.
	}

	@Override
	public int getMaxRadius() {
		return MAX_RADIUS_TICK;
	}


	///////////////////////////////////////////
	// PHYSICAL BOUNDS
	///////////////////////////////////////////


	@Nonnull
    @Override
	public VoxelShape getShape(BlockState state, IBlockReader blockReader, BlockPos pos, ISelectionContext context) {
		final int thisRadius = getRadius(state);
		if (thisRadius <= MAX_RADIUS) {
			return super.getShape(state, blockReader, pos, context);
		}

		final double radius = thisRadius / 16.0;
		return VoxelShapes.create(new AxisAlignedBB(0.5 - radius, 0.0, 0.5 - radius, 0.5 + radius, 1.0, 0.5 + radius));
	}

	@Override
	public boolean isMusable(IBlockReader world, BlockState state, BlockPos pos) {
		return getRadius(state) > 8;
	}

}