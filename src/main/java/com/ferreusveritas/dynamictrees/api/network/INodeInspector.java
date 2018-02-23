package com.ferreusveritas.dynamictrees.api.network;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface INodeInspector {

	public boolean run(IBlockState blockState, World world, BlockPos pos, EnumFacing fromDir);

	public boolean returnRun(IBlockState blockState, World world, BlockPos pos, EnumFacing fromDir);
}
