package com.ferreusveritas.dynamictrees.api.network;

import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;

import net.minecraft.block.Block;
import net.minecraft.world.World;

public interface INodeInspector {

	public boolean run(World world, Block block, BlockPos pos, EnumFacing fromDir);

	public boolean returnRun(World world, Block block, BlockPos pos, EnumFacing fromDir);
}
