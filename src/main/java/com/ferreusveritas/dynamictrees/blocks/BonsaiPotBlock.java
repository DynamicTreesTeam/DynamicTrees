package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.tileentity.BonsaiTileEntity;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CommandUtils;
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

public class BonsaiPotBlock extends ContainerBlock {

	public static final String name = "bonsai_pot";
	protected static final AxisAlignedBB FLOWER_POT_AABB = new AxisAlignedBB(0.3125D, 0.0D, 0.3125D, 0.6875D, 0.375D, 0.6875D);
	
	public BonsaiPotBlock() {
		this(name);
	}
	
	public BonsaiPotBlock(String name) {
		super(Block.Properties.create(Material.MISCELLANEOUS).hardnessAndResistance(0));
		this.setRegistryName(name);
	}

	//////////////////////////////
	// Properties
	//////////////////////////////

	public Species getSpecies(IBlockReader world, BlockPos pos) {
		BonsaiTileEntity bonsaiPotTE = this.getTileEntityBonsai(world, pos);
		return bonsaiPotTE != null ? bonsaiPotTE.getSpecies() : Species.NULL_SPECIES;
	}

	public boolean setSpecies(World world, BlockPos pos, BlockState state, Species species) {
		BonsaiTileEntity bonsaiPotTE = this.getTileEntityBonsai(world, pos);
		if(bonsaiPotTE != null) {
			bonsaiPotTE.setSpecies(species);
			return true;
		}
		return false;
	}

	public BlockState getPotState(World world, BlockPos pos) {
		BonsaiTileEntity bonsaiPotTE = this.getTileEntityBonsai(world, pos);
		return bonsaiPotTE != null ? bonsaiPotTE.getPot() : Blocks.FLOWER_POT.getDefaultState();
	}

	public boolean setPotState(World world, BlockState potState, BlockPos pos) {
		BonsaiTileEntity bonsaiPotTE = getTileEntityBonsai(world, pos);
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
	private BonsaiTileEntity getTileEntityBonsai (IBlockReader world, BlockPos pos) {
		TileEntity tileEntity = world.getTileEntity(pos);
		return tileEntity instanceof BonsaiTileEntity ? (BonsaiTileEntity) tileEntity : null;
	}

	@Override
	public TileEntity createNewTileEntity(IBlockReader worldIn) {
		return new BonsaiTileEntity();
	}


	///////////////////////////////////////////
	// INTERACTION
	///////////////////////////////////////////

	// Unlike a regular flower pot this is only used to eject the contents
	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
		ItemStack heldItem = player.getHeldItem(hand);

		if (hand == Hand.MAIN_HAND && heldItem.getItem() == Items.AIR) { // Empty hand
			Species species = this.getSpecies(world, pos);

			if (!species.isValid())
				return ActionResultType.PASS;

			if(!world.isRemote) {
				ItemStack seedStack = species.getSeedStack(1);
				world.addEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), seedStack));
			}

			world.setBlockState(pos, getPotState(world, pos)); // Return back to an empty pot

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

		BlockState potState = this.getTileEntityBonsai(world, pos).getPot();

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
		if (!world.getBlockState(pos.down()).isSolidSide(world, pos, Direction.UP)) {
			this.spawnDrops(world, pos);
			world.setBlockState(pos, Blocks.AIR.getDefaultState());
		}
	}

	@Override
	public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, FluidState fluid) {
		if (willHarvest) return true; //If it will harvest, delay deletion of the block until after getDrops
		return super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
	}

	@Override
	public void harvestBlock(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable TileEntity te, ItemStack stack) {
		super.harvestBlock(world, player, pos, state, te, stack);
		this.spawnDrops(world, pos);
		world.setBlockState(pos, Blocks.AIR.getDefaultState());
	}

	public void spawnDrops(World world, BlockPos pos) {
		CommandUtils.spawnItemStack(world, pos, new ItemStack(Blocks.FLOWER_POT), false);
		if (this.getSpecies(world, pos) != Species.NULL_SPECIES) { // Safety check in case for whatever reason the species was not set.
			CommandUtils.spawnItemStack(world, pos, this.getSpecies(world, pos).getSeedStack(1), false);
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
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

}
