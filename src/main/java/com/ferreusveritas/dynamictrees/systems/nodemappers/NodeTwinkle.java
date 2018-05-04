package com.ferreusveritas.dynamictrees.systems.nodemappers;

import java.util.Random;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.INodeInspector;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NodeTwinkle implements INodeInspector {
	
	EnumParticleTypes particleType;
	int numParticles;
	
	public NodeTwinkle(EnumParticleTypes type, int num) {
		particleType = type;
		numParticles = num;
	}
	
	@Override
	public boolean run(IBlockState blockState, World world, BlockPos pos, EnumFacing fromDir) {
		if(world.isRemote && TreeHelper.isBranch(blockState)) {
			spawnParticles(world, particleType, pos.getX(), pos.getY() + 1, pos.getZ(), numParticles, world.rand);
		}
		return false;
	}
	
	@Override
	public boolean returnRun(IBlockState blockState, World world, BlockPos pos, EnumFacing fromDir) {
		return false;
	}
	
	public static void spawnParticles(World world, EnumParticleTypes particleType, int x, int y, int z, int numParticles, Random random) {
		for (int i1 = 0; i1 < numParticles; ++i1) {
			double mx = random.nextGaussian() * 0.02D;
			double my = random.nextGaussian() * 0.02D;
			double mz = random.nextGaussian() * 0.02D;
			DynamicTrees.proxy.spawnParticle(world, particleType, x + random.nextFloat(), (double)y + (double)random.nextFloat(), (double)z + random.nextFloat(), mx, my, mz);
		}
	}
	
}
