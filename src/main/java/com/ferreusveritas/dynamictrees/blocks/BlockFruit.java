package com.ferreusveritas.dynamictrees.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.Random;

public class BlockFruit extends Block implements IGrowable {
	
	protected static final AxisAlignedBB[] FRUIT_AABB = new AxisAlignedBB[] {
			new AxisAlignedBB(7/16.0, 1f, 7/16.0, 9/16.0, 15/16.0, 9/16.0),
			new AxisAlignedBB(7/16.0, 1f, 7/16.0, 9/16.0, 14/16.0, 9/16.0),
			new AxisAlignedBB(6/16.0, 1f, 6/16.0, 10/16.0, 12/16.0, 10/16.0),
			new AxisAlignedBB(6/16.0, 15/16.0, 6/16.0, 10/16.0, 11/16.0, 10/16.0)
	};
	
//	public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 3);
	
	public static final String name = "fruit";
	
	protected ItemStack droppedFruit = ItemStack.EMPTY;
	protected boolean bonemealable = false;//Q:Does dusting an apple with bone dust make it grow faster?  A:No.
	
	public BlockFruit() {
		this(name);
	}

	public BlockFruit(String name) {
		super(Properties.create(Material.PLANTS)
				.tickRandomly()
				.hardnessAndResistance(0.3f));
		setRegistryName(name);
//		hasTileEntity = true;
	}

	public BlockFruit setBonemealable(boolean bonemealable) {
		this.bonemealable = bonemealable;
		return this;
	}
	
	public void updateTick(World worldIn, BlockPos pos, BlockState state, Random rand) {
//		if (!this.canBlockStay(worldIn, pos, state)) {
//			this.dropBlock(worldIn, pos, state);
//		}
//		else {
//			int age = state.getValue(AGE);
//
//			if (age < 3 && net.minecraftforge.common.ForgeHooks.onCropsGrowPre(worldIn, pos, state, rand.nextInt(5) == 0)) {
//				worldIn.setBlockState(pos, state.withProperty(AGE, age + 1), 2);
//				net.minecraftforge.common.ForgeHooks.onCropsGrowPost(worldIn, pos, state, worldIn.getBlockState(pos));
//			} else
//			if (age == 3) {
//				if(matureAction(worldIn, pos, state, rand)) {
//					dropBlock(worldIn, pos, state);
//				}
//			}
//		}
	}

//
//	/**
//	 * Override this to make the fruit do something once it's mature.
//	 *
//	 * @param world The world
//	 * @param pos The position of the fruit block
//	 * @param state The current blockstate of the fruit
//	 * @param rand A random number generator
//	 * @return true to drop the block. false will keep the fruit intact
//	 */
//	protected boolean matureAction(World world, BlockPos pos, BlockState state, Random rand) {
//		return false;
//	}
//
//	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
//		if (!this.canBlockStay(worldIn, pos, state)) {
//			this.dropBlock(worldIn, pos, state);
//		}
//	}
//
//	@Override
//	public boolean onBlockActivated(World worldIn, BlockPos pos, BlockState state, EntityPlayer playerIn, EnumHand hand, Direction facing, float hitX, float hitY, float hitZ) {
//		if ( ((Integer)state.getValue(AGE)).intValue() >= 3 ) {
//			this.dropBlock(worldIn, pos, state);
//			return true;
//		}
//
//		return false;
//	}
//
//	private void dropBlock(World worldIn, BlockPos pos, BlockState state) {
//		worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
//		this.dropBlockAsItem(worldIn, pos, state, 0);
//	}
//
//	/**
//	 * Checks if Leaves of any kind are above this block.  Not picky.
//	 *
//	 * @param world
//	 * @param pos
//	 * @param state
//	 * @return true if there are leaves above the block
//	 */
//	public boolean canBlockStay(World world, BlockPos pos, BlockState state) {
//		return world.getBlockState(pos.up()).getBlock() instanceof BlockLeaves;
//	}
//
//
//	///////////////////////////////////////////
//	//BONEMEAL
//	///////////////////////////////////////////

	@Override
	public boolean canGrow(IBlockReader worldIn, BlockPos pos, BlockState state, boolean isClient) {
		return false;
		//return (Integer)state.getValue(AGE) < 3;
	}

	@Override
	public boolean canUseBonemeal(World world, Random rand, BlockPos pos, BlockState state) {
		return bonemealable;
	}

	@Override
	public void grow(World worldIn, Random rand, BlockPos pos, BlockState state) {
//		int age = (Integer)state.getValue(AGE);
//		int newAge = MathHelper.clamp(age + 1, 0, 3);
//		if(newAge != age) {
//			world.setBlockState(pos, state.withProperty(AGE, newAge), 2);
//		}
	}
//
//
//	///////////////////////////////////////////
//	//DROPS
//	///////////////////////////////////////////
//
//	@Override
//	public void getDrops(NonNullList<ItemStack> drops, IBlockReader world, BlockPos pos, BlockState state, int fortune) {
//		super.getDrops(drops, world, pos, state, fortune);
//
//		if(state.getValue(AGE) >= 3) {
//			ItemStack toDrop = getFruitDrop();
//			if(!toDrop.isEmpty()) {
//				drops.add(toDrop);
//			}
//		}
//	}
//
//	public BlockFruit setDroppedItem(ItemStack stack) {
//		droppedFruit = stack;
//		return this;
//	}
//
//	//Override this for a custom item drop
//	public ItemStack getFruitDrop() {
//		return droppedFruit.copy();
//	}
//
//
//	///////////////////////////////////////////
//	// BOUNDARIES
//	///////////////////////////////////////////
//
//	public boolean isFullCube(BlockState state) {
//		return false;
//	}
//
//	/**
//	 * Used to determine ambient occlusion and culling when rebuilding chunks for render
//	 */
//	public boolean isOpaqueCube(BlockState state) {
//		return false;
//	}
//
//	public AxisAlignedBB getBoundingBox(BlockState state, IBlockReader source, BlockPos pos) {
//		return FRUIT_AABB[state.getValue(AGE)];
//	}
//
//	@Override
//	public BlockFaceShape getBlockFaceShape(IBlockReader worldIn, BlockState state, BlockPos pos, Direction face) {
//		return BlockFaceShape.UNDEFINED;
//	}
//
//	///////////////////////////////////////////
//	// BLOCKSTATE
//	///////////////////////////////////////////
//
//	/**
//	 * Convert the given metadata into a BlockState for this Block
//	 */
//	public BlockState getStateFromMeta(int meta) {
//		return this.getDefaultState().withProperty(AGE, Integer.valueOf(meta & 3));
//	}
//
//	/**
//	 * Convert the BlockState into the correct metadata value
//	 */
//	public int getMetaFromState(BlockState state) {
//		return state.getValue(AGE) & 3;
//	}
//
//	protected BlockStateContainer createBlockState() {
//		return new BlockStateContainer(this, new IProperty[] {AGE});
//	}
//
//	public BlockState getStateForAge(int age) {
//		return getDefaultState().withProperty(AGE, age);
//	}
//
//	public int getAgeForWorldGen(World world, BlockPos pos) {
//		return 3;
//	}
//
}
