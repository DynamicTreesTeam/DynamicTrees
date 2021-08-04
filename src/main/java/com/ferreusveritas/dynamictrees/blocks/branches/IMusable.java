package com.ferreusveritas.dynamictrees.blocks.branches;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

public interface IMusable {
    boolean isMusable(IBlockReader world, BlockState state, BlockPos pos);
}
