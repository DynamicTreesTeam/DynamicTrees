package com.dtteam.dynamictrees.item;

import com.dtteam.dynamictrees.config.DTConfigs;
import com.dtteam.dynamictrees.registry.DTRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

public class DirtBucket extends Item {

    public DirtBucket(Identifier id) {
        super(new Properties().stacksTo(1).setId(ResourceKey.create(Registries.ITEM, id)));
        DTRegistries.CREATIVE_TAB_ITEMS.add(this);
    }

    @Override
    public @Nullable ItemStackTemplate getCraftingRemainder() {
        if (craftingRemainingItem == null) craftingRemainingItem = new ItemStackTemplate(this);
        return super.getCraftingRemainder();
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        final ItemStack itemStack = player.getItemInHand(hand);
        final BlockHitResult blockRayTraceResult;

        {
            blockRayTraceResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
            if (blockRayTraceResult.getType() != HitResult.Type.BLOCK) {
                return InteractionResult.FAIL;
            }
        }

        if (DTConfigs.SERVER.dirtBucketPlacesDirt.get()) {
            if (blockRayTraceResult.getType() != HitResult.Type.BLOCK) {
                return InteractionResult.PASS;
            } else {
                final BlockPos pos = blockRayTraceResult.getBlockPos();

                if (!level.mayInteract(player, pos)) {
                    return InteractionResult.FAIL;
                } else {
                    final boolean isReplaceable = level.getBlockState(pos).canBeReplaced();
                    final BlockPos workingPos = isReplaceable && blockRayTraceResult.getDirection() == Direction.UP ? pos : pos.relative(blockRayTraceResult.getDirection());

                    if (!player.mayUseItemAt(workingPos, blockRayTraceResult.getDirection(), itemStack)) {
                        return InteractionResult.FAIL;
                    } else if (this.tryPlaceContainedDirt(player, level, workingPos)) {
                        player.awardStat(Stats.ITEM_USED.get(this));
                        return InteractionResult.SUCCESS;
                    } else {
                        return InteractionResult.FAIL;
                    }
                }
            }
        } else {
            return InteractionResult.PASS;
        }
    }

    public boolean tryPlaceContainedDirt(@Nullable Player player, Level world, BlockPos posIn) {
        BlockState blockState = world.getBlockState(posIn);
        if (blockState.canBeReplaced()) {
            if (!world.isClientSide() && !blockState.isAir()) {
                world.destroyBlock(posIn, true);
            }

            world.playSound(player, posIn, SoundEvents.GRAVEL_PLACE, SoundSource.BLOCKS, 1.0F, 0.8F);
            world.setBlock(posIn, Blocks.DIRT.defaultBlockState(), 11);
            return true;
        }

        return false;
    }

}