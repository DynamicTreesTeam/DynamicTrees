package com.ferreusveritas.dynamictrees.api;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IFutureBreakable {

    void futureBreak(BlockState state, World world, BlockPos pos, LivingEntity player);

}
