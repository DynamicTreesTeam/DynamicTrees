package com.ferreusveritas.dynamictrees.block.branch;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

@FunctionalInterface
public interface Musable {
    boolean isMusable(BlockGetter level, BlockState state, BlockPos pos);
}
