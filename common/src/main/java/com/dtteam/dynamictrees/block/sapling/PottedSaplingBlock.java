package com.dtteam.dynamictrees.block.sapling;

import com.dtteam.dynamictrees.platform.Services;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.utility.helper.ItemUtils;
import com.dtteam.dynamictrees.utility.helper.NullHelper;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class PottedSaplingBlock extends BaseEntityBlock {

    protected static final AABB FLOWER_POT_AABB = new AABB(0.3125D, 0.0D, 0.3125D, 0.6875D, 0.375D, 0.6875D);

    public PottedSaplingBlock() {
        super(Properties.of().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY));
    }

    //////////////////////////////
    // Properties
    //////////////////////////////

    public Species getSpecies(BlockGetter level, BlockPos pos) {
        return NullHelper.applyIfNonnull(this.getTileEntityPottedSapling(level, pos),
                PottedSaplingBlockEntity::getSpecies, Species.NULL_SPECIES);
    }

    public boolean setSpecies(Level level, BlockPos pos, BlockState state, Species species) {
        return NullHelper.consumeIfNonnull(this.getTileEntityPottedSapling(level, pos),
                pottedSaplingBlockEntity -> pottedSaplingBlockEntity.setSpecies(species));
    }

    public BlockState getPotState(Level level, BlockPos pos) {
        return NullHelper.applyIfNonnull(this.getTileEntityPottedSapling(level, pos),
                PottedSaplingBlockEntity::getPot, Blocks.FLOWER_POT.defaultBlockState());
    }

    public boolean setPotState(Level level, BlockState potState, BlockPos pos) {
        return NullHelper.consumeIfNonnull(this.getTileEntityPottedSapling(level, pos),
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

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return Services.REGISTRY.newPottedSaplingBlockEntity(pPos,pState);
    }


    ///////////////////////////////////////////
    // INTERACTION
    ///////////////////////////////////////////

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        final Species species = this.getSpecies(level, pos);
        if (!species.isValid()) return ItemInteractionResult.FAIL;

        removeSaplingFromPot(stack, species, player, level, pos);

        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        final Species species = this.getSpecies(level, pos);
        if (!species.isValid()) return InteractionResult.FAIL;

        removeSaplingFromPot(ItemStack.EMPTY, species, player, level, pos);

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    // Unlike a regular flower pot this is only used to eject the contents.
    private boolean removeSaplingFromPot(ItemStack heldStack, Species species, Player player, Level level, BlockPos pos){
        final ItemStack seedStack = species.getSeedStack(1);

        if (heldStack.isEmpty()){
            player.setItemInHand(InteractionHand.MAIN_HAND, seedStack);
        } else {
            // If they are holding the seed do not empty the pot.
            if (heldStack.getItem() == seedStack.getItem()) return false;

            if (!player.addItem(seedStack)) {
                // If their inventory is full, drop it instead.
                player.drop(seedStack, false);
            }
        }

        // Set the block back to the original pot state.
        level.setBlock(pos, this.getPotState(level, pos), 3);
        return true;
    }

    /**
     * Worse implementation for Fabric, as there's no HitResult
     */
    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        final Species species = this.getSpecies(level, pos);
        if (species.isValid()) {
            return species.getSeedStack(1);
        }
        return new ItemStack(Items.FLOWER_POT);
    }

    /** NeoForge Override */
    @SuppressWarnings("unused")
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        if (target.getType() == HitResult.Type.BLOCK && ((BlockHitResult) target).getDirection() == Direction.UP) {
            final Species species = this.getSpecies(level, pos);
            if (species.isValid()) {
                return species.getSeedStack(1);
            }
        }

        final BlockState potState = NullHelper.applyIfNonnull(this.getTileEntityPottedSapling(level, pos),
                PottedSaplingBlockEntity::getPot, Blocks.AIR.defaultBlockState());

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
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }
    }

    /** NeoForge Override */
    @SuppressWarnings("unused")
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid){
        if (willHarvest) {
            return true; // If it will harvest, delay deletion of the block until after getDrops.
        }

        return level.isClientSide() ? level.setBlock(pos, fluid.createLegacyBlock(), 11) : level.removeBlock(pos, false);
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
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
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
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

}
