 package com.ferreusveritas.dynamictrees.special;

import java.util.Random;

import com.ferreusveritas.dynamictrees.api.IBottomListener;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BottomListenerVine implements IBottomListener {

	@Override
	public void run(World world, DynamicTree tree, BlockPos pos, Random random) {

		ForgeDirection HORIZONTALS[] = { ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.EAST, ForgeDirection.WEST, ForgeDirection.DOWN };
		ForgeDirection dir = HORIZONTALS[random.nextInt(HORIZONTALS.length)];
		
		BlockPos deltaPos = pos.offset(dir);
		
		if(deltaPos.isAirBlock(world) && (coordHashCode(deltaPos) & 7) == 0) {
			int metadata =
				dir == ForgeDirection.NORTH ? 1 :
				dir == ForgeDirection.SOUTH ? 4 :
				dir == ForgeDirection.EAST ? 2 :
				dir == ForgeDirection.WEST ? 8 : 0;
			world.setBlock(pos.getX() + dir.offsetX, pos.getY(), pos.getZ() + dir.offsetZ, Blocks.vine, metadata, 2);
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
