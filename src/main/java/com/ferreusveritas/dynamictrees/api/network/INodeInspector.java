package com.ferreusveritas.dynamictrees.api.network;

import net.minecraft.block.Block;
import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;
import com.ferreusveritas.dynamictrees.api.backport.WorldDec;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;

public interface INodeInspector {

	public boolean run(WorldDec world, Block block, BlockPos pos, EnumFacing fromDir);

	public boolean returnRun(WorldDec world, Block block, BlockPos pos, EnumFacing fromDir);
}
