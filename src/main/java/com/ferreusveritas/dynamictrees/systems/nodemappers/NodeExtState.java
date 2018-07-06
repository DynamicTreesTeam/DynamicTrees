package com.ferreusveritas.dynamictrees.systems.nodemappers;

import java.util.HashMap;
import java.util.Map;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.INodeInspector;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;

/**
* Makes a BlockPos -> IExtendedBlockState map for all of the branches
* @author ferreusveritas
*/
public class NodeExtState implements INodeInspector {
	
	private final Map<BlockPos, IExtendedBlockState> map = new HashMap<>();
	private final BlockPos origin;
	
	public NodeExtState(BlockPos origin) {
		this.origin = origin;
	}
	
	public Map<BlockPos, IExtendedBlockState> getExtStateMap() {
		return map;
	}
	
	@Override
	public boolean run(IBlockState blockState, World world, BlockPos pos, EnumFacing fromDir) {
		BlockBranch branch = TreeHelper.getBranch(blockState);
		
		if(branch != null) {
			map.put(pos.subtract(origin), (IExtendedBlockState) blockState.getBlock().getExtendedState(blockState, world, pos));
		}
		
		return true;
	}
	
	@Override
	public boolean returnRun(IBlockState blockState, World world, BlockPos pos, EnumFacing fromDir) {
		return false;
	}
	
}
