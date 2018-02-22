package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.api.cells.CellNull;
import com.ferreusveritas.dynamictrees.api.cells.ICell;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
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
	public ICell getHydrationCell(IBlockAccess blockAccess, BlockPos pos, IBlockState blockState, EnumFacing dir, ILeavesProperties leavesTree) {
		return CellNull.NULLCELL;
	}

	@Override
	public GrowSignal growSignal(World world, BlockPos pos, GrowSignal signal) {
		return signal;
	}

	@Override
	public int getRadiusForConnection(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, BlockBranch from, EnumFacing side, int fromRadius) {
		//Twigs connect to Vanilla leaves
		if(fromRadius == 1) {
			return from.getTree().isCompatibleVanillaLeaves(blockState, blockAccess, pos) ? 1: 0;
		}
		return 0;
	}

	@Override
	public int probabilityForBlock(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, BlockBranch from) {
		return blockState.getBlock().isAir(blockState, blockAccess, pos) ? 1 : 0;
	}

	@Override
	public int getRadius(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos) {
		return 0;
	}

	@Override
	public MapSignal analyse(World world, BlockPos pos, EnumFacing fromDir, MapSignal signal) {
		return signal;
	}
	
	@Override
	public int branchSupport(IBlockState blockState, IBlockAccess blockAccess, BlockBranch branch, BlockPos pos, EnumFacing dir, int radius) {
		return BlockBranch.setSupport(0, branch.getTree().isCompatibleVanillaLeaves(blockState, blockAccess, pos) ? 1 : 0);
	}

	@Override
	public DynamicTree getTree(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos) {
		return DynamicTree.NULLTREE;
	}

	public final TreePartType getTreePartType() {
		return TreePartType.NULL;
	}
	
}
