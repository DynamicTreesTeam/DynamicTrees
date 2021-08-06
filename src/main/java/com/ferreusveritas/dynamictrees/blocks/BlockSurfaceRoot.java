package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.ModTabs;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;
import com.ferreusveritas.dynamictrees.util.CoordUtils.Surround;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.property.Properties;

import java.util.List;

public class BlockSurfaceRoot extends Block {

	public static final int RADMAX_NORMAL = 8;

	protected static final PropertyInteger RADIUS = PropertyInteger.create("radius", 1, RADMAX_NORMAL);

	public static final IUnlistedProperty GROUNDED = new Properties.PropertyAdapter<Boolean>(PropertyBool.create("grounded"));

	public static final IUnlistedProperty[] CONNECTIONS = {
		new Properties.PropertyAdapter<Integer>(PropertyInteger.create("radiuss", 0, 8)),
		new Properties.PropertyAdapter<Integer>(PropertyInteger.create("radiusw", 0, 8)),
		new Properties.PropertyAdapter<Integer>(PropertyInteger.create("radiusn", 0, 8)),
		new Properties.PropertyAdapter<Integer>(PropertyInteger.create("radiuse", 0, 8))
	};

	public static final IUnlistedProperty[] LEVELS = {
		new Properties.PropertyAdapter<ConnectionLevel>(PropertyEnum.create("levels", ConnectionLevel.class)),
		new Properties.PropertyAdapter<ConnectionLevel>(PropertyEnum.create("levelw", ConnectionLevel.class)),
		new Properties.PropertyAdapter<ConnectionLevel>(PropertyEnum.create("leveln", ConnectionLevel.class)),
		new Properties.PropertyAdapter<ConnectionLevel>(PropertyEnum.create("levele", ConnectionLevel.class))
	};

	public BlockSurfaceRoot(Material material, String name) {
		super(material);
		setUnlocalizedName(name);
		setRegistryName(name);
		setDefaultState(this.blockState.getBaseState().withProperty(RADIUS, 1));
		setHarvestLevel("axe", 0);
		setSoundType(SoundType.WOOD);
		setHardness(2.5f);
		setResistance(1.0f);
		setCreativeTab(ModTabs.dynamicTreesTab);
	}

	public enum ConnectionLevel implements IStringSerializable {

		MID(0),
		LOW(-1),
		HIGH(1);

		private final int yOffset;

		ConnectionLevel(int y) {
			this.yOffset = y;
		}

		@Override
		public String getName() {
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
	protected BlockStateContainer createBlockState() {
		IProperty[] listedProperties = {RADIUS};

		IUnlistedProperty[] unlistedProperties = new IUnlistedProperty[]{
			CONNECTIONS[0], CONNECTIONS[1], CONNECTIONS[2], CONNECTIONS[3],
			LEVELS[0], LEVELS[1], LEVELS[2], LEVELS[3], GROUNDED
		};

		return new ExtendedBlockState(this, listedProperties, unlistedProperties);
	}

	public IProperty<?>[] getIgnorableProperties() {
		return new IProperty<?>[]{RADIUS};
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(RADIUS, (meta & 7) + 1);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(RADIUS) - 1;
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		if (state instanceof IExtendedBlockState) {
			IExtendedBlockState retval = (IExtendedBlockState) state;

			int thisRadius = getRadius(state);

			retval = retval.withProperty(GROUNDED, world.isSideSolid(pos.down(), EnumFacing.UP, false));

			for (EnumFacing dir : EnumFacing.HORIZONTALS) {
				RootConnection conn = getSideConnectionRadius(world, pos, thisRadius, dir);
				if (conn != null) {
					int horIndex = dir.getHorizontalIndex();
					retval = retval.withProperty(CONNECTIONS[horIndex], conn.radius).withProperty(LEVELS[horIndex], conn.level);
				}
			}

			return retval;
		}

		return state;
	}

	public int getRadius(IBlockState blockState) {
		return blockState.getBlock() == this ? blockState.getValue(RADIUS) : 0;
	}

	public int setRadius(World world, BlockPos pos, int radius, EnumFacing originDir, int flags) {
		world.setBlockState(pos, getStateForRadius(radius), flags);
		return radius;
	}

	public IBlockState getStateForRadius(int radius) {
		return getDefaultState().withProperty(RADIUS, MathHelper.clamp(radius, 0, getMaxRadius()));
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

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		return true;
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.UNDEFINED;//This prevents fences and walls from attempting to connect to branches.
	}


	///////////////////////////////////////////
	// PHYSICAL BOUNDS
	///////////////////////////////////////////

	// This is only so effective because the center of the player must be inside the block that contains the tree trunk.
	// The result is that only thin branches and trunks can be climbed
	@Override
	public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity) {
		return false;
	}

	public static final Surround[] sidesFirst = new Surround[]{Surround.N, Surround.S, Surround.W, Surround.E, Surround.NW, Surround.NE, Surround.SW, Surround.SE};

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess blockAccess, BlockPos pos) {

		if (state.getBlock() != this) {
			return NULL_AABB;
		}

		AxisAlignedBB trunkBB = null;

		for (Surround dir : sidesFirst) {
			BlockPos dPos = pos.add(dir.getOffset());
			IBlockState testState = blockAccess.getBlockState(dPos);
			if (testState.getBlock() instanceof BlockBranchThick) {
				BlockBranchThick trunk = (BlockBranchThick) testState.getBlock();
				trunkBB = trunk.getBoundingBox(testState, blockAccess, dPos).offset(dir.getOffsetPos()).intersect(FULL_BLOCK_AABB);
				break;//There should only be one trunk in proximity
			}
		}

		int thisRadius = getRadius(state);
		int radialHeight = getRadialHeight(thisRadius);

		double radius = thisRadius / 16.0;
		double gap = 0.5 - radius;
		AxisAlignedBB aabb = new AxisAlignedBB(-radius, 0, -radius, radius, radialHeight / 16d, radius);
		for (EnumFacing dir : EnumFacing.VALUES) {
			RootConnection conn = getSideConnectionRadius(blockAccess, pos, thisRadius, dir);
			if (conn != null) {
				aabb = aabb.expand(dir.getFrontOffsetX() * gap, dir.getFrontOffsetY() * gap, dir.getFrontOffsetZ() * gap);
				if (conn.level == ConnectionLevel.HIGH) {
					aabb = aabb.setMaxY(1.0 + (radialHeight / 16d));
				}
			}
		}

		aabb = aabb.offset(0.5, 0.0, 0.5);
		return trunkBB != null ? trunkBB.union(aabb) : aabb;
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean p_185477_7_) {
		if (entityIn instanceof EntityFallingTree) {
			return;
		}

		boolean connectionMade = false;
		int thisRadius = getRadius(state);

		for (EnumFacing dir : EnumFacing.HORIZONTALS) {
			RootConnection conn = getSideConnectionRadius(world, pos, thisRadius, dir);
			if (conn != null) {
				connectionMade = true;
				int r = MathHelper.clamp(conn.radius, 1, thisRadius);
				double radius = r / 16.0;
				double radialHeight = getRadialHeight(r) / 16.0;
				double gap = 0.5 - radius;
				AxisAlignedBB aabb = new AxisAlignedBB(-radius, 0, -radius, radius, radialHeight, radius);
				aabb = aabb.expand(dir.getFrontOffsetX() * gap, 0, dir.getFrontOffsetZ() * gap).offset(0.5, 0.0, 0.5);
				addCollisionBoxToList(pos, entityBox, collidingBoxes, aabb);
			}
		}

		if (!connectionMade) {
			double radius = thisRadius / 16.0;
			double radialHeight = getRadialHeight(thisRadius) / 16.0;
			AxisAlignedBB aabb = new AxisAlignedBB(0.5 - radius, 0, 0.5 - radius, 0.5 + radius, radialHeight, 0.5 + radius);
			addCollisionBoxToList(pos, entityBox, collidingBoxes, aabb);
		}

	}

	protected RootConnection getSideConnectionRadius(IBlockAccess blockAccess, BlockPos pos, int radius, EnumFacing side) {

		if (side.getAxis().isHorizontal()) {
			BlockPos dPos = pos.offset(side);
			IBlockState blockState = blockAccess.getBlockState(dPos);
			IBlockState upState = blockAccess.getBlockState(pos.up());
			ConnectionLevel level = (upState.getBlock() == Blocks.AIR && blockState.isNormalCube()) ? ConnectionLevel.HIGH : (blockState.getBlock() == Blocks.AIR ? ConnectionLevel.LOW : ConnectionLevel.MID);

			if (level != ConnectionLevel.MID) {
				dPos = dPos.up(level.yOffset);
				blockState = blockAccess.getBlockState(dPos);
			}

			if (blockState.getBlock() instanceof BlockSurfaceRoot) {
				return new RootConnection(level, ((BlockSurfaceRoot) blockState.getBlock()).getRadius(blockState));
			} else if (level == ConnectionLevel.MID && TreeHelper.isBranch(blockState) && TreeHelper.getTreePart(blockState).getRadius(blockState) >= 8) {
				return new RootConnection(ConnectionLevel.MID, 8);
			}

		}

		return null;
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		IBlockState upstate = worldIn.getBlockState(pos.up());

		if (upstate.getBlock() == ModBlocks.blockTrunkShell) {
			worldIn.setBlockState(pos, upstate);
		}

		for (EnumFacing dir : EnumFacing.HORIZONTALS) {
			BlockPos dPos = pos.offset(dir).down();
			worldIn.getBlockState(dPos).neighborChanged(worldIn, dPos, this, pos);
		}
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos neighbor) {
		if (!canBlockStay(world, pos, state)) {
			world.setBlockToAir(pos);
		}
	}

	protected boolean canBlockStay(World world, BlockPos pos, IBlockState state) {

		BlockPos below = pos.down();
		IBlockState belowState = world.getBlockState(below);

		int thisRadius = getRadius(state);

		if (belowState.isNormalCube()) {//If a branch is sitting on a solid block
			for (EnumFacing dir : EnumFacing.HORIZONTALS) {
				RootConnection conn = getSideConnectionRadius(world, pos, thisRadius, dir);
				if (conn != null && conn.radius > thisRadius) {
					return true;
				}
			}
		} else {//If the branch has no solid block under it
			boolean connections = false;
			for (EnumFacing dir : EnumFacing.HORIZONTALS) {
				RootConnection conn = getSideConnectionRadius(world, pos, thisRadius, dir);
				if (conn != null) {
					if (conn.level == ConnectionLevel.MID) {
						return false;
					}
					if (conn.radius > thisRadius) {
						connections = true;
					}
				}
			}
			return connections;
		}

		return false;
	}

}
