package com.ferreusveritas.dynamictrees.systems.nodemappers;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.INodeInspector;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.util.BranchConnectionData;
import com.ferreusveritas.dynamictrees.util.Connections;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

/**
 * Makes a BlockPos -> BlockState map for all of the branches
 * @author ferreusveritas
 */
public class NodeExtState implements INodeInspector {
	
	private final Map<BlockPos, BranchConnectionData> map = new HashMap<>();
	private final BlockPos origin;
	
	public NodeExtState(BlockPos origin) {
		this.origin = origin;
	}
	
	public Map<BlockPos, BranchConnectionData> getBranchConnectionMap() {
		return map;
	}
	
	@Override
	public boolean run(BlockState blockState, IWorld world, BlockPos pos, Direction fromDir) {
		BranchBlock branch = TreeHelper.getBranch(blockState);
		
		if(branch != null) {
			Connections connData = branch.getConnectionData(world, pos, blockState);
			map.put(pos.subtract(origin), new BranchConnectionData(blockState, connData));
		}
		
		return true;
	}
	
	@Override
	public boolean returnRun(BlockState blockState, IWorld world, BlockPos pos, Direction fromDir) {
		return false;
	}
	
}
