package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;
import com.ferreusveritas.dynamictrees.api.network.GrowSignal;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class NullTreePart implements ITreePart {

	//This is a safe dump for blocks that aren't tree parts
	//Handles some vanilla blocks

	@Override
	public int getHydrationLevel(IBlockAccess blockAccess, BlockPos pos, EnumFacing dir, DynamicTree leavesTree) {
		return 0;
	}

	@Override
	public GrowSignal growSignal(World world, BlockPos pos, GrowSignal signal) {
		return signal;
	}

	@Override
	public int getRadiusForConnection(IBlockAccess world, BlockPos pos, BlockBranch from, int fromRadius) {
		//Twigs connect to Vanilla leaves
		return fromRadius == 1 && from.getTree().getPrimitiveLeaves().matches(pos.getBlockState(world), 3) ? 1 : 0;
	}

	@Override
	public int probabilityForBlock(IBlockAccess blockAccess, BlockPos pos, BlockBranch from) {
		return pos.isAirBlock(blockAccess) ? 1 : 0;
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
	public int branchSupport(IBlockAccess blockAccess, BlockBranch branch, BlockPos pos, EnumFacing dir,	int radius) {
		Block block = pos.getBlock(blockAccess);
		if(block instanceof BlockLeaves) {//Vanilla leaves can be used for support
			if(branch.getTree().getPrimitiveLeaves().matches(pos.getBlockState(blockAccess), 3)) {
				return 0x01;
			}
		}
		return 0;
	}

	@Override
	public boolean applyItemSubstance(World world, BlockPos pos, EntityPlayer player, ItemStack itemStack) {
		return false;
	}

	@Override
	public DynamicTree getTree(IBlockAccess blockAccess, BlockPos pos) {
		return null;
	}

}
