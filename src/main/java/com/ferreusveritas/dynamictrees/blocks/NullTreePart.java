package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.api.backport.BlockAccess;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;
import com.ferreusveritas.dynamictrees.api.backport.EnumHand;
import com.ferreusveritas.dynamictrees.api.backport.IBlockState;
import com.ferreusveritas.dynamictrees.api.backport.World;
import com.ferreusveritas.dynamictrees.api.cells.Cells;
import com.ferreusveritas.dynamictrees.api.cells.ICell;
import com.ferreusveritas.dynamictrees.api.network.GrowSignal;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class NullTreePart implements ITreePart {

	//This is a safe dump for blocks that aren't tree parts
	//Handles some vanilla blocks

	@Override
	public ICell getHydrationCell(BlockAccess blockAccess, BlockPos pos, IBlockState blockState, EnumFacing dir, DynamicTree leavesTree) {
		return Cells.nullCell;
	}

	@Override
	public GrowSignal growSignal(World world, BlockPos pos, GrowSignal signal) {
		return signal;
	}

	@Override
	public int getRadiusForConnection(BlockAccess blockAccess, BlockPos pos, BlockBranch from, int fromRadius) {
		//Twigs connect to Vanilla leaves
		return fromRadius == 1 && from.getTree().getPrimitiveLeaves().matches(blockAccess.getBlockState(pos), 3) ? 1 : 0;
	}

	@Override
	public int probabilityForBlock(BlockAccess blockAccess, BlockPos pos, BlockBranch from) {
		return blockAccess.isAirBlock(pos) ? 1 : 0;
	}

	@Override
	public int getRadius(BlockAccess blockAccess, BlockPos pos) {
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
	public int branchSupport(BlockAccess blockAccess, BlockBranch branch, BlockPos pos, EnumFacing dir, int radius) {
		if(branch.getTree().getPrimitiveLeaves().matches(blockAccess.getBlockState(pos), 3)) {//Vanilla leaves can be used for support
			return 0x01;
		}
		return 0;
	}

	@Override
	public boolean applyItemSubstance(World world, BlockPos pos, EntityPlayer player, EnumHand hand, ItemStack itemStack) {
		return false;
	}

	@Override
	public DynamicTree getTree(BlockAccess blockAccess, BlockPos pos) {
		return null;
	}

}
