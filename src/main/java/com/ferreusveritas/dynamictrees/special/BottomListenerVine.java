 package com.ferreusveritas.dynamictrees.special;

import java.util.Random;

import com.ferreusveritas.dynamictrees.api.IBottomListener;
import com.ferreusveritas.dynamictrees.api.backport.BlockAndMeta;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;
import com.ferreusveritas.dynamictrees.api.backport.World;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.init.Blocks;

public class BottomListenerVine implements IBottomListener {

	@Override
	public void run(World world, DynamicTree tree, BlockPos pos, Random random) {
		EnumFacing dir = EnumFacing.HORIZONTALS[random.nextInt(EnumFacing.HORIZONTALS.length)];
		
		BlockPos deltaPos = pos.offset(dir);
		
		if(world.isAirBlock(deltaPos) && (coordHashCode(deltaPos) & 7) == 0) {
			world.setBlockState(deltaPos, new BlockAndMeta(Blocks.vine, 
					dir == EnumFacing.NORTH ? 1 :
					dir == EnumFacing.SOUTH ? 4 :
					dir == EnumFacing.EAST ? 2 :
					dir == EnumFacing.WEST ? 8 : 0), 2);
		}
		
	}

	@Override
	public float chance() {
		return 1f;
	}

	@Override
	public String getName() {
		return "vine";
	}

	public static int coordHashCode(BlockPos pos) {
		int hash = (pos.getX() * 4111 ^ pos.getY() * 271 ^ pos.getZ() * 3067) >> 1;
		return hash & 0xFFFF;
	}

}
