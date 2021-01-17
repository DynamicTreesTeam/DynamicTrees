package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.CoordUtils.Surround;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

public class SurfaceRootBlock extends Block {
	
	public static final int RADMAX_NORMAL = 8;
	
	protected static final IntegerProperty RADIUS = IntegerProperty.create("radius", 1, RADMAX_NORMAL);

	public static final BooleanProperty GROUNDED = BooleanProperty.create("grounded");

//	public static final IUnlistedProperty CONNECTIONS[] = {
//		new net.minecraftforge.common.property.Properties.PropertyAdapter<Integer>(IntegerProperty.create("radiuss", 0, 8)),
//		new net.minecraftforge.common.property.Properties.PropertyAdapter<Integer>(IntegerProperty.create("radiusw", 0, 8)),
//		new net.minecraftforge.common.property.Properties.PropertyAdapter<Integer>(IntegerProperty.create("radiusn", 0, 8)),
//		new net.minecraftforge.common.property.Properties.PropertyAdapter<Integer>(IntegerProperty.create("radiuse", 0, 8))
//	};
//
//	public static final IUnlistedProperty LEVELS[] = {
//		new net.minecraftforge.common.property.Properties.PropertyAdapter<ConnectionLevel>(PropertyEnum.create("levels", ConnectionLevel.class)),
//		new net.minecraftforge.common.property.Properties.PropertyAdapter<ConnectionLevel>(PropertyEnum.create("levelw", ConnectionLevel.class)),
//		new net.minecraftforge.common.property.Properties.PropertyAdapter<ConnectionLevel>(PropertyEnum.create("leveln", ConnectionLevel.class)),
//		new net.minecraftforge.common.property.Properties.PropertyAdapter<ConnectionLevel>(PropertyEnum.create("levele", ConnectionLevel.class))
//	};

	public SurfaceRootBlock(Material material, String name) {
		super(Block.Properties.create(material)
				.harvestTool(ToolType.AXE)
				.harvestLevel(0)
				.hardnessAndResistance(2.5f, 1.0F)
				.sound(SoundType.WOOD));

		setRegistryName(name);
	}
	
	public enum ConnectionLevel implements IStringSerializable {

		MID(0),
		LOW(-1),
		HIGH(1);

		private final int yOffset;

		private ConnectionLevel(int y) {
			this.yOffset = y;
		}

		@Override
		public String getString() {
			return toString().toLowerCase();
		}

		public int getYOffset() {
			return yOffset;
		}
	}

	public class RootConnection {
		public ConnectionLevel level;
		public int radius;

		public RootConnection(ConnectionLevel level, int radius) {
			this.level = level;
			this.radius = radius;
		}
	}

	///////////////////////////////////////////
	// BLOCKSTATES
	///////////////////////////////////////////


	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(RADIUS, GROUNDED);
	}

//	@Override
//	protected BlockStateContainer createBlockState() {
//		IProperty[] listedProperties = { RADIUS };
//
//		IUnlistedProperty unlistedProperties[] = new IUnlistedProperty[] {
//			CONNECTIONS[0], CONNECTIONS[1], CONNECTIONS[2], CONNECTIONS[3],
//			LEVELS[0], LEVELS[1], LEVELS[2], LEVELS[3], GROUNDED
//		};
//
//		return new ExtendedBlockState(this, listedProperties, unlistedProperties);
//	}

//	public IProperty<?>[] getIgnorableProperties() {
//		return new IProperty<?>[]{ RADIUS };
//	}

//	@Override
//	public BlockState getExtendedState(BlockState state, IBlockReader world, BlockPos pos) {
//		if (state instanceof BlockState) {
//			BlockState retval = (BlockState) state;
//
//			int thisRadius = getRadius(state);
//
//			retval = retval.with(GROUNDED, world.so(pos.down(), Direction.UP, false));
//
//			for(Direction dir: CoordUtils.HORIZONTALS) {
//				RootConnection conn = getSideConnectionRadius(world, pos, thisRadius, dir);
//				if(conn != null) {
//					int horIndex = dir.getHorizontalIndex();
//					retval = retval.with(CONNECTIONS[horIndex], conn.radius).withProperty(LEVELS[horIndex], conn.level);
//				}
//			}
//
//			return retval;
//		}
//
//		return state;
//	}

	public int getRadius(BlockState blockState) {
		return blockState.getBlock() == this ? blockState.get(RADIUS) : 0;
	}

	public int setRadius(World world, BlockPos pos, int radius, Direction originDir, int flags) {
		world.setBlockState(pos, getStateForRadius(radius), flags);
		return radius;
	}

	public BlockState getStateForRadius(int radius) {
		return getDefaultState().with(RADIUS, MathHelper.clamp(radius, 0, getMaxRadius()));
	}

	public int getMaxRadius() {
		return RADMAX_NORMAL;
	}

	public int getRadialHeight(int radius) {
		return radius * 2;
	}

	///////////////////////////////////////////
	// RENDERING
	///////////////////////////////////////////

//	@Override
//	public boolean isOpaqueCube(BlockState state) {
//		return false;
//	}
//
//	@Override
//	public boolean isFullCube(BlockState state) {
//		return false;
//	}
//
//	@Override
//	public boolean shouldSideBeRendered(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
//		return true;
//	}
//
//	@Override
//	public BlockFaceShape getBlockFaceShape(IBlockReader worldIn, BlockState state, BlockPos pos, Direction face) {
//		return BlockFaceShape.UNDEFINED;//This prevents fences and walls from attempting to connect to branches.
//	}


	///////////////////////////////////////////
	// PHYSICAL BOUNDS
	///////////////////////////////////////////

	// This is only so effective because the center of the player must be inside the block that contains the tree trunk.
	// The result is that only thin branches and trunks can be climbed


	@Override
	public boolean isLadder(BlockState state, IWorldReader world, BlockPos pos, LivingEntity entity) {
		return false;
	}

	public static final Surround sidesFirst[] = new Surround[] { Surround.N, Surround.S, Surround.W, Surround.E, Surround.NW, Surround.NE, Surround.SW, Surround.SE };

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader blockReader, BlockPos pos, ISelectionContext context) {
		if (state.getBlock() != this) {
			return VoxelShapes.empty();
		}

		AxisAlignedBB trunkBB = null;

		for(Surround dir: sidesFirst ) {
			BlockPos dPos = pos.add(dir.getOffset());
			BlockState testState  = blockReader.getBlockState(dPos);
			if(testState.getBlock() instanceof ThickBranchBlock) {
				ThickBranchBlock trunk = (ThickBranchBlock) testState.getBlock();
				trunkBB = trunk.getShape(testState, blockReader, dPos, context).getBoundingBox().offset(dir.getOffsetPos()).intersect(VoxelShapes.fullCube().getBoundingBox());
				break;//There should only be one trunk in proximity
			}
		}

		int thisRadius = getRadius(state);
		int radialHeight = getRadialHeight(thisRadius);

		double radius = thisRadius / 16.0;
		double gap = 0.5 - radius;
		AxisAlignedBB aabb = new AxisAlignedBB(-radius, 0, -radius, radius, radialHeight / 16d, radius);
		for (Direction dir : Direction.values()) {
			RootConnection conn = getSideConnectionRadius(blockReader, pos, thisRadius, dir);
			if (conn != null) {
				aabb = aabb.expand(dir.getXOffset() * gap, dir.getYOffset() * gap, dir.getZOffset() * gap);
				if(conn.level == ConnectionLevel.HIGH) {
//					aabb = aabb.setMaxY(1.0 + (radialHeight / 16d));
				}
			}
		}

		aabb = aabb.offset(0.5, 0.0, 0.5);
		return trunkBB != null ? VoxelShapes.create(trunkBB.union(aabb)) : VoxelShapes.create(aabb);
	}

//	@Override
//	public void addCollisionBoxToList(BlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean p_185477_7_) {
//		if(entityIn instanceof EntityFallingTree) {
//			return;
//		}
//
//		boolean connectionMade = false;
//		int thisRadius = getRadius(state);
//
//		for (Direction dir : CoordUtils.HORIZONTALS) {
//			RootConnection conn = getSideConnectionRadius(world, pos, thisRadius, dir);
//			if (conn != null) {
//				connectionMade = true;
//				int r = MathHelper.clamp(conn.radius, 1, thisRadius);
//				double radius = r / 16.0;
//				double radialHeight = getRadialHeight(r) / 16.0;
//				double gap = 0.5 - radius;
//				AxisAlignedBB aabb = new AxisAlignedBB(-radius, 0, -radius, radius, radialHeight, radius);
//				aabb = aabb.expand(dir.getXOffset() * gap, 0, dir.getZOffset() * gap).offset(0.5, 0.0, 0.5);
//				addCollisionBoxToList(pos, entityBox, collidingBoxes, aabb);
//			}
//		}
//
//		if(!connectionMade) {
//			double radius = thisRadius / 16.0;
//			double radialHeight = getRadialHeight(thisRadius) / 16.0;
//			AxisAlignedBB aabb = new AxisAlignedBB(0.5 - radius, 0, 0.5 - radius, 0.5 + radius, radialHeight, 0.5 + radius);
//			addCollisionBoxToList(pos, entityBox, collidingBoxes, aabb);
//		}
//
//	}

	protected RootConnection getSideConnectionRadius(IBlockReader blockReader, BlockPos pos, int radius, Direction side) {
		if(side.getAxis().isHorizontal()) {
			BlockPos dPos = pos.offset(side);
			BlockState blockState = blockReader.getBlockState(dPos);
			BlockState upState = blockReader.getBlockState(pos.up());
			ConnectionLevel level = (upState.getBlock() == Blocks.AIR && blockState.isNormalCube(blockReader, dPos)) ? ConnectionLevel.HIGH : (blockState.getBlock() == Blocks.AIR ? ConnectionLevel.LOW : ConnectionLevel.MID);

			if(level != ConnectionLevel.MID) {
				dPos = dPos.up(level.yOffset);
				blockState = blockReader.getBlockState(dPos);
			}

			if(blockState.getBlock() instanceof SurfaceRootBlock) {
				return new RootConnection(level, ((SurfaceRootBlock)blockState.getBlock()).getRadius(blockState));
			} else
			if(level == ConnectionLevel.MID && TreeHelper.isBranch(blockState) && TreeHelper.getTreePart(blockState).getRadius(blockState) >= 8) {
				return new RootConnection(ConnectionLevel.MID, 8);
			}

		}
		return null;
	}

	@Override
	public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, FluidState fluid) {
		BlockState upstate = world.getBlockState(pos.up());

		if(upstate.getBlock() == DTRegistries.trunkShellBlock) {
			world.setBlockState(pos, upstate);
		}

		for(Direction dir : CoordUtils.HORIZONTALS) {
			BlockPos dPos = pos.offset(dir).down();
//			worldIn.getBlockState(dPos).neighborChanged(state, worldIn, pos, state.getBlock(), );
		}
		return true;
	}

	@Override
	public void neighborChanged(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		if(!canBlockStay(world, pos, state)) {
			world.removeBlock(pos, false);
		}
	}

	protected boolean canBlockStay(World world, BlockPos pos, BlockState state) {

		BlockPos below = pos.down();
		BlockState belowState = world.getBlockState(below);

		int thisRadius = getRadius(state);

		if(belowState.isNormalCube(world,below)) {//If a branch is sitting on a solid block
			for(Direction dir : CoordUtils.HORIZONTALS) {
				RootConnection conn = getSideConnectionRadius(world, pos, thisRadius, dir);
				if(conn != null && conn.radius > thisRadius) {
					return true;
				}
			}
		} else {//If the branch has no solid block under it
			boolean connections = false;
			for(Direction dir : CoordUtils.HORIZONTALS) {
				RootConnection conn = getSideConnectionRadius(world, pos, thisRadius, dir);
				if(conn != null) {
					if(conn.level == ConnectionLevel.MID) {
						return false;
					}
					if(conn.radius > thisRadius) {
						connections = true;
					}
				}
			}
			return connections;
		}

		return false;
	}

}
