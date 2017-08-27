package com.ferreusveritas.dynamictrees.inspectors;

import com.ferreusveritas.dynamictrees.TreeHelper;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
* Destroys all branches on a tree and the surrounding leaves.
* @author ferreusveritas
*/
public class NodeDestroyer implements INodeInspector {

	DynamicTree tree;//Destroy any node that's made of the same kind of wood

	public NodeDestroyer(DynamicTree tree) {
		this.tree = tree;
	}

	@Override
	public boolean run(World world, Block block, int x, int y, int z, ForgeDirection fromDir) {
		BlockBranch branch = TreeHelper.getBranch(block);

		if(branch != null && tree == branch.getTree()) {
			if(branch.getRadius(world, x, y, z) == 1) {
				killSurroundingLeaves(world, x, y, z);//Destroy the surrounding leaves
			}
			world.setBlockToAir(x, y, z);//Destroy the branch
		}

		return true;
	}

	@Override
	public boolean returnRun(World world, Block block, int x, int y, int z, ForgeDirection fromDir) {
		return false;
	}

	//Clumsy hack to eliminate leaves
	public void killSurroundingLeaves(World world, int x, int y, int z) {
		for(int iz = z - 3; iz <= z + 3; iz++) {
			for(int iy = y - 3; iy <= y + 3; iy++) {
				for(int ix = x - 3; ix <= x + 3; ix++) {
					if(tree.isCompatibleGenericLeaves(world, ix, iy, iz)) {
						world.setBlockToAir(ix, iy, iz);
						int qty = tree.getGrowingLeaves().quantitySeedDropped(world.rand);
						if(qty != 0){
							EntityItem itemEntity = new EntityItem(world, x, y, z, new ItemStack(tree.getSeed(), qty));
							itemEntity.setPosition(x, y, z);
							world.spawnEntityInWorld(itemEntity);
						}
					}
				}
			}
		}
	}

}
