package com.ferreusveritas.dynamictrees.blocks;

import java.util.List;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
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

public class BlockSurfaceRoot extends Block {
	
	public static final int RADMAX_NORMAL = 8;
	
	protected static final PropertyInteger RADIUS = PropertyInteger.create("radius", 1, RADMAX_NORMAL);
	
	public static final IUnlistedProperty CONNECTIONS[] = {
		new Properties.PropertyAdapter<Integer>(PropertyInteger.create("radiuss", 0, 8)),
		new Properties.PropertyAdapter<Integer>(PropertyInteger.create("radiusw", 0, 8)),
		new Properties.PropertyAdapter<Integer>(PropertyInteger.create("radiusn", 0, 8)),
		new Properties.PropertyAdapter<Integer>(PropertyInteger.create("radiuse", 0, 8))
	};
	
	public static final IUnlistedProperty LEVELS[] = {
		new Properties.PropertyAdapter<ConnectionLevel>(PropertyEnum.create("levels", ConnectionLevel.class)),
		new Properties.PropertyAdapter<ConnectionLevel>(PropertyEnum.create("levelw", ConnectionLevel.class)),
		new Properties.PropertyAdapter<ConnectionLevel>(PropertyEnum.create("leveln", ConnectionLevel.class)),
		new Properties.PropertyAdapter<ConnectionLevel>(PropertyEnum.create("levele", ConnectionLevel.class))
	};
	
	public BlockSurfaceRoot(Material material, String name) {
		super(material);
		setUnlocalizedName(name);
		setRegistryName(name);
		setHarvestLevel("axe", 0);
		setCreativeTab(DynamicTrees.dynamicTreesTab);
	}
	
	enum ConnectionLevel implements IStringSerializable {
		
		MID(0),
		LOW(-1),
		HIGH(1);

		private final int yOffset;
		
		private ConnectionLevel(int y) {
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
	
	class RootConnection {
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
		IProperty[] listedProperties = { RADIUS };
		
		IUnlistedProperty unlistedProperties[] = new IUnlistedProperty[] {
			CONNECTIONS[0], CONNECTIONS[1], CONNECTIONS[2], CONNECTIONS[3],
			LEVELS[0], LEVELS[1], LEVELS[2], LEVELS[3]
		};
		
		return new ExtendedBlockState(this, listedProperties, unlistedProperties);
	}
	
	public IProperty<?>[] getIgnorableProperties() {
		return new IProperty<?>[]{ RADIUS };
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
			
			for (EnumFacing dir : EnumFacing.HORIZONTALS) {
				BlockPos dPos = pos.offset(dir);
				int radius = 0;
				ConnectionLevel level = ConnectionLevel.MID;
				for(int i = 0; i < 3 && radius == 0; i++) {
					level = ConnectionLevel.values()[i];
					IBlockState blockState = world.getBlockState(dPos.up(level.getYOffset()));
					if(blockState.getBlock() instanceof BlockSurfaceRoot) {
						radius = ((BlockSurfaceRoot)blockState.getBlock()).getRadius(blockState);
					} else
					if(level == ConnectionLevel.MID && TreeHelper.isBranch(blockState) && TreeHelper.getTreePart(blockState).getRadius(blockState) >= 8) {
						radius = 8;
					}
				}
				int horIndex = dir.getHorizontalIndex();
				retval = retval.withProperty(CONNECTIONS[horIndex], radius).withProperty(LEVELS[horIndex], level);
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
	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos,	EnumFacing side) {
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
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess blockAccess, BlockPos pos) {
		
		if (state.getBlock() != this) {
			return NULL_AABB;
		}
		
		int thisRadius = getRadius(state);
		
		boolean connectionMade = false;
		double radius = thisRadius / 16.0;
		double gap = 0.5 - radius;
		AxisAlignedBB aabb = new AxisAlignedBB(-radius, 0, -radius, radius, radius + (1/16d), radius);
		for (EnumFacing dir : EnumFacing.VALUES) {
			RootConnection conn = getSideConnectionRadius(blockAccess, pos, thisRadius, dir);
			if (conn != null) {
				connectionMade = true;
				aabb = aabb.expand(dir.getFrontOffsetX() * gap, dir.getFrontOffsetY() * gap, dir.getFrontOffsetZ() * gap);
			}
		}
		if (connectionMade) {
			return aabb.offset(0.5, 0.0, 0.5);
		}
		
		return new AxisAlignedBB(0.5 - radius, 0, 0.5 - radius, 0.5 + radius, radius, 0.5 + radius);
	}
	
	@Override
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean p_185477_7_) {
		if(entityIn instanceof EntityFallingTree) {
			return;
		}
		
		boolean connectionMade = false;
		int thisRadius = getRadius(state);
		
		for (EnumFacing dir : EnumFacing.HORIZONTALS) {
			RootConnection conn = getSideConnectionRadius(world, pos, thisRadius, dir);
			if (conn != null) {
				connectionMade = true;
				double radius = MathHelper.clamp(conn.radius, 1, thisRadius) / 16.0;
				double gap = 0.5 - radius;
				AxisAlignedBB aabb = new AxisAlignedBB(-radius, 0, -radius, radius, radius + (1/16d), radius);
				aabb = aabb.expand(dir.getFrontOffsetX() * gap, 0, dir.getFrontOffsetZ() * gap).offset(0.5, 0.0, 0.5);
				addCollisionBoxToList(pos, entityBox, collidingBoxes, aabb);
			}
		}
		
		if(!connectionMade) {
			AxisAlignedBB aabb = new AxisAlignedBB(0.5, 0.5, 0.5, 0.5, 0.5, 0.5).grow(thisRadius);
			addCollisionBoxToList(pos, entityBox, collidingBoxes, aabb);
		}
		
	}
	
	protected RootConnection getSideConnectionRadius(IBlockAccess blockAccess, BlockPos pos, int radius, EnumFacing side) {
		
		if(side.getAxis().isHorizontal()) {
			BlockPos dPos = pos.offset(side);
			IBlockState blockState = blockAccess.getBlockState(dPos);
			ConnectionLevel level = blockState.isNormalCube() ? ConnectionLevel.HIGH : (blockState.getBlock() == Blocks.AIR ? ConnectionLevel.LOW : ConnectionLevel.MID); 
			
			if(level != ConnectionLevel.MID) {
				dPos = dPos.up(level.yOffset);
				blockState = blockAccess.getBlockState(dPos);
			}
			
			if(blockState.getBlock() instanceof BlockSurfaceRoot) {
				return new RootConnection(level, ((BlockSurfaceRoot)blockState.getBlock()).getRadius(blockState));
			} else
			if(level == ConnectionLevel.MID && TreeHelper.isBranch(blockState) && TreeHelper.getTreePart(blockState).getRadius(blockState) >= 8) {
				return new RootConnection(ConnectionLevel.MID, 8);
			}
			
		}
		
		return null;
	}
	
}
