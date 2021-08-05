package com.ferreusveritas.dynamictrees.api;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IFutureBreakable {

	void futureBreak(IBlockState state, World world, BlockPos pos, EntityLivingBase player);

}
