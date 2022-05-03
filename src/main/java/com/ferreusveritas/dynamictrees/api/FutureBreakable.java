package com.ferreusveritas.dynamictrees.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@FunctionalInterface
public interface FutureBreakable {

    void futureBreak(BlockState state, Level world, BlockPos pos, LivingEntity player);

}
