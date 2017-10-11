package com.ferreusveritas.dynamictrees.special;

import java.util.Random;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.IBottomListener;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockMushroom;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.init.Blocks;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BottomListenerPodzol implements IBottomListener {

	@Override
	public void run(World world, DynamicTree tree, BlockPos pos, Random random) {

		int x = pos.getX() + random.nextInt(3) - 1;
		int z = pos.getZ() + random.nextInt(3) - 1;

		final int darkThreshold = 4;

		for(int i = 0; i < 32; i++) {
			
			BlockPos offPos = new BlockPos(x, pos.getY() - 1 - i, z);
			
			if(!offPos.isAirBlock(world)) {
				Block block = offPos.getBlock(world);

				if(block instanceof BlockBranch || block instanceof BlockMushroom) {//Skip past Mushrooms and branches on the way down
					continue;
				}
				else 
					if(block instanceof BlockFlower || block instanceof BlockTallGrass || block instanceof BlockDoublePlant) {//Kill Plants
						if(world.getSavedLightValue(EnumSkyBlock.Sky, offPos.getX(), offPos.getY(), offPos.getZ()) <= darkThreshold) {
							world.setBlockToAir(pos.getX(), pos.getY(), pos.getZ());
						}
						continue;
					}
					else
						if(block == Blocks.dirt || block == Blocks.grass) {//Convert grass and dirt to podzol
							if(world.getSavedLightValue(EnumSkyBlock.Sky, offPos.getX(), offPos.getY(), offPos.getZ()) <= darkThreshold) {
								world.setBlock(offPos.getX(), offPos.getY(), offPos.getZ(), Blocks.dirt, 2, 3);//Set to podzol
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

		final ForgeDirection HORIZONTALS[] = { ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.EAST, ForgeDirection.WEST };

		int Podzolish = 0;

		for(ForgeDirection dir: HORIZONTALS) {
			BlockPos deltaPos = pos.offset(dir);
			Block testBlock = deltaPos.getBlock(world);
			Podzolish += testBlock == Blocks.dirt && deltaPos.getMeta(world) == 2 ? 1 : 0;
			Podzolish += testBlock == DynamicTrees.blockRootyDirt ? 1 : 0;
			if(Podzolish >= 3) {
				world.setBlock(pos.getX(), pos.getY(), pos.getZ(), Blocks.dirt, 2, 3);
				break;
			}
		}
	}

}
