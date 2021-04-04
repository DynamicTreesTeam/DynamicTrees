package com.ferreusveritas.dynamictrees.systems.nodemappers;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.INodeInspector;
import com.ferreusveritas.dynamictrees.init.DTClient;
import net.minecraft.block.BlockState;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public class TwinkleNode implements INodeInspector {
	
	private final BasicParticleType particleType;
	private final int numParticles;
	
	public TwinkleNode(BasicParticleType type, int num) {
		particleType = type;
		numParticles = num;
	}
	
	@Override
	public boolean run(BlockState blockState, IWorld world, BlockPos pos, Direction fromDir) {
		if(world.isClientSide() && TreeHelper.isBranch(blockState)) {
			DTClient.spawnParticles(world, this.particleType, pos.getX(), pos.getY(), pos.getZ(), this.numParticles, world.getRandom());
		}
		return false;
	}
	
	@Override
	public boolean returnRun(BlockState blockState, IWorld world, BlockPos pos, Direction fromDir) {
		return false;
	}
	
}
