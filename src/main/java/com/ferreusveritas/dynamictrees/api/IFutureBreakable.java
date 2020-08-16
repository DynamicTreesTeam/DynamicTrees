package com.ferreusveritas.dynamictrees.api;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IFutureBreakable {
	
	public void futureBreak(BlockState state, World world, BlockPos pos, LivingEntity player);

}
