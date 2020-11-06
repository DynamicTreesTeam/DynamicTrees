package com.ferreusveritas.dynamictrees.systems.nodemappers;

import java.util.Random;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.INodeInspector;
import com.ferreusveritas.dynamictrees.init.DTClient;

import net.minecraft.block.BlockState;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NodeTwinkle implements INodeInspector {
	
	BasicParticleType particleType;
	int numParticles;
	
	public NodeTwinkle(BasicParticleType type, int num) {
		particleType = type;
		numParticles = num;
	}
	
	@Override
	public boolean run(BlockState blockState, World world, BlockPos pos, Direction fromDir) {
		if(world.isRemote && TreeHelper.isBranch(blockState)) {
			spawnParticles(world, particleType, pos.getX(), pos.getY(), pos.getZ(), numParticles, world.rand);
		}
		return false;
	}
	
	@Override
	public boolean returnRun(BlockState blockState, World world, BlockPos pos, Direction fromDir) {
		return false;
	}
	
	public static void spawnParticles(World world, BasicParticleType particleType, int x, int y, int z, int numParticles, Random random) {
		for (int i1 = 0; i1 < numParticles; ++i1) {
			double mx = random.nextGaussian() * 0.02D;
			double my = random.nextGaussian() * 0.02D;
			double mz = random.nextGaussian() * 0.02D;
			DTClient.spawnParticle(world, particleType, x + random.nextFloat(), (double)y + (double)random.nextFloat(), (double)z + random.nextFloat(), mx, my, mz);
		}
	}
	
}
