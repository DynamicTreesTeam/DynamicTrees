package com.ferreusveritas.dynamictrees.blocks;

import java.util.List;
import java.util.Optional;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;
import com.ferreusveritas.dynamictrees.util.CoordUtils.Surround;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.BlockStateContainer.StateImplementation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class BlockBranchThick extends BlockBranchBasic implements IMusable {
	
	protected static final AxisAlignedBB maxBranchBB = new AxisAlignedBB(-1, 0, -1, 2, 1, 2);
	public static final int RADMAX_THICK = 24;
	
	protected static final PropertyInteger RADIUSNYBBLE = PropertyInteger.create("radius", 0, 15);
	protected final boolean extended;
	public BlockBranchThick otherBlock;
	
	public BlockBranchThick(Material material, String name) {
		this(material, name, false);
		otherBlock = new BlockBranchThick(material, name + "x", true);
		otherBlock.otherBlock = this;
		
		cacheBranchThickStates();
	}
	
	protected BlockBranchThick(Material material, String name, boolean extended) {
		super(material, name);
		this.extended = extended;
	}
	
	public BlockBranchThick getPairSide(boolean ext) {
		return extended ^ ext ? otherBlock : this; 
	}

	@Override
	public void cacheBranchStates() { }
	
	public void cacheBranchThickStates() {
		if(!extended) {
			branchStates = new IBlockState[RADMAX_THICK + 1];
			otherBlock.branchStates = branchStates;
			
			//Cache the branch blocks states for rapid lookup
			branchStates[0] = Blocks.AIR.getDefaultState();
			
			for(int radius = 1; radius <= RADMAX_THICK; radius++) {
				branchStates[radius] = getPairSide(radius > 16).getDefaultState().withProperty(BlockBranchThick.RADIUSNYBBLE, (radius - 1) & 0x0f);
			}
		}
	}
	
	public IProperty<?>[] getIgnorableProperties() {
		return new IProperty<?>[]{ RADIUSNYBBLE };
	}
	
	@Override
	public void setFamily(TreeFamily tree) {
		super.setFamily(tree);
		if (!extended) otherBlock.setFamily(tree);
	}
	
	@Override
	public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity) {
		return false;
	}
	
	///////////////////////////////////////////
	// BLOCKSTATES
	///////////////////////////////////////////
	
	//////////////////////////////////////////////////
	//The following is highly experimental code
	//////////////////////////////////////////////////
	class StateImplementationCachedRadius extends StateImplementation {
		protected StateImplementationCachedRadius(Block blockIn, ImmutableMap < IProperty<?>, Comparable<? >> propertiesIn) { super(blockIn, propertiesIn); }
		protected StateImplementationCachedRadius(Block blockIn, ImmutableMap<IProperty<?>, Comparable<?>> propertiesIn, ImmutableTable<IProperty<?>, Comparable<?>, IBlockState> propertyValueTable) { super(blockIn, propertiesIn, propertyValueTable); }
		
		private int radius = -1;
		int getRadius() {
			return radius;
		}
	}
	
	class BlockStateContainerCustom extends BlockStateContainer {
		public BlockStateContainerCustom(Block blockIn, IProperty<?>... properties) { this(blockIn, properties, null); }
		protected BlockStateContainerCustom(Block blockIn, IProperty<?>[] properties, ImmutableMap<IUnlistedProperty<?>, Optional<?>> unlistedProperties) { super(blockIn, properties, unlistedProperties); }
		
		@Override
		protected StateImplementation createState(Block block, ImmutableMap<IProperty<?>, Comparable<?>> properties, ImmutableMap<IUnlistedProperty<?>, Optional<?>> unlistedProperties) {
			return new StateImplementationCachedRadius(block, properties);
		}
	}
	//////////////////////////////////////////////////
	//end highly experimental code
	//////////////////////////////////////////////////
	
	@Override
	protected BlockStateContainer createBlockState() {
		IProperty[] listedProperties = { RADIUSNYBBLE };
		return new ExtendedBlockState(this, listedProperties, CONNECTIONS);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(RADIUSNYBBLE, meta);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(RADIUSNYBBLE);
	}
	
	
	///////////////////////////////////////////
	// GROWTH
	///////////////////////////////////////////
	
	public int getRadius(IBlockState blockState) {
		return MathHelper.clamp(blockState.getValue(RADIUSNYBBLE) + (((BlockBranchThick)blockState.getBlock()).extended ? 17 : 1), 1, getMaxRadius());
	}
	
	@Override
	public void setRadius(World world, BlockPos pos, int radius, EnumFacing originDir, int flags) {
		
		//If the radius is <= 8 then we can just set the block as normal and move on
		if(radius <= RADMAX_NORMAL) {
			super.setRadius(world, pos, radius, originDir, flags);
			return;
		}
		
		ReplaceableState repStates[] = new ReplaceableState[8];
		
		boolean setable = true;
		
		for(Surround dir : Surround.values()) {
			BlockPos dPos = pos.add(dir.getOffset());
			ReplaceableState rep = getReplaceability(world, dPos);
			repStates[dir.ordinal()] = rep;
			if (rep == ReplaceableState.BLOCKING) {
				setable = false;
				break;
			}
		}
		
		if(setable) {
			super.setRadius(world, pos, radius, originDir, flags);
			
			for(Surround dir : Surround.values()) {
				BlockPos dPos = pos.add(dir.getOffset());
				ReplaceableState rep = repStates[dir.ordinal()];
				if(rep == ReplaceableState.REPLACEABLE) {
					world.setBlockState(dPos, ModBlocks.blockTrunkShell.getDefaultState().withProperty(BlockTrunkShell.COREDIR, dir.getOpposite()), flags);
				}
			}
		} else {
			super.setRadius(world, pos, RADMAX_NORMAL, originDir, flags);
		}
		
	}
	
	public ReplaceableState getReplaceability(World world, BlockPos pos) {
		
		IBlockState state = world.getBlockState(pos);
		
		if(state.getBlock().isReplaceable(world, pos)) {
			return ReplaceableState.REPLACEABLE;
		}
		
		Block block = state.getBlock();		
		
		if(TreeHelper.isTreePart(block)) {
			return ReplaceableState.TREEPART;
		}
		
		if(getFamily().getCommonSpecies().isAcceptableSoil(world, pos, state)) {
			return ReplaceableState.REPLACEABLE;
		}
		
		//TODO: Possible configurable whitelist for destructable blocks 
		
		return ReplaceableState.BLOCKING;
	}
	
	enum ReplaceableState {
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
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess blockAccess, BlockPos pos) {
		int thisRadius = getRadius(state);
		if(thisRadius <= RADMAX_NORMAL) {
			return super.getBoundingBox(state, blockAccess, pos);
		}
		
		double radius = thisRadius / 16.0;
		return new AxisAlignedBB(0.5 - radius, 0.0, 0.5 - radius, 0.5 + radius, 1.0, 0.5 + radius);
	}
	
	@Override
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean p_185477_7_) {
		int radius = getRadius(state);
		if(radius <= RADMAX_NORMAL) {
			super.addCollisionBoxToList(state, world, pos, entityBox, collidingBoxes, entityIn, p_185477_7_);
			return;
		}
		
		addCollisionBoxToList(pos, entityBox, collidingBoxes, getBoundingBox(state, world, pos));
	}
	
	@Override
	public boolean isMusable() {
		return true;
	}
	
}