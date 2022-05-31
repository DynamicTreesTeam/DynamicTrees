package com.ferreusveritas.dynamictrees.api.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public interface NodeInspector {

    boolean run(BlockState blockState, LevelAccessor world, BlockPos pos, Direction fromDir);

    boolean returnRun(BlockState blockState, LevelAccessor world, BlockPos pos, Direction fromDir);

}
