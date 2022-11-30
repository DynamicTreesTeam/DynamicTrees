package com.ferreusveritas.dynamictrees.block;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.block.entity.PottedSaplingBlockEntity;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.ferreusveritas.dynamictrees.util.BlockStates;
import com.ferreusveritas.dynamictrees.util.ItemUtils;
import com.ferreusveritas.dynamictrees.util.Null;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

@SuppressWarnings("deprecation")
public class PottedSaplingBlock extends BaseEntityBlock {

    public static final ResourceLocation REG_NAME = DynamicTrees.location("potted_sapling");

    protected static final AABB FLOWER_POT_AABB = new AABB(0.3125D, 0.0D, 0.3125D, 0.6875D, 0.375D, 0.6875D);

    public PottedSaplingBlock() {
        super(Block.Properties.of(Material.DECORATION).instabreak().noOcclusion());
    }

    //////////////////////////////
    // Properties
    //////////////////////////////

    public Species getSpecies(BlockGetter level, BlockPos pos) {
        return Null.applyIfNonnull(this.getTileEntityPottedSapling(level, pos),
                PottedSaplingBlockEntity::getSpecies, Species.NULL_SPECIES);
    }

    public boolean setSpecies(Level level, BlockPos pos, BlockState state, Species species) {
        return Null.consumeIfNonnull(this.getTileEntityPottedSapling(level, pos),
                pottedSaplingBlockEntity -> pottedSaplingBlockEntity.setSpecies(species));
    }

    public BlockState getPotState(Level level, BlockPos pos) {
        return Null.applyIfNonnull(this.getTileEntityPottedSapling(level, pos),
                PottedSaplingBlockEntity::getPot, Blocks.FLOWER_POT.defaultBlockState());
    }

    public boolean setPotState(Level level, BlockState potState, BlockPos pos) {
        return Null.consumeIfNonnull(this.getTileEntityPottedSapling(level, pos),
                pottedSaplingBlockEntity -> pottedSaplingBlockEntity.setPot(potState));
    }


    ///////////////////////////////////////////
    // TILE ENTITY
    ///////////////////////////////////////////

    @Nullable
    private PottedSaplingBlockEntity getTileEntityPottedSapling(BlockGetter level, BlockPos pos) {
        final BlockEntity tileEntity = level.getBlockEntity(pos);
        return tileEntity instanceof PottedSaplingBlockEntity ? (PottedSaplingBlockEntity) tileEntity : null;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new PottedSaplingBlockEntity(pPos,pState);
    }


    ///////////////////////////////////////////
    // INTERACTION
    ///////////////////////////////////////////

    // Unlike a regular flower pot this is only used to eject the contents.
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        final ItemStack heldItem = player.getItemInHand(hand);
        final Species species = this.getSpecies(level, pos);

        if (!species.isValid()) {
            return InteractionResult.FAIL;
        }

        final ItemStack seedStack = species.getSeedStack(1);

        // If they are holding the seed do not empty the pot.
        if (heldItem.getItem() == seedStack.getItem() || (hand == InteractionHand.OFF_HAND &&
                player.getItemInHand(InteractionHand.MAIN_HAND).getItem() == seedStack.getItem())) {
            return InteractionResult.PASS;
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
        level.setBlock(pos, this.getPotState(level, pos), 3);

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {

        if (target.getType() == HitResult.Type.BLOCK && ((BlockHitResult) target).getDirection() == Direction.UP) {
            final Species species = this.getSpecies(level, pos);
            if (species.isValid()) {
                return species.getSeedStack(1);
            }
        }

        final BlockState potState = Null.applyIfNonnull(this.getTileEntityPottedSapling(level, pos),
                PottedSaplingBlockEntity::getPot, BlockStates.AIR);

        if (potState.getBlock() == Blocks.FLOWER_POT) {
            return new ItemStack(Items.FLOWER_POT);
        }

        if (potState.getBlock() instanceof FlowerPotBlock) {
            return new ItemStack(potState.getBlock(), 1);
        }

        return new ItemStack(Items.FLOWER_POT);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.getBlockState(pos.below()).isFaceSturdy(level, pos, Direction.UP)) {
            this.spawnDrops(level, pos);
            level.setBlockAndUpdate(pos, BlockStates.AIR);
        }
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (willHarvest) {
            return true; // If it will harvest, delay deletion of the block until after getDrops.
        }

        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity te, ItemStack stack) {
        super.playerDestroy(level, player, pos, state, te, stack);
        this.spawnDrops(level, pos);
        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
    }

    public void spawnDrops(Level level, BlockPos pos) {
        ItemUtils.spawnItemStack(level, pos, new ItemStack(Blocks.FLOWER_POT), false);
        if (this.getSpecies(level, pos) != Species.NULL_SPECIES) { // Safety check in case for whatever reason the species was not set.
            ItemUtils.spawnItemStack(level, pos, this.getSpecies(level, pos).getSeedStack(1), false);
        }
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType pathType) {
        return false;
    }

    ///////////////////////////////////////////
    // PHYSICAL BOUNDS
    ///////////////////////////////////////////

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.create(FLOWER_POT_AABB);
    }


    ///////////////////////////////////////////
    // RENDERING
    ///////////////////////////////////////////

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

}
