package com.ferreusveritas.dynamictrees.special;

import java.util.Random;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.IBottomListener;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockMushroom;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.init.Blocks;
import net.minecraft.world.EnumSkyBlock;
import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;
import com.ferreusveritas.dynamictrees.api.backport.IBlockState;
import com.ferreusveritas.dynamictrees.api.backport.World;
import com.ferreusveritas.dynamictrees.api.backport.BlockAndMeta;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;

public class BottomListenerPodzol implements IBottomListener {

	private static final IBlockState podzolState = new BlockAndMeta(Blocks.dirt, 2);
	
	@Override
	public void run(World world, DynamicTree tree, BlockPos pos, Random random) {
		
		int x = pos.getX() + random.nextInt(3) - 1;
		int z = pos.getZ() + random.nextInt(3) - 1;

		final int darkThreshold = 4;

		for(int i = 0; i < 32; i++) {
			
			BlockPos offPos = new BlockPos(x, pos.getY() - 1 - i, z);
			
			if(!world.isAirBlock(offPos)) {
				Block block = world.getBlockState(offPos).getBlock();

				if(block instanceof BlockBranch || block instanceof BlockMushroom) {//Skip past Mushrooms and branches on the way down
					continue;
				}
				else 
					if(block instanceof BlockFlower || block instanceof BlockTallGrass || block instanceof BlockDoublePlant) {//Kill Plants
						if(world.getLightFor(EnumSkyBlock.Sky, offPos) <= darkThreshold) {
							world.setBlockToAir(pos);
						}
						continue;
					}
					else
						if(block == Blocks.dirt || block == Blocks.grass) {//Convert grass and dirt to podzol
							if(world.getLightFor(EnumSkyBlock.Sky, offPos.up()) <= darkThreshold) {
								world.setBlockState(offPos, podzolState);
							} else {
								spreadPodzol(world, pos);
							}
						}
				break;
			}
		}

	}

	@Override
	public float chance() {
		return 1f/256f;
	}

	@Override
	public String getName() {
		return "podzol";
	}

	public static void spreadPodzol(World world, BlockPos pos) {

		int Podzolish = 0;

		for(EnumFacing dir: EnumFacing.HORIZONTALS) {
			BlockPos deltaPos = pos.offset(dir);
			IBlockState testBlockState = world.getBlockState(deltaPos);
			Block testBlock = testBlockState.getBlock();
			Podzolish += testBlock == Blocks.dirt && testBlockState.getMeta() == 2 ? 1 : 0;
			Podzolish += testBlock == DynamicTrees.blockRootyDirt ? 1 : 0;
			if(Podzolish >= 3) {
				world.setBlockState(pos, podzolState);
				break;
			}
		}
	}

}
