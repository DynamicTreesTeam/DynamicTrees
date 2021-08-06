package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.seasons.SeasonHelper;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class BlockFruit extends Block implements IGrowable {

	enum MatureFruitAction {
		Nothing,
		Drop,
		Rot,
		Custom
	}

	protected static final AxisAlignedBB[] FRUIT_AABB = new AxisAlignedBB[]{
		new AxisAlignedBB(7 / 16.0, 1f, 7 / 16.0, 9 / 16.0, 15 / 16.0, 9 / 16.0),
		new AxisAlignedBB(7 / 16.0, 1f, 7 / 16.0, 9 / 16.0, 14 / 16.0, 9 / 16.0),
		new AxisAlignedBB(6 / 16.0, 1f, 6 / 16.0, 10 / 16.0, 12 / 16.0, 10 / 16.0),
		new AxisAlignedBB(6 / 16.0, 15 / 16.0, 6 / 16.0, 10 / 16.0, 11 / 16.0, 10 / 16.0)
	};

	public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 3);

	public static final String name = "fruit";

	private static final Map<Species, BlockFruit> speciesFruitMap = new HashMap<>();

	public static BlockFruit getFruitBlockForSpecies(Species species) {
		return speciesFruitMap.getOrDefault(species, null);
	}

	protected ItemStack droppedFruit = ItemStack.EMPTY;
	protected boolean bonemealable = false;//Q:Does dusting an apple with bone dust make it grow faster?  A:No.
	protected Species species = Species.NULLSPECIES;

	public BlockFruit() {
		this(name);
	}

	public BlockFruit(String name) {
		super(Material.PLANTS);
		setDefaultState(this.blockState.getBaseState().withProperty(AGE, 0));
		setRegistryName(name);
		setUnlocalizedName(name);
		setTickRandomly(true);
		setHardness(0.3f);
	}

	public BlockFruit setBonemealable(boolean bonemealable) {
		this.bonemealable = bonemealable;
		return this;
	}

	public void setSpecies(Species species) {
		speciesFruitMap.put(species, this);
		this.species = species;
	}

	public Species getSpecies() {
		return species;
	}

	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		if (!this.canBlockStay(world, pos, state)) {
			this.dropBlock(world, pos, state);
			return;
		}

		int age = state.getValue(AGE);
		Float season = SeasonHelper.getSeasonValue(world, pos);

		if (season != null && getSpecies() != null) { //Non-Null means we are season capable
			if (getSpecies().seasonalFruitProductionFactor(world, pos) < 0.2f) {
				outOfSeasonAction(world, pos);//Destroy the block or similar action
				return;
			}
			if (age == 0 && getSpecies().testFlowerSeasonHold(world, pos, season)) {
				return;//Keep fruit at the flower stage
			}
		}

		if (age < 3) {
			boolean doGrow = rand.nextFloat() < getGrowthChance(world, pos);
			boolean eventGrow = net.minecraftforge.common.ForgeHooks.onCropsGrowPre(world, pos, state, doGrow);
			if (season != null ? doGrow || eventGrow : eventGrow) { //Prevent a seasons mod from canceling the growth, we handle that ourselves
				world.setBlockState(pos, state.withProperty(AGE, age + 1), 2);
				net.minecraftforge.common.ForgeHooks.onCropsGrowPost(world, pos, state, world.getBlockState(pos));
			}
		} else {
			if (age == 3) {
				switch (matureAction(world, pos, state, rand)) {
					case Nothing:
						break;
					case Drop:
						dropBlock(world, pos, state);
						break;
					case Rot:
						world.setBlockToAir(pos);
						break;
					case Custom:
						break;
				}
			}
		}
	}

	protected float getGrowthChance(World world, BlockPos blockPos) {
		return 0.2f;
	}

	/**
	 * Override this to make the fruit do something once it's mature.
	 *
	 * @param world The world
	 * @param pos   The position of the fruit block
	 * @param state The current blockstate of the fruit
	 * @param rand  A random number generator
	 * @return MatureFruitAction action to take
	 */
	protected MatureFruitAction matureAction(World world, BlockPos pos, IBlockState state, Random rand) {
		return MatureFruitAction.Nothing;
	}

	protected void outOfSeasonAction(World world, BlockPos pos) {
		world.setBlockToAir(pos);
	}

	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		if (!this.canBlockStay(worldIn, pos, state)) {
			this.dropBlock(worldIn, pos, state);
		}
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (state.getValue(AGE) >= 3) {
			this.dropBlock(worldIn, pos, state);
			return true;
		}

		return false;
	}

	private void dropBlock(World worldIn, BlockPos pos, IBlockState state) {
		worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
		this.dropBlockAsItem(worldIn, pos, state, 0);
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return getFruitDrop();
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


	///////////////////////////////////////////
	//BONEMEAL
	///////////////////////////////////////////

	@Override
	public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
		return state.getValue(AGE) < 3;
	}

	@Override
	public boolean canUseBonemeal(World world, Random rand, BlockPos pos, IBlockState state) {
		return bonemealable;
	}

	@Override
	public void grow(World world, Random rand, BlockPos pos, IBlockState state) {
		int age = state.getValue(AGE);
		int newAge = MathHelper.clamp(age + 1, 0, 3);
		if (newAge != age) {
			world.setBlockState(pos, state.withProperty(AGE, newAge), 2);
		}
	}


	///////////////////////////////////////////
	//DROPS
	///////////////////////////////////////////

	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		super.getDrops(drops, world, pos, state, fortune);

		if (state.getValue(AGE) >= 3) {
			ItemStack toDrop = getFruitDrop();
			if (!toDrop.isEmpty()) {
				drops.add(toDrop);
			}
		}
	}

	public BlockFruit setDroppedItem(ItemStack stack) {
		droppedFruit = stack;
		return this;
	}

	//Override this for a custom item drop
	public ItemStack getFruitDrop() {
		return droppedFruit.copy();
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

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
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
		return new BlockStateContainer(this, AGE);
	}

	public IBlockState getStateForAge(int age) {
		return getDefaultState().withProperty(AGE, age);
	}

	public int getAgeForWorldGen(World world, BlockPos pos) {
		Float seasonValue = SeasonHelper.getSeasonValue(world, pos);
		return seasonValue != null ? getAgeForSeasonalWorldGen(world, pos, seasonValue) : 3;
	}

	public int getAgeForSeasonalWorldGen(World world, BlockPos pos, float seasonValue) {
		if (getSpecies().testFlowerSeasonHold(world, pos, seasonValue)) {
			return 0;//Fruit is as the flower stage
		}
		return Math.min(world.rand.nextInt(6), 3);//Half the time the fruit is fully mature
	}

}
