package com.ferreusveritas.dynamictrees.systems.nodemappers;

import com.ferreusveritas.dynamictrees.api.network.INodeInspector;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Set;

public class NodeCollector implements INodeInspector {

	private final Set<BlockPos> nodeSet;

	public NodeCollector(Set nodeSet) {
		this.nodeSet = nodeSet;
	}

	@Override
	public boolean run(IBlockState blockState, World world, BlockPos pos, EnumFacing fromDir) {
		nodeSet.add(pos);
		return false;
	}

	@Override
	public boolean returnRun(IBlockState blockState, World world, BlockPos pos, EnumFacing fromDir) {
		return false;
	}

	public boolean contains(BlockPos pos) {
		return nodeSet.contains(pos);
	}
}
