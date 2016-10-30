package com.ferreusveritas.growingtrees.inspectors;

import com.ferreusveritas.growingtrees.TreeHelper;
import com.ferreusveritas.growingtrees.blocks.BlockBranch;
import com.ferreusveritas.growingtrees.items.Seed;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class NodeDestroyer implements INodeInspector {

	BlockBranch wood;//Destroy any node that's made of the same kind of wood
	
	public NodeDestroyer(BlockBranch matchWood) {
		wood = matchWood;
	}
	
	@Override
	public boolean run(World world, Block block, int x, int y, int z, ForgeDirection fromDir) {
		BlockBranch branch = TreeHelper.getBranch(block);
		
		if(wood.isSameWood(branch)){
			if(branch.getRadius(world, x, y, z) == 1){
				killSurroundingLeaves(world, x, y, z);//Destroy the surrounding leaves
			}
			world.setBlockToAir(x, y, z);//Destroy the branch
		}
		
		return true;
	}

	//Clumsy hack to eliminate leaves
	public void killSurroundingLeaves(World world, int x, int y, int z){
		for(int iz = z - 3; iz <= z + 3; iz++){
			for(int iy = y - 3; iy <= y + 3; iy++){
				for(int ix = x - 3; ix <= x + 3; ix++){
					if(wood.isCompatibleGenericLeaves(world, ix, iy, iz)){
						world.setBlockToAir(ix, iy, iz);
						int qty = wood.getGrowingLeaves().quantityDropped(world.rand);
						if(qty != 0){
							Seed seed = wood.getGrowingLeaves().getSeed(wood.getGrowingLeavesSub());
							EntityItem itemEntity = new EntityItem(world, x, y, z, new ItemStack(seed, qty));
							itemEntity.setPosition(x, y, z);
							world.spawnEntityInWorld(itemEntity);
						}
					}
				}
			}
		}
	}
	
}
