package com.ferreusveritas.dynamictrees.api;

import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

@FunctionalInterface
public interface Ageable {

    /**
     * @param level The level
     * @param pos   the position of this block that is being aged
     * @param state the state of this block
     * @param rand  random number generator
     * @return -1 if block was destroyed after the ageing, otherwise the hydro value of the block
     */
    int age(LevelAccessor level, BlockPos pos, BlockState state, RandomSource rand, SafeChunkBounds safeBounds);

}
