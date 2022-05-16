package com.ferreusveritas.dynamictrees.items;

import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;

public class DirtBucket extends Item {

    public DirtBucket() {
        super(new Item.Properties().stacksTo(1).tab(DTRegistries.ITEM_GROUP));
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getContainerItem(ItemStack itemStack) {
        return itemStack.copy();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {

        final ItemStack itemStack = player.getItemInHand(hand);
        final BlockHitResult blockRayTraceResult;

        {
            blockRayTraceResult = getPlayerPOVHitResult(world, player, ClipContext.Fluid.NONE);
            if (blockRayTraceResult.getType() != HitResult.Type.BLOCK) {
                return new InteractionResultHolder<>(InteractionResult.FAIL, itemStack);
            }
        }

        if (DTConfigs.DIRT_BUCKET_PLACES_DIRT.get()) {
            if (blockRayTraceResult.getType() != HitResult.Type.BLOCK) {
                return new InteractionResultHolder<>(InteractionResult.PASS, itemStack);
            } else {
                final BlockPos pos = blockRayTraceResult.getBlockPos();

                if (!world.mayInteract(player, pos)) {
                    return new InteractionResultHolder<>(InteractionResult.FAIL, itemStack);
                } else {
                    final boolean isReplaceable = world.getBlockState(pos).getMaterial().isReplaceable();
                    final BlockPos workingPos = isReplaceable && blockRayTraceResult.getDirection() == Direction.UP ? pos : pos.relative(blockRayTraceResult.getDirection());

                    if (!player.mayUseItemAt(workingPos, blockRayTraceResult.getDirection(), itemStack)) {
                        return new InteractionResultHolder<>(InteractionResult.FAIL, itemStack);
                    } else if (this.tryPlaceContainedDirt(player, world, workingPos)) {
                        player.awardStat(Stats.ITEM_USED.get(this));
                        return !player.getAbilities().instabuild ? new InteractionResultHolder<>(InteractionResult.SUCCESS, new ItemStack(Items.BUCKET)) : new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStack);
                    } else {
                        return new InteractionResultHolder<>(InteractionResult.FAIL, itemStack);
                    }
                }
            }
        } else {
            return new InteractionResultHolder<>(InteractionResult.PASS, itemStack);
        }
    }

    public boolean tryPlaceContainedDirt(@Nullable Player player, Level world, BlockPos posIn) {
        BlockState blockState = world.getBlockState(posIn);
        if (blockState.getMaterial().isReplaceable()) {
            if (!world.isClientSide && !blockState.isAir()) {
                world.destroyBlock(posIn, true);
            }

            world.playSound(player, posIn, SoundEvents.GRAVEL_PLACE, SoundSource.BLOCKS, 1.0F, 0.8F);
            world.setBlock(posIn, Blocks.DIRT.defaultBlockState(), 11);
            return true;
        }

        return false;
    }

}
