package com.ferreusveritas.growingtrees.blocks;

import com.ferreusveritas.growingtrees.trees.GrowingTree;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class NullTreePart implements ITreePart {

	//This is a safe dump for blocks that aren't tree parts
	//Handles some vanilla blocks
	
	@Override
	public int getHydrationLevel(IBlockAccess blockAccess, int x, int y, int z, ForgeDirection dir,	GrowingTree leavesTree) {
		return 0;
	}

	@Override
	public GrowSignal growSignal(World world, int x, int y, int z, GrowSignal signal) {
		return signal;
	}

	@Override
	public int getRadiusForConnection(IBlockAccess world, int x, int y, int z, BlockBranch from, int fromRadius) {
		//Twigs connect to Vanilla leaves
		return fromRadius == 1 && from.getTree().getPrimitiveLeaves().matches(world, x, y, z, 3) ? 1 : 0;
	}

	@Override
	public int probabilityForBlock(IBlockAccess blockAccess, int x, int y, int z, BlockBranch from) {
		return blockAccess.isAirBlock(x, y, z) ? 1 : 0;
	}

	@Override
	public int getRadius(IBlockAccess blockAccess, int x, int y, int z) {
		return 0;
	}

	@Override
	public MapSignal analyse(World world, int x, int y, int z, ForgeDirection fromDir, MapSignal signal) {
		return signal;
	}

	@Override
	public boolean isRootNode() {
		return false;
	}

	@Override
	public int branchSupport(IBlockAccess blockAccess, BlockBranch branch, int x, int y, int z, ForgeDirection dir,	int radius) {
		Block block = blockAccess.getBlock(x, y, z);
		if(block instanceof BlockLeaves){//Vanilla leaves can be used for support
			if(branch.getTree().getPrimitiveLeaves().matches(blockAccess, x, y, z, 3)){
				return 0x01;
			}
		}
		return 0;
	}

	@Override
	public boolean applyItemSubstance(World world, int x, int y, int z, EntityPlayer player, ItemStack itemStack){
		return false;
	}

	@Override
	public GrowingTree getTree(IBlockAccess blockAccess, int x, int y, int z) {
		return null;
	}

}
