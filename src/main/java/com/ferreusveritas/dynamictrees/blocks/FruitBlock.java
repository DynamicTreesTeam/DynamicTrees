package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.compat.seasons.SeasonHelper;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class FruitBlock extends Block implements IGrowable {

	enum MatureFruitAction {
		NOTHING,
		DROP,
		ROT,
		CUSTOM
	}

	protected static final AxisAlignedBB[] FRUIT_AABB = new AxisAlignedBB[] {
			new AxisAlignedBB(7/16.0, 1f, 7/16.0, 9/16.0, 15/16.0, 9/16.0),
			new AxisAlignedBB(7/16.0, 1f, 7/16.0, 9/16.0, 14/16.0, 9/16.0),
			new AxisAlignedBB(6/16.0, 1f, 6/16.0, 10/16.0, 12/16.0, 10/16.0),
			new AxisAlignedBB(6/16.0, 15/16.0, 6/16.0, 10/16.0, 11/16.0, 10/16.0)
	};
	
	public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 3);
	
	public static final String name = "apple_fruit";

	private static Map<Species, FruitBlock> speciesFruitMap = new HashMap<>();

	public static FruitBlock getFruitBlockForSpecies(Species species) {
		return speciesFruitMap.getOrDefault(species, null);
	}

	protected ItemStack droppedFruit = ItemStack.EMPTY;
	protected boolean bonemealable = false;//Q:Does dusting an apple with bone dust make it grow faster?  A:No.
	protected Vector3d itemSpawnOffset = new Vector3d(0.5, 0.6, 0.5);
	protected Species species = Species.NULL_SPECIES;

	public FruitBlock() {
		this(name);
	}

	public FruitBlock(String name) {
		super(Properties.create(Material.PLANTS)
				.tickRandomly()
				.hardnessAndResistance(0.3f));
		setRegistryName(name);
	}

	public FruitBlock setBonemealable(boolean bonemealable) {
		this.bonemealable = bonemealable;
		return this;
	}

	public void setItemSpawnOffset (float x, float y, float z){
		this.itemSpawnOffset = new Vector3d(Math.min(Math.max(x,0),1),Math.min(Math.max(y,0),1),Math.min(Math.max(z,0),1));
	}

	public void setSpecies(Species species) {
		speciesFruitMap.put(species, this);
		this.species = species;
	}

	public Species getSpecies() {
		return species;
	}

	@Override
	public void tick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
		if (!this.canBlockStay(world, pos, state)) {
			this.dropBlock(world, pos);
			return;
		}

		int age = state.get(AGE);
		Float season = SeasonHelper.getSeasonValue(world, pos);

		if(season != null && getSpecies() != null) { //Non-Null means we are season capable
			if(getSpecies().seasonalFruitProductionFactor(world, pos) < 0.2f) {
				outOfSeasonAction(world, pos);//Destroy the block or similar action
				return;
			}
			if(age == 0 && getSpecies().testFlowerSeasonHold(season)) {
				return;//Keep fruit at the flower stage
			}
		}

		if (age < 3) {
			boolean doGrow = rand.nextFloat() < getGrowthChance(world, pos);
			boolean eventGrow = net.minecraftforge.common.ForgeHooks.onCropsGrowPre(world, pos, state, doGrow);
			if(season != null ? doGrow || eventGrow : eventGrow) { //Prevent a seasons mod from canceling the growth, we handle that ourselves
				world.setBlockState(pos, state.with(AGE, age + 1), 2);
				net.minecraftforge.common.ForgeHooks.onCropsGrowPost(world, pos, state);
			}
		} else {
			if (age == 3) {
				switch(matureAction(world, pos, state, rand)) {
					case NOTHING: break;
					case DROP: this.dropBlock(world, pos); break;
					case ROT: world.setBlockState(pos, DTRegistries.blockStates.air); break;
					case CUSTOM: break;
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
	 * @param pos The position of the fruit block
	 * @param state The current blockstate of the fruit
	 * @param rand A random number generator
	 * @return MatureFruitAction action to take
	 */
	protected MatureFruitAction matureAction(World world, BlockPos pos, BlockState state, Random rand) {
		return MatureFruitAction.NOTHING;
	}

	protected void outOfSeasonAction(World world, BlockPos pos) {
		world.setBlockState(pos, DTRegistries.blockStates.air);
	}

	@Override
	public void onNeighborChange(BlockState state, IWorldReader world, BlockPos pos, BlockPos neighbor) {
		if (!this.canBlockStay(world, pos, state)) {
			this.dropBlock((World)world, pos);
		}
	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		if (state.get(AGE) >= 3 ) {
			this.dropBlock(worldIn, pos);
			return ActionResultType.SUCCESS;
		}

		return ActionResultType.FAIL;
	}

	private void dropBlock(World worldIn, BlockPos pos) {
		worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
		worldIn.addEntity(new ItemEntity(worldIn, pos.getX() + itemSpawnOffset.x, pos.getY() + itemSpawnOffset.y, pos.getZ() + itemSpawnOffset.z, droppedFruit));
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
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
	public boolean canBlockStay(IBlockReader world, BlockPos pos, BlockState state) {
		return world.getBlockState(pos.up()).getBlock() instanceof LeavesBlock;
	}


	///////////////////////////////////////////
	//BONEMEAL
	///////////////////////////////////////////

	@Override
	public boolean canGrow(IBlockReader worldIn, BlockPos pos, BlockState state, boolean isClient) {
		return state.get(AGE) < 3;
	}

	@Override
	public boolean canUseBonemeal(World world, Random rand, BlockPos pos, BlockState state) {
		return bonemealable;
	}

	@Override
	public void grow(ServerWorld world, Random rand, BlockPos pos, BlockState state) {
		int age = state.get(AGE);
		int newAge = MathHelper.clamp(age + 1, 0, 3);
		if(newAge != age) {
			world.setBlockState(pos, state.with(AGE, newAge), 2);
		}
	}


	///////////////////////////////////////////
	//DROPS
	///////////////////////////////////////////


	@Override
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
		List<ItemStack> drops = super.getDrops(state, builder);
		if(state.get(AGE) >= 3) {
			ItemStack toDrop = getFruitDrop();
			if(!toDrop.isEmpty()) {
				drops.add(toDrop);
			}
		}
		return drops;
	}

	public FruitBlock setDroppedItem(ItemStack stack) {
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

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return VoxelShapes.create(FRUIT_AABB[state.get(AGE)]);
	}

	///////////////////////////////////////////
	// BLOCKSTATE
	///////////////////////////////////////////

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(AGE);
	}

	public BlockState getStateForAge(int age) {
		return getDefaultState().with(AGE, age);
	}

	public int getAgeForWorldGen(World world, BlockPos pos) {
		Float seasonValue = SeasonHelper.getSeasonValue(world, pos);
		return seasonValue != null ? this.getAgeForSeasonalWorldGen(world, pos, seasonValue) : 3;
	}

	public int getAgeForSeasonalWorldGen(IWorld world, BlockPos pos, float seasonValue) {
		if (this.getSpecies().testFlowerSeasonHold(seasonValue)) {
			return 0;//Fruit is as the flower stage
		}
		return Math.min(world.getRandom().nextInt(6), 3);//Half the time the fruit is fully mature
	}

}
