package com.ferreusveritas.dynamictrees.inspectors;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.block.Block;
import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;
import com.ferreusveritas.dynamictrees.api.backport.World;
import com.ferreusveritas.dynamictrees.api.backport.BlockState;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;

public class NodeFruitCocoa extends NodeFruit {
	
	boolean finished;
	
	public NodeFruitCocoa(DynamicTree tree) {
		super(tree);
		finished = false;
	}
	
	@Override
	public boolean run(World world, Block block, BlockPos pos, EnumFacing fromDir) {
		
		if(!finished) {
			int hashCode = coordHashCode(pos);
			if((hashCode % 97) % 29 == 0) {
				BlockBranch branch = TreeHelper.getBranch(world, pos);
				if(branch != null && branch.getRadius(world, pos) == 8) {
					int side = (hashCode % 4) + 2;
					EnumFacing dir = EnumFacing.getFront(side);
					pos = pos.offset(dir);
					if (world.isAirBlock(pos)) {
						int meta = DynamicTrees.blockFruitCocoa.onBlockPlaced(world.real(), pos.getX(), pos.getY(), pos.getZ(), side, 0, 0, 0, 0);
						world.setBlockState(pos, new BlockState(DynamicTrees.blockFruitCocoa, meta), 2);
					}
				} else {
					finished = true;
				}
			}
		}
		return false;
	}
	
	public static int coordHashCode(BlockPos pos) {
		int hash = (pos.getX() * 7933711 ^ pos.getY() * 6144389 ^ pos.getZ() * 9538033) >> 1;
		return hash & 0xFFFF;
	}
	
}
