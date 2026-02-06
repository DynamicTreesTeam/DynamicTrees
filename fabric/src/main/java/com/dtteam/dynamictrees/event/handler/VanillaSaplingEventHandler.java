package com.dtteam.dynamictrees.event.handler;

import com.dtteam.dynamictrees.block.sapling.DynamicSaplingBlock;
import com.dtteam.dynamictrees.config.DTConfigs;
import com.dtteam.dynamictrees.tree.species.Species;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;

public class VanillaSaplingEventHandler {

    private static boolean isEnabled = false;

    public static void register() {
        UseBlockCallback.EVENT.register(VanillaSaplingEventHandler::onUseBlock);
    }

    public static void updateEnabled() {
        isEnabled = DTConfigs.COMMON.replaceVanillaSaplings.get();
    }

    private static InteractionResult onUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (!isEnabled || level.isClientSide()) {
            return InteractionResult.PASS;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem blockItem)) {
            return InteractionResult.PASS;
        }

        Block block = blockItem.getBlock();
        if (!DynamicSaplingBlock.SAPLING_REPLACERS.containsKey(block)) {
            return InteractionResult.PASS;
        }

        BlockPos placePos = hitResult.getBlockPos().relative(hitResult.getDirection());

        if (!level.getBlockState(placePos).canBeReplaced()) {
            return InteractionResult.PASS;
        }

        Species targetSpecies = DynamicSaplingBlock.SAPLING_REPLACERS.get(block);
        Species species = targetSpecies.selfOrLocationOverride(level, placePos);

        if (species.plantSapling(level, placePos, targetSpecies != species)) {
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
