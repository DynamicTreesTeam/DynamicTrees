package com.ferreusveritas.dynamictrees.blocks;

import java.util.Random;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.cells.CellNull;
import com.ferreusveritas.dynamictrees.api.cells.ICell;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.MathHelper;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockRooty extends Block implements ITreePart {
	
	public static final PropertyInteger LIFE = PropertyInteger.create("life", 0, 15);
	
	public BlockRooty(String name, Material material) {
		super(material);
		setSoundType(SoundType.GROUND);
		setDefaultState(this.blockState.getBaseState().withProperty(LIFE, 15));
		setTickRandomly(true);
		setCreativeTab(DynamicTrees.dynamicTreesTab);
		setUnlocalizedName(name);
		setRegistryName(name);
	}
	
	///////////////////////////////////////////
	// BLOCKSTATES
	///////////////////////////////////////////
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[]{LIFE});
	}
	
	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(LIFE, meta);
	}
	
	/**
	 * Convert the BlockState into the correct metadata value
	 */
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(LIFE).intValue();
	}
	
	///////////////////////////////////////////
	// INTERACTION
	///////////////////////////////////////////
	
	@Override
	public void randomTick(World world, BlockPos pos, IBlockState state, Random random) {
		updateTree(world, pos, random, false);
	}
	
	public EnumFacing getTrunkDirection(IBlockAccess access, BlockPos rootPos) {
		return EnumFacing.UP; 
	}
	
	/**
	 * 
	 * @param world
	 * @param rootPos
	 * @param random
	 * @return false if tree was not found
	 */
	public boolean updateTree(World world, BlockPos rootPos, Random random, boolean rapid) {
		
		if(CoordUtils.isSurroundedByLoadedChunks(world, rootPos)) {

			boolean viable = false;
			
			Species species = getSpecies(world, rootPos);

			if(species != Species.NULLSPECIES) {
				BlockPos treePos = rootPos.offset(getTrunkDirection(world, rootPos));
				ITreePart treeBase = TreeHelper.getTreePart(world, treePos);

				if(treeBase != TreeHelper.nullTreePart) {
					viable = species.update(world, this, rootPos, getSoilLife(world, rootPos), treeBase, treePos, random, rapid);
				}
			}
			
			if(!viable) {
				world.setBlockState(rootPos, getDecayBlockState(world, rootPos), 3);
			}

		}

		return true;
	}
	
	/**
	 * This is the state the rooty dirt returns to once it no longer supports a tree structure.
	 * 
	 * @param access
	 * @param pos The position of the {@link BlockRooty}
	 * @return
	 */
	public IBlockState getDecayBlockState(IBlockAccess access, BlockPos pos) {
		return Blocks.DIRT.getDefaultState();
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Item.getItemFromBlock(Blocks.DIRT);
	}
	
	@Override
	public float getBlockHardness(IBlockState blockState, World worldIn, BlockPos pos) {
		return 20.0f;//Encourage proper tool usage and discourage bypassing tree felling by digging the root from under the tree
	};
	
	@Override
	protected boolean canSilkHarvest() {
		return false;
	}
	
	@Override
	public boolean hasComparatorInputOverride(IBlockState state) {
		return true;
	}
	
	@Override
	public int getComparatorInputOverride(IBlockState blockState, World world, BlockPos pos) {
		return getSoilLife(world, pos);
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack heldItem = player.getHeldItem(hand);
		return getTree(world, pos).onTreeActivated(world, pos, state, player, hand, heldItem, facing, hitX, hitY, hitZ);
	}
	
	public void destroyTree(World world, BlockPos pos) {
		BlockBranch branch = TreeHelper.getBranch(world, pos.up());
		if(branch != null) {
			branch.destroyEntireTree(world, pos.up());
		}
	}
	
	@Override
	public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		destroyTree(world, pos);
	}
	
	@Override
	public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
		destroyTree(world, pos);
	}
	
	public int getSoilLife(IBlockAccess blockAccess, BlockPos pos) {
		return blockAccess.getBlockState(pos).getValue(LIFE);
	}
	
	public void setSoilLife(World world, BlockPos pos, int life) {
		world.setBlockState(pos, getDefaultState().withProperty(LIFE, MathHelper.clamp(life, 0, 15)), 3);
		world.notifyNeighborsOfStateChange(pos, this, false);//Notify all neighbors of NSEWUD neighbors(for comparator)
	}
	
	public boolean fertilize(World world, BlockPos pos, int amount) {
		int soilLife = getSoilLife(world, pos);
		if((soilLife == 0 && amount < 0) || (soilLife == 15 && amount > 0)) {
			return false;//Already maxed out
		}
		setSoilLife(world, pos, soilLife + amount);
		return true;
	}
	
	@Override
	public ICell getHydrationCell(IBlockAccess blockAccess, BlockPos pos, IBlockState blockState, EnumFacing dir, ILeavesProperties leavesTree) {
		return CellNull.NULLCELL;
	}
	
	@Override
	public GrowSignal growSignal(World world, BlockPos pos, GrowSignal signal) {
		return signal;
	}
	
	@Override
	public int getRadiusForConnection(IBlockAccess blockAccess, BlockPos pos, BlockBranch from, int fromRadius) {
		return 8;
	}
	
	@Override
	public int probabilityForBlock(IBlockAccess blockAccess, BlockPos pos, BlockBranch from) {
		return 0;
	}
	
	@Override
	public int getRadius(IBlockAccess blockAccess, BlockPos pos) {
		return 0;
	}
	
	public MapSignal startAnalysis(World world, BlockPos rootPos, MapSignal signal) {
		EnumFacing dir = getTrunkDirection(world, rootPos);
		BlockPos treePos = rootPos.offset(dir);
		
		TreeHelper.getTreePart(world, treePos).analyse(world, treePos, null, signal);
		
		return signal;
	}
	
	@Override
	public MapSignal analyse(World world, BlockPos pos, EnumFacing fromDir, MapSignal signal) {
		signal.run(world, this, pos, fromDir);//Run inspector of choice
		
		signal.root = pos;
		signal.found = true;
		
		return signal;
	}
	
	@Override
	public int branchSupport(IBlockAccess blockAccess, BlockBranch branch, BlockPos pos, EnumFacing dir, int radius) {
		return dir == EnumFacing.DOWN ? BlockBranch.setSupport(1, 1) : 0;
	}

	@Override
	public DynamicTree getTree(IBlockAccess blockAccess, BlockPos pos) {
		BlockPos treePos = pos.offset(getTrunkDirection(blockAccess, pos));
		return TreeHelper.isBranch(blockAccess, treePos) ? TreeHelper.getBranch(blockAccess, treePos).getTree(blockAccess, treePos) : DynamicTree.NULLTREE;
	}

	/**
	 * Rooty Dirt can report whatever {@link DynamicTree} species it wants to be.  By default we'll just 
	 * make it report whatever {@link DynamicTree} the above {@link BlockBranch} says it is.
	 */
	public Species getSpecies(World world, BlockPos rootPos) {
		BlockPos treePos = rootPos.offset(getTrunkDirection(world, rootPos));
		return TreeHelper.isBranch(world, treePos) ? TreeHelper.getBranch(world, treePos).getTree(world, treePos).getSpeciesForLocation(world, treePos) : Species.NULLSPECIES;
	}
	
	@Override
	public EnumPushReaction getMobilityFlag(IBlockState state) {
		return EnumPushReaction.BLOCK;
	}
	
	public final TreePartType getTreePartType() {
		return TreePartType.ROOT;
	}
	
	@Override
	public final boolean isRootNode() {
		return true;
	}
}
