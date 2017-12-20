package com.ferreusveritas.dynamictrees.inspectors;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.INodeInspector;

import net.minecraft.block.Block;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NodeNetVolume implements INodeInspector {

	private int volume;//number of voxels(1x1x1 pixels) of wood accumulated from network analysis

	@Override
	public boolean run(World world, Block block, BlockPos pos, EnumFacing fromDir) {
		int radius = TreeHelper.getSafeTreePart(block).getRadius(world, pos);
		volume += radius * radius * 64;//Integrate volume of this tree part into the total volume calculation
		return true;
	}

	@Override
	public boolean returnRun(World world, Block block, BlockPos pos, EnumFacing fromDir) {
		return false;
	}

	public int getVolume() {
		return volume;
	}

}
