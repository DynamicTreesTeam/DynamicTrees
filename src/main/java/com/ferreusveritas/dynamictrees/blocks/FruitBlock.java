package com.ferreusveritas.dynamictrees.blocks;

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

import java.util.List;
import java.util.Random;

public class FruitBlock extends Block implements IGrowable {
	
	protected static final AxisAlignedBB[] FRUIT_AABB = new AxisAlignedBB[] {
			new AxisAlignedBB(7/16.0, 1f, 7/16.0, 9/16.0, 15/16.0, 9/16.0),
			new AxisAlignedBB(7/16.0, 1f, 7/16.0, 9/16.0, 14/16.0, 9/16.0),
			new AxisAlignedBB(6/16.0, 1f, 6/16.0, 10/16.0, 12/16.0, 10/16.0),
			new AxisAlignedBB(6/16.0, 15/16.0, 6/16.0, 10/16.0, 11/16.0, 10/16.0)
	};
	
	public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 3);
	
	public static final String name = "fruitapple";
	public Vector3d itemSpawnOffset = new Vector3d(0.5, 0.6, 0.5);

	protected ItemStack droppedFruit = ItemStack.EMPTY;
	protected boolean bonemealable = false;//Q:Does dusting an apple with bone dust make it grow faster?  A:No.
	
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
		itemSpawnOffset = new Vector3d(Math.min(Math.max(x,0),1),Math.min(Math.max(y,0),1),Math.min(Math.max(z,0),1));
	}

	@Override
	public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
		if (!this.canBlockStay(worldIn, pos, state)) {
			this.dropBlock(worldIn, pos);
		}
		else {
			int age = state.get(AGE);

			if (age < 3 && net.minecraftforge.common.ForgeHooks.onCropsGrowPre(worldIn, pos, state, rand.nextInt(5) == 0)) {
				worldIn.setBlockState(pos, state.with(AGE, age + 1), 2);
				net.minecraftforge.common.ForgeHooks.onCropsGrowPost(worldIn, pos, state);
			} else
			if (age == 3) {
				if(matureAction(worldIn, pos, state, rand)) {
					dropBlock(worldIn, pos);
				}
			}
		}
	}

	/**
	 * Override this to make the fruit do something once it's mature.
	 *
	 * @param world The world
	 * @param pos The position of the fruit block
	 * @param state The current blockstate of the fruit
	 * @param rand A random number generator
	 * @return true to drop the block. false will keep the fruit intact
	 */
	protected boolean matureAction(World world, BlockPos pos, BlockState state, Random rand) {
		return false;
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

//	@Override
//	public BlockFaceShape getBlockFaceShape(IBlockReader worldIn, BlockState state, BlockPos pos, Direction face) {
//		return BlockFaceShape.UNDEFINED;
//	}

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

	public int getAgeForWorldGen(IWorld world, BlockPos pos) {
		return 3;
	}

}
