package com.ferreusveritas.dynamictrees.blocks;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockFruit extends Block implements IGrowable {
	
	protected static final AxisAlignedBB[] FRUIT_AABB = new AxisAlignedBB[] {
			new AxisAlignedBB(7/16.0, 1f, 7/16.0, 9/16.0, 15/16.0, 9/16.0),
			new AxisAlignedBB(7/16.0, 1f, 7/16.0, 9/16.0, 14/16.0, 9/16.0),
			new AxisAlignedBB(6/16.0, 1f, 6/16.0, 10/16.0, 12/16.0, 10/16.0),
			new AxisAlignedBB(6/16.0, 15/16.0, 6/16.0, 10/16.0, 11/16.0, 10/16.0)
	};
	
	public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 3);
	
	public static final String name = "fruit";
	
	public BlockFruit() {
		this(name);
	}
	
	public BlockFruit(String name) {
		super(Material.PLANTS);
		this.setDefaultState(this.blockState.getBaseState().withProperty(AGE, Integer.valueOf(0)));
		setRegistryName(name);
		setUnlocalizedName(name);
		this.setTickRandomly(true);
		this.setHardness(0.3f);
	}
	
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		if (!this.canBlockStay(worldIn, pos, state)) {
			this.dropBlock(worldIn, pos, state);
		}
		else {
			int age = state.getValue(AGE);
			
			if (age < 3 && net.minecraftforge.common.ForgeHooks.onCropsGrowPre(worldIn, pos, state, rand.nextInt(5) == 0)) {
				worldIn.setBlockState(pos, state.withProperty(AGE, age + 1), 2);
				net.minecraftforge.common.ForgeHooks.onCropsGrowPost(worldIn, pos, state, worldIn.getBlockState(pos));
			}
		}
	}
	
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		if (!this.canBlockStay(worldIn, pos, state)) {
			this.dropBlock(worldIn, pos, state);
		}
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if ( ((Integer)state.getValue(AGE)).intValue() >= 3 ) {
			this.dropBlock(worldIn, pos, state);
			return true;
		}
		
		return false;
	}
	
	private void dropBlock(World worldIn, BlockPos pos, IBlockState state) {
		worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
		this.dropBlockAsItem(worldIn, pos, state, 0);
	}
	
	/**
	 * Checks if Leaves of any kind are above this block.  Not picky.
	 * 
	 * @param world
	 * @param pos
	 * @param state
	 * @return true if there are leaves above the block
	 */
	public boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
		return world.getBlockState(pos.up()).getBlock() instanceof BlockLeaves;
	}
	
	@Override
	public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
		return false;
	}
	
	@Override
	public boolean canUseBonemeal(World world, Random rand, BlockPos pos, IBlockState state) {
		return false;//Q:Does dusting an apple with bone dust make it grow faster?  A:No.
	}
	
	@Override
	public void grow(World world, Random rand, BlockPos pos, IBlockState state) {
		world.setBlockState(pos, state.withProperty(AGE, Integer.valueOf(((Integer)state.getValue(AGE)).intValue() + 1)), 2);
	}
	
	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		super.getDrops(drops, world, pos, state, fortune);
		
		if(state.getValue(AGE) >= 3) {
			drops.add(new ItemStack(Items.APPLE));
		}
	}
	
	
	///////////////////////////////////////////
	// BOUNDARIES
	///////////////////////////////////////////
	
	public boolean isFullCube(IBlockState state) {
		return false;
	}
	
	/**
	 * Used to determine ambient occlusion and culling when rebuilding chunks for render
	 */
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return FRUIT_AABB[state.getValue(AGE)];
	}
	
	
	///////////////////////////////////////////
	// BLOCKSTATE
	///////////////////////////////////////////
	
	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(AGE, Integer.valueOf(meta & 3));
	}
	
	/**
	 * Convert the BlockState into the correct metadata value
	 */
	public int getMetaFromState(IBlockState state) {
		return state.getValue(AGE) & 3;
	}
	
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] {AGE});
	}
	
}
