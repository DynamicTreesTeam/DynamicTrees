package com.dtteam.dynamictrees.platform;

import com.dtteam.dynamictrees.item.Seed;
import com.dtteam.dynamictrees.platform.services.IInteractionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class FabricInteractionHelper implements IInteractionHelper {

    //Fabric doesn't have any implementation of tool abilities
    //so a simple axe check will have to do.
    @Override
    public boolean canToolAxeStrip(ItemStack stack) {
        return stack.is(ItemTags.AXES);
    }

    @Override
    public boolean canToolAxeDig(ItemStack stack) {
        return stack.is(ItemTags.AXES);
    }

    @Override
    public int setSeedItemEntityLifespan(ItemEntity entityItem, Seed seed) {
        return 0;
    }

    @Override
    public boolean blockDestroyByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluidState) {
        return false;
    }
}
