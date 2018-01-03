package com.ferreusveritas.dynamictrees.blocks;

import java.util.List;
import java.util.Random;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockDynamicSapling extends Block implements IGrowable {
	
	public Species tree;
	
	public BlockDynamicSapling(String name) {
		super(Material.PLANTS);
		setDefaultState(this.blockState.getBaseState());
		setSoundType(SoundType.PLANT);
		setTickRandomly(true);
		setUnlocalizedName(name);
		setRegistryName(name);
	}
	
	///////////////////////////////////////////
	// INTERACTION
	///////////////////////////////////////////
	
	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		grow(world, rand, pos, state);
	}
	
	public static boolean canSaplingStay(World world, Species species, BlockPos pos) {
		//Ensure there are no adjacent branches or other saplings
		for(EnumFacing dir: EnumFacing.HORIZONTALS) {
			IBlockState blockState = world.getBlockState(pos.offset(dir));
			Block block = blockState.getBlock();
			if(TreeHelper.isBranch(block) || block instanceof BlockDynamicSapling) {
				return false;
			}
		}
		
		//Air above and acceptable soil below
		return world.isAirBlock(pos.up()) && species.isAcceptableSoil(world, pos.down(), world.getBlockState(pos.down()));
	}
	
	public boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
		return canSaplingStay(world, getSpecies(state), pos);
	}

	@Override
	public void grow(World world, Random rand, BlockPos pos, IBlockState state) {
		Species species = getSpecies(state);
		if(canBlockStay(world, pos, state)) {
			//Ensure planting conditions are right
			DynamicTree tree = species.getTree();
			if(world.isAirBlock(pos.up()) && species.isAcceptableSoil(world, pos.down(), world.getBlockState(pos.down()))) {
				world.setBlockState(pos, tree.getDynamicBranch().getDefaultState());//set to a single branch with 1 radius
				world.setBlockState(pos.up(), tree.getDynamicLeavesState());//Place a single leaf block on top
				species.placeRootyDirtBlock(world, pos.down(), 15);//Set to fully fertilized rooty dirt underneath
			}
		} else {
			dropBlock(world, species, state, pos);
		}
	}
	
	
	///////////////////////////////////////////
	// TREE INFORMATION
	///////////////////////////////////////////
	
	public Species getSpecies(IBlockState state) {
		return this.tree;
	}
	
	public BlockDynamicSapling setSpecies(IBlockState state, Species species) {
		this.tree = species;
		return this;
	}
	
	
	///////////////////////////////////////////
	// DROPS
	///////////////////////////////////////////
	
	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
		if (!this.canBlockStay(world, pos, state)) {
			dropBlock(world, getSpecies(state), state, pos);
		}
	}
	
	private void dropBlock(World world, Species tree, IBlockState state, BlockPos pos) {
		world.setBlockToAir(pos);
		dropBlockAsItem(world, pos, state, 0);
	}
	
	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		List<ItemStack> dropped = super.getDrops(world, pos, state, fortune);
		dropped.add(getSpecies(state).getSeedStack(1));
		return dropped;
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return null;
	}
	
	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return getSpecies(state).getSeedStack(1);
	}
	
	
	///////////////////////////////////////////
	// PHYSICAL BOUNDS
	///////////////////////////////////////////
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return new AxisAlignedBB(0.25f, 0.0f, 0.25f, 0.75f, 0.75f, 0.75f);
	}
	
	
	///////////////////////////////////////////
	// RENDERING
	///////////////////////////////////////////
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT_MIPPED;
	}
	
	@Override
	public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
		System.out.println("test");
		return getSpecies(state).canGrowWithBoneMeal(world, pos);
	}
	
	@Override
	public boolean canUseBonemeal(World world, Random rand, BlockPos pos, IBlockState state) {
		return getSpecies(state).canUseBoneMealNow(world, rand, pos);
	}
	
}
