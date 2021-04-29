package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.tileentity.PottedSaplingTileEntity;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.ItemUtils;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class PottedSaplingBlock extends ContainerBlock {

	public static final ResourceLocation REG_NAME = DynamicTrees.resLoc("potted_sapling");

	protected static final AxisAlignedBB FLOWER_POT_AABB = new AxisAlignedBB(0.3125D, 0.0D, 0.3125D, 0.6875D, 0.375D, 0.6875D);

	public PottedSaplingBlock() {
		super(Block.Properties.of(Material.DECORATION).strength(0));
	}

	//////////////////////////////
	// Properties
	//////////////////////////////

	public Species getSpecies(IBlockReader world, BlockPos pos) {
		PottedSaplingTileEntity bonsaiPotTE = this.getTileEntityPottedSapling(world, pos);
		return bonsaiPotTE != null ? bonsaiPotTE.getSpecies() : Species.NULL_SPECIES;
	}

	public boolean setSpecies(World world, BlockPos pos, BlockState state, Species species) {
		PottedSaplingTileEntity bonsaiPotTE = this.getTileEntityPottedSapling(world, pos);
		if(bonsaiPotTE != null) {
			bonsaiPotTE.setSpecies(species);
			return true;
		}
		return false;
	}

	public BlockState getPotState(World world, BlockPos pos) {
		PottedSaplingTileEntity bonsaiPotTE = this.getTileEntityPottedSapling(world, pos);
		return bonsaiPotTE != null ? bonsaiPotTE.getPot() : Blocks.FLOWER_POT.defaultBlockState();
	}

	public boolean setPotState(World world, BlockState potState, BlockPos pos) {
		PottedSaplingTileEntity bonsaiPotTE = getTileEntityPottedSapling(world, pos);
		if(bonsaiPotTE != null) {
			bonsaiPotTE.setPot(potState);
			return true;
		}
		return false;
	}
	
	
	///////////////////////////////////////////
	// TILE ENTITY
	///////////////////////////////////////////

	@Nullable
	private PottedSaplingTileEntity getTileEntityPottedSapling(IBlockReader world, BlockPos pos) {
		TileEntity tileEntity = world.getBlockEntity(pos);
		return tileEntity instanceof PottedSaplingTileEntity ? (PottedSaplingTileEntity) tileEntity : null;
	}

	@Override
	public TileEntity newBlockEntity(IBlockReader worldIn) {
		return new PottedSaplingTileEntity();
	}


	///////////////////////////////////////////
	// INTERACTION
	///////////////////////////////////////////

	// Unlike a regular flower pot this is only used to eject the contents
	@Override
	public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
		ItemStack heldItem = player.getItemInHand(hand);

		if (hand == Hand.MAIN_HAND && heldItem.getItem() == Items.AIR) { // Empty hand
			Species species = this.getSpecies(world, pos);

			if (!species.isValid())
				return ActionResultType.PASS;

			if(!world.isClientSide) {
				ItemStack seedStack = species.getSeedStack(1);
				world.addFreshEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), seedStack));
			}

			world.setBlockAndUpdate(pos, getPotState(world, pos)); // Return back to an empty pot

			return ActionResultType.SUCCESS;
		}

		return ActionResultType.PASS;
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {

//		if(target.sideHit == Direction.UP) {
//			Species species = getSpecies(world, pos);
//			if(species != Species.NULLSPECIES) {
//				return species.getSeedStack(1);
//			}
//		}

		BlockState potState = this.getTileEntityPottedSapling(world, pos).getPot();

		if(potState.getBlock() == Blocks.FLOWER_POT) {
			return new ItemStack(Items.FLOWER_POT);
		}

		if(potState.getBlock() instanceof FlowerPotBlock) {
			return new ItemStack(potState.getBlock(), 1);
		}

		return new ItemStack(Items.FLOWER_POT);
	}

	@Override
	public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
		if (!world.getBlockState(pos.below()).isFaceSturdy(world, pos, Direction.UP)) {
			this.spawnDrops(world, pos);
			world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
		}
	}

	@Override
	public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, FluidState fluid) {
		if (willHarvest) return true; //If it will harvest, delay deletion of the block until after getDrops
		return super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
	}

	@Override
	public void playerDestroy(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable TileEntity te, ItemStack stack) {
		super.playerDestroy(world, player, pos, state, te, stack);
		this.spawnDrops(world, pos);
		world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
	}

	public void spawnDrops(World world, BlockPos pos) {
		ItemUtils.spawnItemStack(world, pos, new ItemStack(Blocks.FLOWER_POT), false);
		if (this.getSpecies(world, pos) != Species.NULL_SPECIES) { // Safety check in case for whatever reason the species was not set.
			ItemUtils.spawnItemStack(world, pos, this.getSpecies(world, pos).getSeedStack(1), false);
		}
	}


	///////////////////////////////////////////
	// PHYSICAL BOUNDS
	///////////////////////////////////////////

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return VoxelShapes.create(FLOWER_POT_AABB);
	}


	///////////////////////////////////////////
	// RENDERING
	///////////////////////////////////////////

	@Override
	public BlockRenderType getRenderShape(BlockState state) {
		return BlockRenderType.MODEL;
	}

}
