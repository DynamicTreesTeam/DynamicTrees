package com.ferreusveritas.dynamictrees.api;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IFutureBreakable {
	
	public void futureBreak(IBlockState state, World world, BlockPos pos, EntityPlayer player);
	
}
