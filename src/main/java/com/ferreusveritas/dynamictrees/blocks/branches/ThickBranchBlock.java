package com.ferreusveritas.dynamictrees.blocks.branches;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.blocks.IMusable;
import com.ferreusveritas.dynamictrees.blocks.SurfaceRootBlock;
import com.ferreusveritas.dynamictrees.blocks.TrunkShellBlock;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;
import com.ferreusveritas.dynamictrees.util.CoordUtils.Surround;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BushBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
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
import net.minecraft.world.World;

import javax.annotation.Nonnull;

//TODO: 1.14.4 This gets flattened into the normal block branch with radius 1 - 32 and eliminated 

public class ThickBranchBlock extends BasicBranchBlock implements IMusable {

//	protected static final AxisAlignedBB maxBranchBB = new AxisAlignedBB(-1, 0, -1, 2, 1, 2);
	public static final int RADMAX_THICK = 32;

	protected static final IntegerProperty RADIUSNYBBLE = IntegerProperty.create("radius", 0, 15); //39 ?
	protected final boolean extended;
	public ThickBranchBlock otherBlock;

	public ThickBranchBlock(String name) {
		this(Properties.create(Material.WOOD),name);
	}

	public ThickBranchBlock(Properties properties, String name) {
		this(properties, name, false);
		otherBlock = new ThickBranchBlock(properties, name + "_thick", true);
		otherBlock.otherBlock = this;

		cacheBranchThickStates();
	}

	protected ThickBranchBlock(Properties properties, String name, boolean extended) {
		super(properties, name);
		this.setDefaultState(this.getDefaultState().with(RADIUSNYBBLE, 1));
		this.extended = extended;
	}

	public ThickBranchBlock getPairSide(boolean ext) {
		return extended ^ ext ? otherBlock : this;
	}

	public TrunkShellBlock getTrunkShell (){
		return DTRegistries.trunkShellBlock;
	}

	//We can't override this function since the "otherBlock" will not have been created yet.
	@Override
	public void cacheBranchStates() { }

	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(RADIUSNYBBLE);
	}

	public void cacheBranchThickStates() {
		setDefaultState(this.getStateContainer().getBaseState().with(RADIUSNYBBLE, 0));

		if(!extended) {
			branchStates = new BlockState[RADMAX_THICK + 1];
			otherBlock.branchStates = branchStates;

			//Cache the branch blocks states for rapid lookup
			branchStates[0] = Blocks.AIR.getDefaultState();

			for(int radius = 1; radius <= RADMAX_THICK; radius++) {
				branchStates[radius] = getPairSide(radius > 16).getDefaultState().with(ThickBranchBlock.RADIUSNYBBLE, (radius - 1) & 0x0f);
			}
		}
	}
//
//	public IProperty<?>[] getIgnorableProperties() {
//		return new IProperty<?>[]{ RADIUSNYBBLE };
//	}


	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
		if(extended) {
			return this.otherBlock.getPickBlock(otherBlock.getDefaultState(), target, world, pos, player);
		}

		return super.getPickBlock(state, target, world, pos, player);
	}

	@Override
	public void setFamily(TreeFamily tree) {
		super.setFamily(tree);
		if (!extended) otherBlock.setFamily(tree);
	}

	///////////////////////////////////////////
	// BLOCKSTATES
	///////////////////////////////////////////

//	//////////////////////////////////////////////////
//	//The following is highly experimental code
//	//////////////////////////////////////////////////
//	class StateImplementationCachedRadius extends StateImplementation {
//		protected StateImplementationCachedRadius(Block blockIn, ImmutableMap < IProperty<?>, Comparable<? >> propertiesIn) { super(blockIn, propertiesIn); }
//		protected StateImplementationCachedRadius(Block blockIn, ImmutableMap<IProperty<?>, Comparable<?>> propertiesIn, ImmutableTable<IProperty<?>, Comparable<?>, BlockState> propertyValueTable) { super(blockIn, propertiesIn, propertyValueTable); }
//
//		private int radius = -1;
//		int getRadius() {
//			return radius;
//		}
//	}
//
//	class BlockStateContainerCustom extends BlockStateContainer {
//		public BlockStateContainerCustom(Block blockIn, IProperty<?>... properties) { this(blockIn, properties, null); }
//		protected BlockStateContainerCustom(Block blockIn, IProperty<?>[] properties, ImmutableMap<IUnlistedProperty<?>, Optional<?>> unlistedProperties) { super(blockIn, properties, unlistedProperties); }
//
//		@Override
//		protected StateImplementation createState(Block block, ImmutableMap<IProperty<?>, Comparable<?>> properties, ImmutableMap<IUnlistedProperty<?>, Optional<?>> unlistedProperties) {
//			return new StateImplementationCachedRadius(block, properties);
//		}
//	}
//	//////////////////////////////////////////////////
//	//end highly experimental code
//	//////////////////////////////////////////////////


	///////////////////////////////////////////
	// GROWTH
	///////////////////////////////////////////

	public int getRadius(BlockState blockState) {
		return blockState.getBlock() == this ? MathHelper.clamp(blockState.get(RADIUSNYBBLE) + (((ThickBranchBlock)blockState.getBlock()).extended ? 17 : 1), 1, getMaxRadius()) : 0;
	}

	@Override
	public int setRadius(World world, BlockPos pos, int radius, Direction originDir, int flags) {

		//If the radius is <= 8 then we can just set the block as normal and move on
		if(radius <= RADMAX_NORMAL) {
			return super.setRadius(world, pos, radius, originDir, flags);
		}

		ReplaceableState repStates[] = new ReplaceableState[8];

		boolean setable = true;

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
			int setRadius = super.setRadius(world, pos, radius, originDir, flags);

			for(Surround dir : Surround.values()) {
				BlockPos dPos = pos.add(dir.getOffset());
				ReplaceableState rep = repStates[dir.ordinal()];
				if(rep == ReplaceableState.REPLACEABLE) {
					world.setBlockState(dPos, getTrunkShell().getDefaultState().with(TrunkShellBlock.COREDIR, dir.getOpposite()), flags);
				}
			}
			return setRadius;
		} else {
			return super.setRadius(world, pos, RADMAX_NORMAL, originDir, flags);
		}

	}

	@Override
	public int getRadiusForConnection(BlockState blockState, IBlockReader world, BlockPos pos, BranchBlock from, Direction side, int fromRadius) {
		if (from == this || from == this.otherBlock) {
			return getRadius(blockState);
		}

		return Math.min(getRadius(blockState), RADMAX_NORMAL);
	}

	@Override
	protected int getSideConnectionRadius(IBlockReader blockAccess, BlockPos pos, int radius, Direction side) {
		BlockPos deltaPos = pos.offset(side);
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
	}

	public ReplaceableState getReplaceability(World world, BlockPos pos, BlockPos corePos) {

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