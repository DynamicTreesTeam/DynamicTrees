package com.ferreusveritas.dynamictrees.api.network;

import net.minecraft.block.Block;

import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;
import com.ferreusveritas.dynamictrees.api.backport.World;

public interface INodeInspector {

	public boolean run(World world, Block block, BlockPos pos, EnumFacing fromDir);

	public boolean returnRun(World world, Block block, BlockPos pos, EnumFacing fromDir);
}
