package com.ferreusveritas.dynamictrees.api;

import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

@FunctionalInterface
public interface Ageable {

    /**
     * @param world The world
     * @param pos   the position of this block that is being aged
     * @param state the state of this block
     * @param rand  random number generator
     * @return -1 if block was destroyed after the ageing, otherwise the hydro value of the block
     */
    int age(LevelAccessor world, BlockPos pos, BlockState state, Random rand, SafeChunkBounds safeBounds);

}
