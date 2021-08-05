package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.api.cells.CellNull;
import com.ferreusveritas.dynamictrees.api.cells.ICell;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class NullTreePart implements ITreePart {

	//This is a safe dump for blocks that aren't tree parts
	//Handles some vanilla blocks

	@Override
	public ICell getHydrationCell(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing dir, ILeavesProperties leavesTree) {
		return CellNull.NULLCELL;
	}

	@Override
	public GrowSignal growSignal(World world, BlockPos pos, GrowSignal signal) {
		return signal;
	}

	@Override
	public int getRadiusForConnection(IBlockState state, IBlockAccess blockAccess, BlockPos pos, BlockBranch from, EnumFacing side, int fromRadius) {
		//Twigs connect to Vanilla leaves
		if (fromRadius == 1) {
			return from.getFamily().isCompatibleVanillaLeaves(state, blockAccess, pos) ? 1 : 0;
		}
		return 0;
	}

	@Override
	public int probabilityForBlock(IBlockState state, IBlockAccess world, BlockPos pos, BlockBranch from) {
		return state.getBlock().isAir(state, world, pos) ? 1 : 0;
	}

	@Override
	public int getRadius(IBlockState state) {
		return 0;
	}

	@Override
	public boolean shouldAnalyse() {
		return false;
	}

	@Override
	public MapSignal analyse(IBlockState state, World world, BlockPos pos, EnumFacing fromDir, MapSignal signal) {
		return signal;
	}

	@Override
	public int branchSupport(IBlockState state, IBlockAccess world, BlockBranch branch, BlockPos pos, EnumFacing dir, int radius) {
		return BlockBranch.setSupport(0, branch.getFamily().isCompatibleVanillaLeaves(state, world, pos) ? 1 : 0);
	}

	@Override
	public TreeFamily getFamily(IBlockState state, IBlockAccess world, BlockPos pos) {
		return TreeFamily.NULLFAMILY;
	}

	public final TreePartType getTreePartType() {
		return TreePartType.NULL;
	}

}
