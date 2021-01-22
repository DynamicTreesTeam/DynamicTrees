package com.ferreusveritas.dynamictrees.blocks.branches;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.event.SafeChunkEvents;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;
import com.ferreusveritas.dynamictrees.util.CoordUtils.Surround;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BushBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

//TODO: 1.14.4 This gets flattened into the normal block branch with radius 1 - 32 and eliminated 

public class ThickBranchBlock extends BasicBranchBlock implements IMusable {

//	protected static final AxisAlignedBB maxBranchBB = new AxisAlignedBB(-1, 0, -1, 2, 1, 2);
	public static final int RADMAX_THICK = 24;

	protected static final IntegerProperty RADIUSDOUBLE = IntegerProperty.create("radius", 1, RADMAX_THICK); //39 ?

	public ThickBranchBlock(String name) {
		this(Properties.create(Material.WOOD),name);
	}

	public ThickBranchBlock(Properties properties, String name) {
		this(properties, name, false);

		cacheBranchThickStates();
	}

	protected ThickBranchBlock(Properties properties, String name, boolean extended) {
		super(properties, name);
		this.setDefaultState(this.getDefaultState().with(RADIUSDOUBLE, 1));
	}

	public TrunkShellBlock getTrunkShell (){
		return DTRegistries.trunkShellBlock;
	}

	//We can't override this function since the "otherBlock" will not have been created yet.
	@Override
	public void cacheBranchStates() { }

	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(RADIUSDOUBLE);
	}

	public void cacheBranchThickStates() {
		setDefaultState(this.getStateContainer().getBaseState().with(RADIUSDOUBLE, 1));

		branchStates = new BlockState[RADMAX_THICK + 1];

		//Cache the branch blocks states for rapid lookup
		branchStates[0] = Blocks.AIR.getDefaultState();

		for(int radius = 1; radius <= RADMAX_THICK; radius++) {
			branchStates[radius] = getDefaultState().with(ThickBranchBlock.RADIUSDOUBLE, radius);
		}
	}

	@Nullable
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.getDefaultState().with(RADIUSDOUBLE, 1);
	}

	///////////////////////////////////////////
	// GROWTH
	///////////////////////////////////////////

	public int getRadius(BlockState blockState) {
		return blockState.getBlock() == this ? MathHelper.clamp(blockState.get(RADIUSDOUBLE), 1, getMaxRadius()) : 0;
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
					world.setBlockState(dPos, getTrunkShell().getDefaultState().with(TrunkShellBlock.COREDIR, dir.getOpposite()), flags);
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public int getRadiusForConnection(BlockState blockState, IBlockReader world, BlockPos pos, BranchBlock from, Direction side, int fromRadius) {
		if (from == this) {
			return getRadius(blockState);
		}

		return Math.min(getRadius(blockState), RADMAX_NORMAL);
	}

	@Override
	protected int getSideConnectionRadius(IBlockReader blockAccess, BlockPos pos, int radius, Direction side) {
		BlockPos deltaPos = pos.offset(side);

		try {

			BlockState blockState = blockAccess.getBlockState(deltaPos);
			int connectionRadius = TreeHelper.getTreePart(blockState).getRadiusForConnection(blockState, blockAccess, deltaPos, this, side, radius);

			if (radius > 8) {
				if (side == Direction.DOWN) {
					return connectionRadius >= radius ? 1 : 0;
				} else if (side == Direction.UP) {
					return connectionRadius >= radius ? 2 : connectionRadius > 0 ? 1 : 0;
				}
			}

			return Math.min(RADMAX_NORMAL, connectionRadius);
		} catch (Exception e) { // Temporary measure until we find a way to solve calling an out-of-bounds block here.
			System.out.println("X: " + deltaPos.getX() + " Y: " + deltaPos.getY() + " Z: " + deltaPos.getZ());
			return 0;
		}
	}

	public ReplaceableState getReplaceability(IWorld world, BlockPos pos, BlockPos corePos) {

		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();

		if(block instanceof TrunkShellBlock) {
			//Determine if this shell belongs to the trunk.  Block otherwise.
			Surround surr = state.get(TrunkShellBlock.COREDIR);
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