package com.ferreusveritas.dynamictrees.api.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public interface NodeInspector {

    boolean run(BlockState state, LevelAccessor level, BlockPos pos, Direction fromDir);

    boolean returnRun(BlockState state, LevelAccessor level, BlockPos pos, Direction fromDir);

}
