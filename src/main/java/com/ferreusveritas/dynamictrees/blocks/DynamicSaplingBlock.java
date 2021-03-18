package com.ferreusveritas.dynamictrees.blocks;

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
import net.minecraft.loot.LootContext;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.IPlantable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DynamicSaplingBlock extends Block implements IGrowable, IPlantable {
	
	protected Species species;
	
	public DynamicSaplingBlock(Species species) {
		super(Properties.create(Material.PLANTS).sound(SoundType.PLANT).tickRandomly());
		this.species = species;
	}

	
	///////////////////////////////////////////
	// TREE INFORMATION
	///////////////////////////////////////////
	
	public Species getSpecies() {
		return species;
	}
	
	@Override
	public boolean canGrow(@Nonnull IBlockReader world, @Nonnull BlockPos pos, @Nonnull BlockState state, boolean isClient) {
		return getSpecies().canSaplingConsumeBoneMeal((World) world, pos);
	}
	
	@Override
	public boolean canUseBonemeal(@Nonnull World world, @Nonnull Random rand, @Nonnull BlockPos pos, @Nonnull BlockState state) {
		return getSpecies().canSaplingGrowAfterBoneMeal(world, rand, pos);
	}
	
	///////////////////////////////////////////
	// INTERACTION
	///////////////////////////////////////////

	@Override
	public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
		return getSpecies().saplingFireSpread();
	}

	@Override
	public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
		return getSpecies().saplingFlammability();
	}

	@Override
	public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
		if (getSpecies().canSaplingGrowNaturally(worldIn, pos))
			this.grow(worldIn, rand, pos, state);
	}

	public static boolean canSaplingStay(IWorld world, Species species, BlockPos pos) {
		//Ensure there are no adjacent branches or other saplings
		for(Direction dir: CoordUtils.HORIZONTALS) {
			BlockState blockState = world.getBlockState(pos.offset(dir));
			Block block = blockState.getBlock();
			if(TreeHelper.isBranch(block) || block instanceof DynamicSaplingBlock) {
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
	public void grow(@Nonnull ServerWorld world, @Nonnull Random rand, @Nonnull BlockPos pos, @Nonnull BlockState state) {
		if (canBlockStay(world, pos, state)) {
			if (getSpecies().canSaplingGrow(world, pos)){
				getSpecies().transitionToTree(world, pos);
			}
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
	
	@Nonnull
	@Override
	public ItemStack getItem(IBlockReader worldIn, BlockPos pos, BlockState state) {
		return getSpecies().getSeedStack(1);
	}
	
	@Nonnull
	@Override
	public List<ItemStack> getDrops(@Nonnull BlockState state, @Nonnull LootContext.Builder builder) {
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
	
	@Nonnull
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader access, BlockPos pos, ISelectionContext context) {
		return getSpecies().getSaplingShape();
	}
	
	///////////////////////////////////////////
	// RENDERING
	///////////////////////////////////////////

	@Override
	public BlockState getPlant(IBlockReader world, BlockPos pos) {
		return species.getSapling().get().getDefaultState();
	}
	
}
