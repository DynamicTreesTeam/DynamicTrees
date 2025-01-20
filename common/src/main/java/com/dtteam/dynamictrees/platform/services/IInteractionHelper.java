package com.dtteam.dynamictrees.platform.services;

import com.dtteam.dynamictrees.item.Seed;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public interface IInteractionHelper {

    boolean canToolAxeStrip(ItemStack stack);
    boolean canToolAxeDig (ItemStack stack);
    int setSeedItemEntityLifespan (ItemEntity entityItem, Seed seed);
    boolean blockDestroyByPlayer (BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluidState);

}
