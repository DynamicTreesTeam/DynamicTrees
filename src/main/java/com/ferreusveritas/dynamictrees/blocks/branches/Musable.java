package com.ferreusveritas.dynamictrees.blocks.branches;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

@FunctionalInterface
public interface Musable {
    boolean isMusable(BlockGetter world, BlockState state, BlockPos pos);
}
