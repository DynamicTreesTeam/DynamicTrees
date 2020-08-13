package com.ferreusveritas.dynamictrees.systems.nodemappers;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.INodeInspector;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

/**
* Makes a BlockPos -> BlockState map for all of the branches
* @author ferreusveritas
*/
public class NodeExtState implements INodeInspector {
	
//	private final Map<BlockPos, ExtendedBlockState> map = new HashMap<>();
	private final BlockPos origin;

	public NodeExtState(BlockPos origin) {
		this.origin = origin;
	}

//	public Map<BlockPos, BlockState> getExtStateMap() {
//		return map;
//	}

	@Override
	public boolean run(BlockState blockState, World world, BlockPos pos, Direction fromDir) {
		BlockBranch branch = TreeHelper.getBranch(blockState);

//		if(branch != null) {
//			map.put(pos.subtract(origin), (BlockState) blockState.getBlock().getExtendedState(blockState, world, pos));
//		}

		return true;
	}

	@Override
	public boolean returnRun(BlockState blockState, World world, BlockPos pos, Direction fromDir) {
		return false;
	}

}
