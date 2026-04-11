package com.dtteam.dynamictrees.platform;

import com.dtteam.dynamictrees.item.Seed;
import com.dtteam.dynamictrees.platform.services.IInteractionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.common.ItemAbilities;

public class NeoForgeInteractionHelper implements IInteractionHelper {

    @Override
    public boolean canToolAxeStrip(ItemStack stack) {
        return stack.canPerformAction(ItemAbilities.AXE_STRIP);
    }

    @Override
    public boolean canToolAxeDig(ItemStack stack) {
        return stack.canPerformAction(ItemAbilities.AXE_DIG);
    }

    /**
     * TODO: this can be done better now
     */
    @Override
    public int setSeedItemEntityLifespan(ItemEntity entityItem, Seed seed) {
        if (entityItem.lifespan == 6000) { // 6000 (5 minutes) is the default lifespan for an entity item
            entityItem.lifespan = seed.getTimeToLive(entityItem.getItem()) + 20; // override default lifespan with new value + 20 ticks (1 second)
            if (entityItem.lifespan == 6000) {
                entityItem.lifespan = 6001; // Ensure this isn't run again
            }
        }
        return entityItem.lifespan;
    }

    @Override
    public boolean blockDestroyByPlayer (BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluidState){
        return state.onDestroyedByPlayer(level, pos, player, willHarvest, fluidState);
    }

}