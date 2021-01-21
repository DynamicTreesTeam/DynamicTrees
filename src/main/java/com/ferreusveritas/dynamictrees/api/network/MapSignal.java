package com.ferreusveritas.dynamictrees.api.network;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import java.util.ArrayList;

public class MapSignal {

	protected ArrayList<INodeInspector> nodeInspectors;

	public BlockPos root;
	public int depth;

	public Direction localRootDir;

	public boolean overflow;
	public boolean found;

	public MapSignal() {
		localRootDir = null;
		nodeInspectors = new ArrayList<INodeInspector>();
	}

	public MapSignal(INodeInspector ... nis) {
		this();

		for(INodeInspector ni: nis) {
			nodeInspectors.add(ni);
		}
	}

	public boolean run(BlockState blockState, IWorld world, BlockPos pos, Direction fromDir) {
		for(INodeInspector inspector: nodeInspectors) {
			inspector.run(blockState, world, pos, fromDir);
		}
		return false;
	}

	public boolean returnRun(BlockState blockState, IWorld world, BlockPos pos, Direction fromDir) {
		for(INodeInspector inspector: nodeInspectors) {
			inspector.returnRun(blockState, world, pos, fromDir);
		}
		return false;
	}

	public ArrayList<INodeInspector> getInspectors() {
		return nodeInspectors;
	}
	
}
