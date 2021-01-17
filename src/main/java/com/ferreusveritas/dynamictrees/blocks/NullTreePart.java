package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.systems.*;
import com.ferreusveritas.dynamictrees.api.cells.CellNull;
import com.ferreusveritas.dynamictrees.api.cells.ICell;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class NullTreePart implements ITreePart {
	
	//This is a safe dump for blocks that aren't tree parts
	//Handles some vanilla blocks
	
	@Override
	public ICell getHydrationCell(IBlockReader blockAccess, BlockPos pos, BlockState blockState, Direction dir, ILeavesProperties leavesTree) {
		return CellNull.NULLCELL;
	}

	@Override
	public GrowSignal growSignal(World world, BlockPos pos, GrowSignal signal) {
		return signal;
	}

	@Override
	public int getRadiusForConnection(BlockState blockState, IBlockReader blockAccess, BlockPos pos, BranchBlock from, Direction side, int fromRadius) {
		//Twigs connect to Vanilla leaves
		if(fromRadius == 1) {
			return from.getFamily().isCompatibleVanillaLeaves(blockState, blockAccess, pos) ? 1: 0;
		}
		return 0;
	}
	
	@Override
	public int probabilityForBlock(BlockState blockState, IBlockReader blockAccess, BlockPos pos, BranchBlock from) {
		return blockState.getBlock().isAir(blockState, blockAccess, pos) ? 1 : 0;
	}
	
	@Override
	public int getRadius(BlockState blockState) {
		return 0;
	}
	
	@Override
	public boolean shouldAnalyse() {
		return false;
	}
	
	@Override
	public MapSignal analyse(BlockState blockState, World world, BlockPos pos, Direction fromDir, MapSignal signal) {
		return signal;
	}
	
	@Override
	public int branchSupport(BlockState blockState, IBlockReader blockAccess, BranchBlock branch, BlockPos pos, Direction dir, int radius) {
//		return BlockBranch.setSupport(0, branch.getFamily().isCompatibleVanillaLeaves(blockState, blockAccess, pos) ? 1 : 0);
		return 0;
	}
	
	@Override
	public TreeFamily getFamily(BlockState blockState, IBlockReader blockAccess, BlockPos pos) {
		return TreeFamily.NULLFAMILY;
	}
	
	public final TreePartType getTreePartType() {
		return TreePartType.NULL;
	}
	
}
