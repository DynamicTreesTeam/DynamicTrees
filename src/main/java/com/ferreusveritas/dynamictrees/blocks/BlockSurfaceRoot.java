package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
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
		new Properties.PropertyAdapter<SearchLevel>(PropertyEnum.create("levels", SearchLevel.class)),
		new Properties.PropertyAdapter<SearchLevel>(PropertyEnum.create("levelw", SearchLevel.class)),
		new Properties.PropertyAdapter<SearchLevel>(PropertyEnum.create("leveln", SearchLevel.class)),
		new Properties.PropertyAdapter<SearchLevel>(PropertyEnum.create("levele", SearchLevel.class))
	};
	
	public BlockSurfaceRoot(Material material, String name) {
		super(material);
		setUnlocalizedName(name);
		setRegistryName(name);
		setHarvestLevel("axe", 0);
		setCreativeTab(DynamicTrees.dynamicTreesTab);
	}
	
	enum SearchLevel implements IStringSerializable {
		
		MID(0),
		LOW(-1),
		HIGH(1);

		private final int yOffset;
		
		private SearchLevel(int y) {
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
			int search[] = new int[]{ 0,-1, 1 };//Search level then low, then high.
			
			for (EnumFacing dir : EnumFacing.HORIZONTALS) {
				BlockPos dPos = pos.offset(dir);
				int radius = 0;
				SearchLevel level = SearchLevel.MID;
				for(int i = 0; i < 3 && radius == 0; i++) {
					level = SearchLevel.values()[i];
					IBlockState blockState = world.getBlockState(dPos.up(level.getYOffset()));
					if(blockState.getBlock() instanceof BlockSurfaceRoot) {
						radius = ((BlockSurfaceRoot)blockState.getBlock()).getRadius(blockState);
					} else
					if(level == SearchLevel.MID && TreeHelper.isBranch(blockState) && TreeHelper.getTreePart(blockState).getRadius(blockState) >= 8) {
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
	
}
