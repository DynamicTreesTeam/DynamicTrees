package com.ferreusveritas.growingtrees.inspectors;

import com.ferreusveritas.growingtrees.TreeHelper;
import com.ferreusveritas.growingtrees.blocks.BlockBranch;
import com.ferreusveritas.growingtrees.trees.GrowingTree;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Destroys all thin(radius == 1) branches on a tree.. leaving it to rot.
 * @author ferreusveritas
 */
public class NodeDisease implements INodeInspector {

	GrowingTree tree;//Destroy any thin branches made of the same kind of wood.
	
	public NodeDisease(GrowingTree tree) {
		this.tree = tree;
	}
	
	@Override
	public boolean run(World world, Block block, int x, int y, int z, ForgeDirection fromDir) {
		BlockBranch branch = TreeHelper.getBranch(block);
		
		if(branch != null && tree == branch.getTree()){
			if(branch.getRadius(world, x, y, z) == 1){
				world.setBlockToAir(x, y, z);//Destroy the thin branch
			}
		}
		
		return true;
	}
	
	@Override
	public boolean returnRun(World world, Block block, int x, int y, int z, ForgeDirection fromDir) {
		return false;
	}

}
