package com.ferreusveritas.dynamictrees.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext.Builder;

//TODO: 1.14.4  Tile Entity is eliminated and each species has it's own sapling block state
//TODO: 1.14.4  Implement IPlantable

public class BlockDynamicSapling extends Block implements IGrowable {

	protected Species species;
	
	public BlockDynamicSapling(Species species) {
		super(Properties.create(Material.PLANTS).sound(SoundType.PLANT).tickRandomly());
		setRegistryName(species.getRegistryName().getPath() + "_sapling");
		this.species = species;
	}

	
	///////////////////////////////////////////
	// TREE INFORMATION
	///////////////////////////////////////////

	public Species getSpecies() {
		return species;
	}


	///////////////////////////////////////////
	// INTERACTION
	///////////////////////////////////////////


	@Override
	public void tick(BlockState state, World world, BlockPos pos, Random rand) {
		grow(world, rand, pos, state);
	}

	public static boolean canSaplingStay(IWorld world, Species species, BlockPos pos) {
		//Ensure there are no adjacent branches or other saplings
		for(Direction dir: CoordUtils.HORIZONTALS) {
			BlockState blockState = world.getBlockState(pos.offset(dir));
			Block block = blockState.getBlock();
			if(TreeHelper.isBranch(block) || block instanceof BlockDynamicSapling) {
				return false;
			}
		}

		//Air above and acceptable soil below
		return world.isAirBlock(pos.up()) && species.isAcceptableSoil(world, pos.down(), world.getBlockState(pos.down()));
	}

	public boolean canBlockStay(World world, BlockPos pos, BlockState state) {
		return canSaplingStay(world, getSpecies(), pos);
	}

	@Override
	public void grow(World world, Random rand, BlockPos pos, BlockState state) {
		if(canBlockStay(world, pos, state)) {
			getSpecies().transitionToTree(world, pos);
		} else {
			dropBlock(world, state, pos);
		}
	}

	@Override
	public SoundType getSoundType(BlockState state, IWorldReader world, BlockPos pos, @Nullable Entity entity) {
		return getSpecies().getSaplingSound();
	}

	///////////////////////////////////////////
	// DROPS
	///////////////////////////////////////////


	@Override
	public void neighborChanged(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		if (!canBlockStay(world, pos, state)) {
			dropBlock(world, state, pos);
		}
	}

	protected void dropBlock(World world, BlockState state, BlockPos pos) {
		world.addEntity(new ItemEntity(world, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, getSpecies().getSeedStack(1)));
		world.removeBlock(pos, false);
	}

	@Override
	public ItemStack getItem(IBlockReader worldIn, BlockPos pos, BlockState state) {
		return getSpecies().getSeedStack(1);
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, Builder builder) {
		//TODO: Deal with 1.14's new loot drop system.  For now just return a fresh array.
		List<ItemStack> drops = new ArrayList<>();
		drops.add(getSpecies().getSeedStack(1));
		return drops;
	}
	
	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
		return getSpecies().getSeedStack(1);
	}


	///////////////////////////////////////////
	// PHYSICAL BOUNDS
	///////////////////////////////////////////


	@Override
	public VoxelShape getShape(BlockState state, IBlockReader access, BlockPos pos, ISelectionContext context) {
		return getSpecies().getSaplingShape();
	}

	///////////////////////////////////////////
	// RENDERING
	///////////////////////////////////////////

//	@Override
//	public boolean isFullCube(BlockState state) {
//		return false;
//	}
//
//	@Override
//	public boolean isOpaqueCube(BlockState state) {
//		return false;
//	}
//
//	@Override
//	public BlockFaceShape getBlockFaceShape(IBlockReader worldIn, BlockState state, BlockPos pos, Direction face) {
//		return BlockFaceShape.UNDEFINED;//This prevents fences and walls from attempting to connect to saplings.
//	}
//
//	@Override
//	@OnlyIn(Dist.CLIENT)
//	public BlockRenderLayer getBlockLayer() {
//		return BlockRenderLayer.CUTOUT_MIPPED;
//	}


	@Override
	public boolean canGrow(IBlockReader world, BlockPos pos, BlockState state, boolean isClient) {
		return getSpecies().canGrowWithBoneMeal((World) world, pos);
	}

	@Override
	public boolean canUseBonemeal(World world, Random rand, BlockPos pos, BlockState state) {
		return getSpecies().canUseBoneMealNow(world, rand, pos);
	}
	
}
