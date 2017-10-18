package com.ferreusveritas.dynamictrees.api.network;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public interface INodeInspector {

	public boolean run(World world, Block block, BlockPos pos, EnumFacing fromDir);

	public boolean returnRun(World world, Block block, BlockPos pos, EnumFacing fromDir);
}
