package com.ferreusveritas.dynamictrees.inspectors;

import java.util.Random;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.TreeHelper;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class NodeTwinkle implements INodeInspector {

	String particleName;
	int numParticles;

	public NodeTwinkle(String name, int num) {
		particleName = name;
		numParticles = num;
	}

	@Override
	public boolean run(World world, Block block, int x, int y, int z, ForgeDirection fromDir) {
		if(world.isRemote && TreeHelper.isBranch(block)) {
			spawnParticles(world, particleName, x, y + 1, z, numParticles, world.rand);
		}
		return false;
	}

	@Override
	public boolean returnRun(World world, Block block, int x, int y, int z, ForgeDirection fromDir) {
		return false;
	}

	public static void spawnParticles(World world, String particleName, int x, int y, int z, int numParticles, Random random) {
		for (int i1 = 0; i1 < numParticles; ++i1) {
			double mx = random.nextGaussian() * 0.02D;
			double my = random.nextGaussian() * 0.02D;
			double mz = random.nextGaussian() * 0.02D;
			DynamicTrees.proxy.spawnParticle(world, particleName, x + random.nextFloat(), (double)y + (double)random.nextFloat(), (double)z + random.nextFloat(), mx, my, mz);
		}
	}

}
