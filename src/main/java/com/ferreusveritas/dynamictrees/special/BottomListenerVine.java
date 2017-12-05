 package com.ferreusveritas.dynamictrees.special;

import java.util.Random;

import com.ferreusveritas.dynamictrees.api.IBottomListener;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.block.BlockVine;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BottomListenerVine implements IBottomListener {

	@Override
	public void run(World world, DynamicTree tree, BlockPos pos, Random random) {

		EnumFacing dir = EnumFacing.HORIZONTALS[random.nextInt(EnumFacing.HORIZONTALS.length)];
		
		BlockPos deltaPos = pos.offset(dir);
		
		if(world.isAirBlock(deltaPos) && (coordHashCode(deltaPos) & 7) == 0) {
			world.setBlockState(deltaPos, Blocks.VINE.getDefaultState()
					.withProperty(BlockVine.UP, dir == EnumFacing.UP)
					.withProperty(BlockVine.NORTH, dir == EnumFacing.NORTH)
					.withProperty(BlockVine.SOUTH, dir == EnumFacing.SOUTH)
					.withProperty(BlockVine.EAST, dir == EnumFacing.EAST)
					.withProperty(BlockVine.WEST, dir == EnumFacing.WEST), 2);
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
