package com.ferreusveritas.dynamictrees.inspectors;

import javax.swing.text.AbstractDocument.BranchElement;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.TreeHelper;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class NodeFruitCocoa extends NodeFruit {

	boolean finished;

	public NodeFruitCocoa(DynamicTree tree) {
		super(tree);
		finished = false;
	}

	@Override
	public boolean run(World world, Block block, int x, int y, int z, ForgeDirection fromDir) {
		if(!finished) {
			int hashCode = coordHashCode(x, y, z);
			if((hashCode % 97) % 29 == 0) {
				BlockBranch branch = TreeHelper.getBranch(world, x, y, z);
				if(branch != null && branch.getRadius(world, x, y, z) == 8) {
					int side = (hashCode % 4) + 2;
					ForgeDirection dir = ForgeDirection.getOrientation(side);
					x += dir.offsetX;
					z += dir.offsetZ;
					if (world.isAirBlock(x, y, z)) {
						int meta = DynamicTrees.blockFruitCocoa.onBlockPlaced(world, x, y, z, side, 0, 0, 0, 0);
						world.setBlock(x, y, z, DynamicTrees.blockFruitCocoa, meta, 2);
					}
				} else {
					finished = true;
				}
			}
		}
		return false;
	}

	public static int coordHashCode(int x, int y, int z) {
		int hash = (x * 7933711 ^ y * 6144389 ^ z * 9538033) >> 1;
		return hash & 0xFFFF;
	}

}
