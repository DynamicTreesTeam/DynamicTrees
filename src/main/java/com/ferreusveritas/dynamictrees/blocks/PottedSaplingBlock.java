package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.tileentity.PottedSaplingTileEntity;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.BlockStates;
import com.ferreusveritas.dynamictrees.util.ItemUtils;
import com.ferreusveritas.dynamictrees.util.Null;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.pathfinding.PathType;
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

@SuppressWarnings("deprecation")
public class PottedSaplingBlock extends ContainerBlock {

    public static final ResourceLocation REG_NAME = DynamicTrees.resLoc("potted_sapling");

    protected static final AxisAlignedBB FLOWER_POT_AABB = new AxisAlignedBB(0.3125D, 0.0D, 0.3125D, 0.6875D, 0.375D, 0.6875D);

    public PottedSaplingBlock() {
        super(Block.Properties.of(Material.DECORATION).instabreak().noOcclusion());
    }

    //////////////////////////////
    // Properties
    //////////////////////////////

    public Species getSpecies(IBlockReader world, BlockPos pos) {
        return Null.applyIfNonnull(this.getTileEntityPottedSapling(world, pos),
                PottedSaplingTileEntity::getSpecies, Species.NULL_SPECIES);
    }

    public boolean setSpecies(World world, BlockPos pos, BlockState state, Species species) {
        return Null.consumeIfNonnull(this.getTileEntityPottedSapling(world, pos),
                pottedSaplingTileEntity -> pottedSaplingTileEntity.setSpecies(species));
    }

    public BlockState getPotState(World world, BlockPos pos) {
        return Null.applyIfNonnull(this.getTileEntityPottedSapling(world, pos),
                PottedSaplingTileEntity::getPot, Blocks.FLOWER_POT.defaultBlockState());
    }

    public boolean setPotState(World world, BlockState potState, BlockPos pos) {
        return Null.consumeIfNonnull(this.getTileEntityPottedSapling(world, pos),
                pottedSaplingTileEntity -> pottedSaplingTileEntity.setPot(potState));
    }


    ///////////////////////////////////////////
    // TILE ENTITY
    ///////////////////////////////////////////

    @Nullable
    private PottedSaplingTileEntity getTileEntityPottedSapling(IBlockReader world, BlockPos pos) {
        final TileEntity tileEntity = world.getBlockEntity(pos);
        return tileEntity instanceof PottedSaplingTileEntity ? (PottedSaplingTileEntity) tileEntity : null;
    }

    @Override
    public TileEntity newBlockEntity(IBlockReader worldIn) {
        return new PottedSaplingTileEntity();
    }


    ///////////////////////////////////////////
    // INTERACTION
    ///////////////////////////////////////////

    // Unlike a regular flower pot this is only used to eject the contents.
    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        final ItemStack heldItem = player.getItemInHand(hand);
        final Species species = this.getSpecies(world, pos);

        if (!species.isValid()) {
            return ActionResultType.FAIL;
        }

        final ItemStack seedStack = species.getSeedStack(1);

        // If they are holding the seed do not empty the pot.
        if (heldItem.getItem() == seedStack.getItem() || (hand == Hand.OFF_HAND &&
                player.getItemInHand(Hand.MAIN_HAND).getItem() == seedStack.getItem())) {
            return ActionResultType.PASS;
        }

        if (heldItem.isEmpty()) {
            // If they're holding nothing, put it in their hand.
            player.setItemInHand(hand, seedStack);
            // Otherwise try to add it to their inventory.
        } else if (!player.addItem(seedStack)) {
            // If their inventory is full, drop it instead.
            player.drop(seedStack, false);
        }

        // Set the block back to the original pot state.
        world.setBlock(pos, this.getPotState(world, pos), 3);

        return ActionResultType.sidedSuccess(world.isClientSide);
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {

        if (target.getType() == RayTraceResult.Type.BLOCK && ((BlockRayTraceResult) target).getDirection() == Direction.UP) {
            final Species species = this.getSpecies(world, pos);
            if (species.isValid()) {
                return species.getSeedStack(1);
            }
        }

        final BlockState potState = Null.applyIfNonnull(this.getTileEntityPottedSapling(world, pos),
                PottedSaplingTileEntity::getPot, BlockStates.AIR);

        if (potState.getBlock() == Blocks.FLOWER_POT) {
            return new ItemStack(Items.FLOWER_POT);
        }

        if (potState.getBlock() instanceof FlowerPotBlock) {
            return new ItemStack(potState.getBlock(), 1);
        }

        return new ItemStack(Items.FLOWER_POT);
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!world.getBlockState(pos.below()).isFaceSturdy(world, pos, Direction.UP)) {
            this.spawnDrops(world, pos);
            world.setBlockAndUpdate(pos, BlockStates.AIR);
        }
    }

    @Override
    public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, FluidState fluid) {
        if (willHarvest) {
            return true; // If it will harvest, delay deletion of the block until after getDrops.
        }

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

    @Override
    public boolean isPathfindable(BlockState state, IBlockReader world, BlockPos pos, PathType pathType) {
        return false;
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
