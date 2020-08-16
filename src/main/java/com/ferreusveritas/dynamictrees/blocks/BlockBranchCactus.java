package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.cells.CellNull;
import com.ferreusveritas.dynamictrees.api.cells.ICell;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.google.common.base.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class BlockBranchCactus extends BlockBranch {

	// The direction it grew from. Can't be up, since cacti can't grow down.
	public static final EnumProperty<Direction> ORIGIN = EnumProperty.<Direction>create("origin", Direction.class, new Predicate<Direction>() {
		@Override
		public boolean apply(@Nullable Direction dir) {
			return dir != Direction.UP;
		}
	});
	 // Not sure it's technically called the 'trunk' on cacti, but whatever
	public static final BooleanProperty TRUNK = BooleanProperty.create("trunk");
	
	public BlockBranchCactus(String name) {
		super(Properties.create(Material.CACTUS)
				.sound(SoundType.CLOTH)
				.harvestTool(ToolType.AXE)
				.harvestLevel(0), name);

		setDefaultState(this.getStateContainer().getBaseState().with(TRUNK, true).with(ORIGIN, Direction.DOWN));
	}

	///////////////////////////////////////////
	// BLOCKSTATES
	///////////////////////////////////////////

	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(ORIGIN, TRUNK);
	}

	@Override
	public BlockState getExtendedState(BlockState state, IBlockReader blockAcess, BlockPos pos) {
		if (state != null) {
			int thisRadius = getRadius(state);

//			for (Direction dir : Direction.values()) {
//				retval = retval.with(CONNECTIONS[dir.getIndex()], getSideConnectionRadius(blockAcess, pos, thisRadius, dir));
//			}
			return (BlockState) state;
		}

		return state;
	}

	///////////////////////////////////////////
	// TREE INFORMATION
	///////////////////////////////////////////

	@Override
	public int branchSupport(BlockState blockState, IBlockReader blockAccess, BlockBranch branch, BlockPos pos, Direction dir, int radius) {
		return 0;// Cacti don't have leaves and don't rot
	}

	///////////////////////////////////////////
	// PHYSICAL PROPERTIES
	///////////////////////////////////////////


	@Override
	public float getBlockHardness(BlockState blockState, IBlockReader worldIn, BlockPos pos) {
		int radius = getRadius(blockState);
		return getFamily().getPrimitiveLog().getBlock().getBlockHardness(blockState, worldIn, pos) * (radius * radius) / 64.0f * 8.0f;
	}

	///////////////////////////////////////////
	// WORLD UPDATE
	///////////////////////////////////////////

	public boolean checkForRot(World world, BlockPos pos, Species species, int radius, Random rand, float chance, boolean rapid) {
		return false;//Do nothing.  Cacti don't rot
	}

	///////////////////////////////////////////
	// INTERACTION
	///////////////////////////////////////////


	@Override
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
		entityIn.attackEntityFrom(DamageSource.CACTUS, 1.0F);
	}

	@Override
	public BlockState getStateForPlacement(BlockState state, Direction facing, BlockState state2, IWorld world, BlockPos pos1, BlockPos pos2, Hand hand) {
		BlockState returnState = this.getDefaultState();

		BlockState adjState = world.getBlockState(pos1.offset(facing.getOpposite()));
		boolean trunk = (facing == Direction.UP && (adjState.isSolid() || (adjState.getBlock() == this && adjState.get(TRUNK))));

		return returnState.with(TRUNK, trunk).with(ORIGIN, facing != Direction.DOWN ? facing.getOpposite() : Direction.DOWN);
	}

	///////////////////////////////////////////
	// RENDERING
	///////////////////////////////////////////


	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT_MIPPED;
	}

	///////////////////////////////////////////
	// GROWTH
	///////////////////////////////////////////

	@Override
	public ICell getHydrationCell(IBlockReader blockAccess, BlockPos pos, BlockState blockState, Direction dir, ILeavesProperties leavesProperties) {
		return CellNull.NULLCELL;
	}

	public int getRadius(BlockState blockState) {
		return blockState.getBlock() == this ? (blockState.get(TRUNK) ? 5 : 4) : 0;
	}

	@Override
	public int setRadius(World world, BlockPos pos, int radius, Direction originDir, int flags) {
		return radius;//Do nothing
	}

	// Directionless probability grabber
	@Override
	public int probabilityForBlock(BlockState blockState, IBlockReader blockAccess, BlockPos pos, BlockBranch from) {
		return isSameTree(from) ? getRadius(blockState) + 2 : 0;
	}

	public GrowSignal growIntoAir(World world, BlockPos pos, GrowSignal signal, int fromRadius) {
		Direction originDir = signal.dir.getOpposite(); // Direction this signal originated from
		boolean trunk = signal.isInTrunk();

		if (originDir.getAxis() != Direction.Axis.Y && (world.getBlockState(pos.up()).getBlock() == this || world.getBlockState(pos.down()).getBlock() == this)) {
			signal.success = false;
			return signal;
		}

		signal.success = world.setBlockState(pos, this.stateContainer.getBaseState().with(TRUNK, trunk).with(ORIGIN, originDir), 2);
		signal.radius = (int) (trunk ? signal.getSpecies().getFamily().getPrimaryThickness(): signal.getSpecies().getFamily().getSecondaryThickness());
		return signal;
	}

	@Override
	public GrowSignal growSignal(World world, BlockPos pos, GrowSignal signal) {

		if (signal.step()) { // This is always placed at the beginning of every growSignal function
			Species species = signal.getSpecies();

			//Direction originDir = signal.dir.getOpposite(); // Direction this signal originated from
			Direction targetDir = species.selectNewDirection(world, pos, this, signal); // This must be cached on the stack for proper recursion
			signal.doTurn(targetDir);

			BlockPos deltaPos = pos.offset(targetDir);
			BlockState deltaState = world.getBlockState(deltaPos);

			// Pass grow signal to next block in path
			ITreePart treepart = TreeHelper.getTreePart(deltaState);

			if (treepart == this) {
				signal = treepart.growSignal(world, deltaPos, signal); // Recurse
			} else if (world.isAirBlock(deltaPos)) {
				signal = growIntoAir(world, deltaPos, signal, (int) signal.radius);
			}
		}

		return signal;
	}

	@Override
	public BlockState getStateForRadius(int radius) {
		return getDefaultState().with(TRUNK, radius > 4);
	}

	///////////////////////////////////////////
	// PHYSICAL BOUNDS
	///////////////////////////////////////////


	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		if (state.getBlock() != this) {
			return VoxelShapes.empty();
		}

		int thisRadius = getRadius(state);

		boolean connectionMade = false;
		double radius = thisRadius / 16.0;
		double gap = 0.5 - radius;
		AxisAlignedBB aabb = new AxisAlignedBB(0, 0, 0, 0, 0, 0).grow(radius);
		int numConnections = 0;
		for (Direction dir : Direction.values()) {
			if (getSideConnectionRadius(worldIn, pos, thisRadius, dir) > 0) {
				connectionMade = true;
				numConnections ++;
				aabb = aabb.expand(dir.getXOffset() * gap, dir.getYOffset() * gap, dir.getZOffset() * gap);
			}
		}
		if (!state.get(TRUNK) && numConnections == 1 && state.get(ORIGIN).getAxis().isHorizontal()) {
			aabb = aabb.expand(Direction.UP.getXOffset() * gap, Direction.UP.getYOffset() * gap, Direction.UP.getZOffset() * gap);
		}
		if (connectionMade) {
			return VoxelShapes.create(aabb.offset(0.5, 0.5, 0.5));
		}
		return VoxelShapes.create(new AxisAlignedBB(0.5 - radius, 0.5 - radius, 0.5 - radius, 0.5 + radius, 0.5 + radius, 0.5 + radius));
	}

//	@Override
//	public void addCollisionBoxToList(BlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean p_185477_7_) {
//		int thisRadius = getRadius(state);
//
//		int numConnections = 0;
//		for (Direction dir : Direction.VALUES) {
//			int connRadius = getSideConnectionRadius(world, pos, thisRadius, dir);
//			if (connRadius > 0) {
//				numConnections++;
//				double radius = MathHelper.clamp(connRadius, 1, thisRadius) / 16.0;
//				double gap = 0.5 - radius;
//				AxisAlignedBB aabb = new AxisAlignedBB(0, 0, 0, 0, 0, 0).grow(radius);
//				aabb = aabb.offset(dir.getFrontOffsetX() * gap, dir.getFrontOffsetY() * gap, dir.getFrontOffsetZ() * gap).offset(0.5, 0.5, 0.5);
//				addCollisionBoxToList(pos, entityBox, collidingBoxes, aabb);
//			}
//		}
//		if (!state.getValue(TRUNK) && numConnections == 1 && state.getValue(ORIGIN).getAxis().isHorizontal()) {
//			double radius = MathHelper.clamp(4, 1, thisRadius) / 16.0;
//			double gap = 0.5 - radius;
//			AxisAlignedBB aabb = new AxisAlignedBB(0, 0, 0, 0, 0, 0).grow(radius);
//			aabb = aabb.offset(Direction.UP.getFrontOffsetX() * gap, Direction.UP.getFrontOffsetY() * gap, Direction.UP.getFrontOffsetZ() * gap).offset(0.5, 0.5, 0.5);
//			addCollisionBoxToList(pos, entityBox, collidingBoxes, aabb);
//		}
//
//		double min = 0.5 - (thisRadius / 16.0), max = 0.5 + (thisRadius / 16.0);
//		addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(min, min, min, max, max, max));
//	}

	@Override
	public int getRadiusForConnection(BlockState blockState, IBlockReader blockAccess, BlockPos pos, BlockBranch from, Direction side, int fromRadius) {
		return 0;
	}

	protected int getSideConnectionRadius(IBlockReader blockAccess, BlockPos pos, int radius, Direction side) {
		BlockPos deltaPos = pos.offset(side);
		BlockState otherState = blockAccess.getBlockState(deltaPos);
		BlockState state = blockAccess.getBlockState(pos);

		if (otherState.getBlock() == this && state.getBlock() == this && (otherState.get(ORIGIN) == side.getOpposite() || state.get(ORIGIN) == side)) {
			return (state.get(TRUNK) && otherState.get(TRUNK)) ? 5 : 4;
		} else if (side == Direction.DOWN && state.getBlock() == this && state.get(TRUNK) && state.get(ORIGIN) == side) {
			return 5;
		}

		return 0;
	}

	///////////////////////////////////////////
	// NODE ANALYSIS
	///////////////////////////////////////////

	@Override
	public MapSignal analyse(BlockState blockState, World world, BlockPos pos, Direction fromDir, MapSignal signal) {
		// Note: fromDir will be null in the origin node
		if (signal.depth++ < 32) {// Prevents going too deep into large networks, or worse, being caught in a network loop
			BlockState state = world.getBlockState(pos);
			signal.run(blockState, world, pos, fromDir);// Run the inspectors of choice
			for (Direction dir : Direction.values()) {// Spread signal in various directions
				if (dir != fromDir) {// don't count where the signal originated from
					BlockPos deltaPos = pos.offset(dir);
					BlockState deltaState = world.getBlockState(deltaPos);

					if (deltaState.getBlock() == this && deltaState.get(ORIGIN) == dir.getOpposite()) {
						signal = ((ITreePart) deltaState.getBlock()).analyse(deltaState, world, deltaPos, dir.getOpposite(), signal);
					} else if (state.getBlock() == this && state.get(ORIGIN) == dir) {
						signal = TreeHelper.getTreePart(deltaState).analyse(deltaState, world, deltaPos, dir.getOpposite(), signal);
					}

					// This should only be true for the originating block when the root node is found
					if (signal.found && signal.localRootDir == null && fromDir == null) {
						signal.localRootDir = dir;
					}
				}
			}
			signal.returnRun(blockState, world, pos, fromDir);
		} else {
			BlockState state = world.getBlockState(pos);
			if(state.getBlock() instanceof BlockBranch) {
				BlockBranch branch = (BlockBranch) state.getBlock();
				branch.breakDeliberate(world, pos, DynamicTrees.EnumDestroyMode.OVERFLOW);// Destroy one of the offending nodes
			}
			signal.overflow = true;
		}
		signal.depth--;
		return signal;
	}

}
