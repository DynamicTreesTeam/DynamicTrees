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
				killSurroundingLeaves(world, x, y, z);//Destroy the surrounding leaves
			}
		}
		
		return true;
	}

	//Clumsy hack to eliminate leaves
	public void killSurroundingLeaves(World world, int x, int y, int z){
		for(int iz = z - 3; iz <= z + 3; iz++){
			for(int iy = y - 3; iy <= y + 3; iy++){
				for(int ix = x - 3; ix <= x + 3; ix++){
					if(tree.isCompatibleGenericLeaves(world, ix, iy, iz)){
						world.setBlockToAir(ix, iy, iz);
						int qty = tree.getGrowingLeaves().quantityDropped(world.rand);
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
