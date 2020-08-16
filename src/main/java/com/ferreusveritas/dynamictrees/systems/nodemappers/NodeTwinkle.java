package com.ferreusveritas.dynamictrees.systems.nodemappers;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.INodeInspector;
import net.minecraft.block.BlockState;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class NodeTwinkle implements INodeInspector {
	
	ParticleTypes particleType;
	int numParticles;
	
	public NodeTwinkle(ParticleTypes type, int num) {
		particleType = type;
		numParticles = num;
	}
	
	@Override
	public boolean run(BlockState blockState, World world, BlockPos pos, Direction fromDir) {
		if(world.isRemote && TreeHelper.isBranch(blockState)) {
			spawnParticles(world, particleType, pos.getX(), pos.getY() + 1, pos.getZ(), numParticles, world.rand);
		}
		return false;
	}
	
	@Override
	public boolean returnRun(BlockState blockState, World world, BlockPos pos, Direction fromDir) {
		return false;
	}
	
	public static void spawnParticles(World world, ParticleTypes particleType, int x, int y, int z, int numParticles, Random random) {
//		for (int i1 = 0; i1 < numParticles; ++i1) {
//			double mx = random.nextGaussian() * 0.02D;
//			double my = random.nextGaussian() * 0.02D;
//			double mz = random.nextGaussian() * 0.02D;
//			DynamicTrees.proxy.spawnParticle(world, particleType, x + random.nextFloat(), (double)y + (double)random.nextFloat(), (double)z + random.nextFloat(), mx, my, mz);
//		}
	}
	
}
