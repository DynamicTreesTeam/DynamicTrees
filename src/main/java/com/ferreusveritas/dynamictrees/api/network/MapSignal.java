package com.ferreusveritas.dynamictrees.api.network;

import com.ferreusveritas.dynamictrees.systems.nodemappers.NodeCollector;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;

public class MapSignal {

	protected ArrayList<INodeInspector> nodeInspectors;

	public BlockPos root = null;
	public int depth;
	public boolean multiroot = false;
	public boolean destroyLoopedNodes = true;
	public boolean trackVisited = false;

	public EnumFacing localRootDir;

	public boolean overflow;
	public boolean found;

	public MapSignal() {
		localRootDir = null;
		nodeInspectors = new ArrayList<INodeInspector>();
	}

	public MapSignal(INodeInspector... nis) {
		this();

		for (INodeInspector ni : nis) {
			nodeInspectors.add(ni);
		}
	}

	public boolean run(IBlockState blockState, World world, BlockPos pos, EnumFacing fromDir) {
		for (INodeInspector inspector : nodeInspectors) {
			inspector.run(blockState, world, pos, fromDir);
		}
		return false;
	}

	public boolean returnRun(IBlockState blockState, World world, BlockPos pos, EnumFacing fromDir) {
		for (INodeInspector inspector : nodeInspectors) {
			inspector.returnRun(blockState, world, pos, fromDir);
		}
		return false;
	}

	public ArrayList<INodeInspector> getInspectors() {
		return nodeInspectors;
	}

	public boolean doTrackingVisited(BlockPos pos) {
		if (nodeInspectors.size() > 0) {
			INodeInspector inspector = nodeInspectors.get(0);
			if (inspector instanceof NodeCollector) {
				return ((NodeCollector) inspector).contains(pos);
			}
		}
		return false;
	}

}
