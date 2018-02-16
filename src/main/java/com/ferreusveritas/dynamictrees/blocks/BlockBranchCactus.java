package com.ferreusveritas.dynamictrees.blocks;

import java.util.List;

import javax.annotation.Nullable;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.cells.CellNull;
import com.ferreusveritas.dynamictrees.api.cells.ICell;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.MathHelper;
import com.google.common.base.Predicate;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockBranchCactus extends BlockBranch {
	
	// The direction it grew from. Can't be up, since cacti can't grow down.
	public static final PropertyEnum<EnumFacing> ORIGIN = PropertyEnum.<EnumFacing>create("origin", EnumFacing.class, new Predicate<EnumFacing>() {
		public boolean apply(@Nullable EnumFacing dir) {
			return dir != EnumFacing.UP;
		}
	});
	 // Not sure it's technically called the 'trunk' on cacti, but whatever
	public static final PropertyBool TRUNK = PropertyBool.create("trunk");
	
	public BlockBranchCactus(String name) {
		super(Material.CACTUS);
		setSoundType(SoundType.CLOTH);
		setHarvestLevel("axe", 0);
		setDefaultState(this.blockState.getBaseState().withProperty(TRUNK, true).withProperty(ORIGIN, EnumFacing.DOWN));
		setUnlocalizedName(name);
		setRegistryName(name);
	}
	
	///////////////////////////////////////////
	// BLOCKSTATES
	///////////////////////////////////////////
	
	@Override
	protected BlockStateContainer createBlockState() {
		IProperty[] listedProperties = { ORIGIN, TRUNK };
		return new ExtendedBlockState(this, listedProperties, CONNECTIONS);
	}
	
	/**
	* Convert the given metadata into a BlockState for this Block
	*/
	@Override
	public IBlockState getStateFromMeta(int meta) {
		EnumFacing rootDir = EnumFacing.getFront((meta & 7) % 6);
		boolean trunk = rootDir == EnumFacing.UP;
			
		return this.getDefaultState().withProperty(ORIGIN, trunk ? EnumFacing.DOWN : rootDir).withProperty(TRUNK, trunk);
	}
	
	/**
	* Convert the BlockState into the correct metadata value
	*/
	@Override
	public int getMetaFromState(IBlockState state) {
		if (state.getValue(TRUNK)) {
			return EnumFacing.UP.getIndex();
		}
		return state.getValue(ORIGIN).getIndex();
	}
	
	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		if (state instanceof IExtendedBlockState) {
			IExtendedBlockState retval = (IExtendedBlockState) state;
			int thisRadius = getRadius(state);
	
			for (EnumFacing dir : EnumFacing.VALUES) {
				retval = retval.withProperty(CONNECTIONS[dir.getIndex()], getSideConnectionRadius(world, pos, thisRadius, dir));
			}
			return retval;
		}
	
		return state;
	}
	
	///////////////////////////////////////////
	// INTERACTION
	///////////////////////////////////////////
	
	@Override
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
        entityIn.attackEntityFrom(DamageSource.CACTUS, 1.0F);
    }
	
	///////////////////////////////////////////
	// RENDERING
	///////////////////////////////////////////
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos,	EnumFacing side) {
		return true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT_MIPPED;
	}
	
	///////////////////////////////////////////
	// GROWTH
	///////////////////////////////////////////
    
    @Override
	public ICell getHydrationCell(IBlockAccess blockAccess, BlockPos pos, IBlockState blockState, EnumFacing dir, ILeavesProperties leavesProperties) {
		return CellNull.NULLCELL;
	}
    
    @Override
	public int getRadius(IBlockAccess blockAccess, BlockPos pos) {
		return getRadius(blockAccess.getBlockState(pos));
	}
	
    @Override
	public int getRadius(IBlockState blockState) {
		if (blockState.getBlock() == this) {
			return blockState.getValue(TRUNK) ? 5 : 4;
		} else {
			return 0;
		}
	}
	
	@Override
	public void setRadius(World world, BlockPos pos, int radius) {
		// Do nothing
	}
	
	// Directionless probability grabber
	@Override
	public int probabilityForBlock(IBlockAccess blockAccess, BlockPos pos, BlockBranch from) {
		return isSameWood(from) ? getRadius(blockAccess, pos) + 2 : 0;
	}
    
    @Override
    public GrowSignal growIntoAir(World world, BlockPos pos, GrowSignal signal, int fromRadius) {
    	EnumFacing originDir = signal.dir.getOpposite(); // Direction this signal originated from
    	boolean trunk = signal.isInTrunk();
    	
    	if (originDir.getAxis() != EnumFacing.Axis.Y && (world.getBlockState(pos.up()).getBlock() == this || world.getBlockState(pos.down()).getBlock() == this)) {
    		signal.success = false;
    		return signal;
    	}
    	
    	signal.success = world.setBlockState(pos, this.blockState.getBaseState().withProperty(TRUNK, trunk).withProperty(ORIGIN, originDir), 2);
    	signal.radius = (int) (trunk ? signal.getSpecies().getPrimaryThickness(): signal.getSpecies().getSecondaryThickness());
    	return signal;
    }
    
    @Override
	public GrowSignal growSignal(World world, BlockPos pos, GrowSignal signal) {
		
		if (signal.step()) { // This is always placed at the beginning of every growSignal function
			Species species = signal.getSpecies();
			
			//EnumFacing originDir = signal.dir.getOpposite(); // Direction this signal originated from
			EnumFacing targetDir = species.selectNewDirection(world, pos, this, signal); // This must be cached on the stack for proper recursion
			signal.doTurn(targetDir);
			
			BlockPos deltaPos = pos.offset(targetDir);
			
			// Pass grow signal to next block in path
			ITreePart treepart = TreeHelper.getTreePart(world, deltaPos);
			
			if (treepart == this) {
				signal = treepart.growSignal(world, deltaPos, signal); // Recurse
			} else if (world.isAirBlock(deltaPos)) {
				signal = growIntoAir(world, deltaPos, signal, (int) signal.radius);
			}
		}
		
		return signal;
	}
    
	///////////////////////////////////////////
	// PHYSICAL BOUNDS
	///////////////////////////////////////////
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess blockAccess, BlockPos pos) {
		if (state.getBlock() != this) {
			return NULL_AABB;
		}
		
		int thisRadius = getRadius(state);
		
		boolean connectionMade = false;
		double radius = thisRadius / 16.0;
		double gap = 0.5 - radius;
		AxisAlignedBB aabb = new AxisAlignedBB(0, 0, 0, 0, 0, 0).grow(radius);
		for (EnumFacing dir : EnumFacing.VALUES) {
			if (getSideConnectionRadius(blockAccess, pos, thisRadius, dir) > 0) {
				connectionMade = true;
				aabb = aabb.expand(dir.getFrontOffsetX() * gap, dir.getFrontOffsetY() * gap, dir.getFrontOffsetZ() * gap);
			}
		}
		if (connectionMade) {
			return aabb.offset(0.5, 0.5, 0.5);
		}
		return new AxisAlignedBB(0.5 - radius, 0.5 - radius, 0.5 - radius, 0.5 + radius, 0.5 + radius, 0.5 + radius);
	}
	
	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean p_185477_7_) {
		int thisRadius = getRadius(state);
		
		for (EnumFacing dir : EnumFacing.VALUES) {
			int connRadius = getSideConnectionRadius(worldIn, pos, thisRadius, dir);
			if (connRadius > 0) {
				double radius = MathHelper.clamp(connRadius, 1, thisRadius) / 16.0;
				double gap = 0.5 - radius;
				AxisAlignedBB aabb = new AxisAlignedBB(0, 0, 0, 0, 0, 0).grow(radius);
				aabb = aabb.offset(dir.getFrontOffsetX() * gap, dir.getFrontOffsetY() * gap, dir.getFrontOffsetZ() * gap).offset(0.5, 0.5, 0.5);
				addCollisionBoxToList(pos, entityBox, collidingBoxes, aabb);
			}
		}
		
		double min = 0.5 - (thisRadius / 16.0), max = 0.5 + (thisRadius / 16.0);
		addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(min, min, min, max, max, max));
	}
	
	@Override
	public int getRadiusForConnection(IBlockAccess world, BlockPos pos, BlockBranch from, int fromRadius) {
		return 0;
	}
	
	@Override
	public int getSideConnectionRadius(IBlockAccess blockAccess, BlockPos pos, int radius, EnumFacing side) {
		BlockPos deltaPos = pos.offset(side);
		//ITreePart otherPart = TreeHelper.getTreePart(blockAccess, deltaPos);
		IBlockState otherState = blockAccess.getBlockState(deltaPos);
		IBlockState state = blockAccess.getBlockState(pos);
		
		if (otherState.getBlock() == this && state.getBlock() == this && (otherState.getValue(ORIGIN) == side.getOpposite() || state.getValue(ORIGIN) == side)) {
			return (state.getValue(TRUNK) && otherState.getValue(TRUNK)) ? 5 : 4;
		} else if (side == EnumFacing.DOWN && state.getBlock() == this && state.getValue(TRUNK) && state.getValue(ORIGIN) == side) {
			return 5;
		}
		
		return 0;
	}
	
	///////////////////////////////////////////
	// NODE ANALYSIS
	///////////////////////////////////////////
	
	@Override
	public MapSignal analyse(World world, BlockPos pos, EnumFacing fromDir, MapSignal signal) {
		// Note: fromDir will be null in the origin node
		if (signal.depth++ < 32) {// Prevents going too deep into large networks, or worse, being caught in a network loop
			IBlockState state = world.getBlockState(pos);
			signal.run(world, this, pos, fromDir);// Run the inspectors of choice
			for (EnumFacing dir : EnumFacing.VALUES) {// Spread signal in various directions
				if (dir != fromDir) {// don't count where the signal originated from
					BlockPos deltaPos = pos.offset(dir);
					IBlockState adjState = world.getBlockState(deltaPos);
					
					if (adjState.getBlock() == this && adjState.getValue(ORIGIN) == dir.getOpposite()) {
						signal = ((ITreePart) adjState.getBlock()).analyse(world, deltaPos, dir.getOpposite(), signal);
					} else if (state.getBlock() == this && state.getValue(ORIGIN) == dir) {
						signal = TreeHelper.getTreePart(world, deltaPos).analyse(world, deltaPos, dir.getOpposite(), signal);
					}
					
					// This should only be true for the originating block when the root node is found
					if (signal.found && signal.localRootDir == null && fromDir == null) {
						signal.localRootDir = dir;
					}
				}
			}
			signal.returnRun(world, this, pos, fromDir);
		} else {
			world.setBlockToAir(pos);// Destroy one of the offending nodes
			signal.overflow = true;
		}
		signal.depth--;
		return signal;
	}
    
}
