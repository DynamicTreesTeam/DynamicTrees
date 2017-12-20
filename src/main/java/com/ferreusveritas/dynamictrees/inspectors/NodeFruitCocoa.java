package com.ferreusveritas.dynamictrees.inspectors;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;
import com.ferreusveritas.dynamictrees.api.backport.IBlockState;
import com.ferreusveritas.dynamictrees.api.backport.World;
import com.ferreusveritas.dynamictrees.api.network.INodeInspector;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCocoa;

public class NodeFruitCocoa implements INodeInspector {

	boolean finished = false;
	boolean worldGen = false;
	
	public NodeFruitCocoa() {
	}

	public NodeFruitCocoa setWorldGen(boolean worldGen) {
		this.worldGen = worldGen;
		return this;
	}
	
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
						IBlockState cocoaState = ModBlocks.blockFruitCocoa.onBlockPlaced(world, pos, dir, 0, 0, 0, 0, null);
						if(worldGen) {
							cocoaState = cocoaState.withMeta(2 << 2);
						}
						world.setBlockState(pos, cocoaState, 2);
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

	@Override
	public boolean returnRun(World world, Block block, BlockPos pos, EnumFacing fromDir) {
		return false;
	}

}
