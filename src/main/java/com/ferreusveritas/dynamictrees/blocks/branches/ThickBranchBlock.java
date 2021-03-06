package com.ferreusveritas.dynamictrees.blocks.branches;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.CoordUtils.Surround;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BushBlock;
import net.minecraft.block.material.Material;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nonnull;

public class ThickBranchBlock extends BasicBranchBlock implements IMusable {

	public static final int RADMAX_THICK = 24;

	protected static final IntegerProperty RADIUS_DOUBLE = IntegerProperty.create("radius", 1, RADMAX_THICK); //39 ?

	public ThickBranchBlock(Material material, ResourceLocation registryName) {
		this(Properties.create(material), registryName);
	}

	public ThickBranchBlock(Properties properties, ResourceLocation registryName) {
		super(properties, registryName);

		cacheBranchThickStates();
	}

	public TrunkShellBlock getTrunkShell (){
		return DTRegistries.trunkShellBlock;
	}

	//We can't override this function since the "otherBlock" will not have been created yet.
	@Override
	public void cacheBranchStates() { }

	@Override
	public void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(RADIUS_DOUBLE);
	}

	public void cacheBranchThickStates() {
		setDefaultState(this.getStateContainer().getBaseState().with(RADIUS_DOUBLE, 1));

		branchStates = new BlockState[RADMAX_THICK + 1];

		//Cache the branch blocks states for rapid lookup
		branchStates[0] = Blocks.AIR.getDefaultState();

		for(int radius = 1; radius <= RADMAX_THICK; radius++) {
			branchStates[radius] = getDefaultState().with(ThickBranchBlock.RADIUS_DOUBLE, radius);
		}
	}

	///////////////////////////////////////////
	// GROWTH
	///////////////////////////////////////////

	@Override
	public int getRadius(BlockState blockState) {
		if (!(blockState.getBlock() instanceof ThickBranchBlock))
			return super.getRadius(blockState);
		return isSameTree(blockState) ? MathHelper.clamp(blockState.get(RADIUS_DOUBLE), 1, getMaxRadius()) : 0;
	}

	@Override
	public int setRadius(IWorld world, BlockPos pos, int radius, Direction originDir, int flags) {

		if (updateTrunkShells(world, pos, radius, flags)){
			return super.setRadius(world, pos, radius, originDir, flags);
		} else {
			return super.setRadius(world, pos, RADMAX_NORMAL, originDir, flags);
		}

	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		updateTrunkShells(worldIn, pos, getRadius(state), 6);
		super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
	}

	private boolean updateTrunkShells (IWorld world, BlockPos pos, int radius, int flags){
		//If the radius is <= 8 then we can just set the block as normal and move on
		if(radius <= RADMAX_NORMAL) {
			return true;
		}

		boolean setable = true;
		ReplaceableState[] repStates = new ReplaceableState[8];

		for(Surround dir : Surround.values()) {
			BlockPos dPos = pos.add(dir.getOffset());
			ReplaceableState rep = getReplaceability(world, dPos, pos);
			repStates[dir.ordinal()] = rep;
			if (rep == ReplaceableState.BLOCKING) {
				setable = false;
				break;
			}
		}

		if(setable) {
			for(Surround dir : Surround.values()) {
				BlockPos dPos = pos.add(dir.getOffset());
				ReplaceableState rep = repStates[dir.ordinal()];
				if(rep == ReplaceableState.REPLACEABLE) {
					world.setBlockState(dPos, getTrunkShell().getDefaultState().with(TrunkShellBlock.CORE_DIR, dir.getOpposite()), flags);
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public int getRadiusForConnection(BlockState blockState, IBlockReader world, BlockPos pos, BranchBlock from, Direction side, int fromRadius) {
		if (from instanceof ThickBranchBlock)
			return getRadius(blockState);
		return Math.min(getRadius(blockState), RADMAX_NORMAL);
	}

	@Override
	protected int getSideConnectionRadius(IBlockReader blockAccess, BlockPos pos, int radius, Direction side) {
		final BlockPos deltaPos = pos.offset(side);
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

		return Math.min(RADMAX_NORMAL, connectionRadius);
	}

	public ReplaceableState getReplaceability(IWorld world, BlockPos pos, BlockPos corePos) {

		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();

		if(block instanceof TrunkShellBlock) {
			//Determine if this shell belongs to the trunk.  Block otherwise.
			Surround surr = state.get(TrunkShellBlock.CORE_DIR);
			return pos.add(surr.getOffset()).equals(corePos) ? ReplaceableState.SHELL : ReplaceableState.BLOCKING;
		}

		if(state.getMaterial().isReplaceable() || block instanceof BushBlock) {
			return ReplaceableState.REPLACEABLE;
		}

		if(TreeHelper.isTreePart(block)) {
			return ReplaceableState.TREEPART;
		}

		if(block instanceof SurfaceRootBlock) {
			return ReplaceableState.TREEPART;
		}

		if(getFamily().getCommonSpecies().isAcceptableSoil(world, pos, state)) {
			return ReplaceableState.REPLACEABLE;
		}

		//TODO: Possible configurable whitelist for destructable blocks

		return ReplaceableState.BLOCKING;
	}

	enum ReplaceableState {
		SHELL,			//This indicates that the block is already a shell
		REPLACEABLE,	//This indicates that the block is truly replaceable and will be erased
		BLOCKING,		//This indicates that the block is not replaceable, will NOT be erased, and will prevent the tree from growing
		TREEPART		//This indicates that the block is part of a tree, will NOT be erase, and will NOT prevent the tree from growing
	}

	@Override
	public int getMaxRadius() {
		return RADMAX_THICK;
	}


	///////////////////////////////////////////
	// PHYSICAL BOUNDS
	///////////////////////////////////////////


	@Nonnull
    @Override
	public VoxelShape getShape(BlockState state, IBlockReader blockReader, BlockPos pos, ISelectionContext context) {
		int thisRadius = getRadius(state);
		if(thisRadius <= RADMAX_NORMAL) {
			return super.getShape(state, blockReader, pos, context);
		}

		double radius = thisRadius / 16.0;
		return VoxelShapes.create(new AxisAlignedBB(0.5 - radius, 0.0, 0.5 - radius, 0.5 + radius, 1.0, 0.5 + radius));
	}

	@Override
	public boolean isMusable() {
		return true;
	}

}