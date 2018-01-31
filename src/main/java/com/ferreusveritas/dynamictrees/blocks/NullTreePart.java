package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.api.cells.CellNull;
import com.ferreusveritas.dynamictrees.api.cells.ICell;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class NullTreePart implements ITreePart {

	//This is a safe dump for blocks that aren't tree parts
	//Handles some vanilla blocks

	@Override
	public ICell getHydrationCell(IBlockAccess blockAccess, BlockPos pos, IBlockState blockState, EnumFacing dir, DynamicTree leavesTree) {
		return CellNull.nullCell;
	}

	@Override
	public GrowSignal growSignal(World world, BlockPos pos, GrowSignal signal) {
		return signal;
	}

	@Override
	public int getRadiusForConnection(IBlockAccess blockAccess, BlockPos pos, BlockBranch from, int fromRadius) {
		//Twigs connect to Vanilla leaves
		if(fromRadius == 1) {
			IBlockState blockState = blockAccess.getBlockState(pos);
			IBlockState primState = from.getTree().getPrimitiveLeaves();
			if(blockState.getBlock() == primState.getBlock()) {
				//Ignore "no decay" and "check decay" flags and only compare leaves type.
				if((blockState.getBlock().getMetaFromState(blockState) & 3) == (primState.getBlock().getMetaFromState(primState) & 3)) {
					return 1;
				}
			}
		}
		return 0;
	}

	@Override
	public int probabilityForBlock(IBlockAccess blockAccess, BlockPos pos, BlockBranch from) {
		return blockAccess.isAirBlock(pos) ? 1 : 0;
	}

	@Override
	public int getRadius(IBlockAccess blockAccess, BlockPos pos) {
		return 0;
	}

	@Override
	public MapSignal analyse(World world, BlockPos pos, EnumFacing fromDir, MapSignal signal) {
		return signal;
	}

	@Override
	public boolean isRootNode() {
		return false;
	}

	@Override
	public boolean isBranch() {
		return false;
	}
	
	@Override
	public int branchSupport(IBlockAccess blockAccess, BlockBranch branch, BlockPos pos, EnumFacing dir, int radius) {
		IBlockState blockState = blockAccess.getBlockState(pos);
		IBlockState primState = branch.getTree().getPrimitiveLeaves();
		
		if(blockState.getBlock() == primState.getBlock()) {//Vanilla leaves can be used for support
			if ( (primState.getBlock().getMetaFromState(primState) & 3) == (blockState.getBlock().getMetaFromState(blockState) & 3) ) {//Compare meta
				return 0x01;
			}
		}
		return 0;
	}

	@Override
	public DynamicTree getTree(IBlockAccess blockAccess, BlockPos pos) {
		return null;
	}

}
