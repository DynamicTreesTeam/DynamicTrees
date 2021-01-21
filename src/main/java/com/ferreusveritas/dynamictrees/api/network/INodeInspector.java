package com.ferreusveritas.dynamictrees.api.network;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public interface INodeInspector {

	public boolean run(BlockState blockState, IWorld world, BlockPos pos, Direction fromDir);

	public boolean returnRun(BlockState blockState, IWorld world, BlockPos pos, Direction fromDir);
}
