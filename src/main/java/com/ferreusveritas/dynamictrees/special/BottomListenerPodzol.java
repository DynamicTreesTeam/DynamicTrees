package com.ferreusveritas.dynamictrees.special;

import java.util.Random;

import com.ferreusveritas.dynamictrees.DynamicTrees;
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
	public void run(World world, DynamicTree tree, int x, int y, int z, Random random) {

		x = + random.nextInt(3) - 1;
		y = + random.nextInt(3) - 1;

		final int darkThreshold = 4;

		for(int i = 0; i < 32; i++) {
			int offy = y - 1 - i;
			if(!world.isAirBlock(x, offy, z)) {
				Block block = world.getBlock(x, offy, z);

				if(block instanceof BlockBranch || block instanceof BlockMushroom) {//Skip past Mushrooms and branches on the way down
					continue;
				}
				else 
					if(block instanceof BlockFlower || block instanceof BlockTallGrass || block instanceof BlockDoublePlant) {//Kill Plants
						if(world.getSavedLightValue(EnumSkyBlock.Sky, x, offy, z) <= darkThreshold) {
							world.setBlockToAir(x, y, z);
						}
						continue;
					}
					else
						if(block == Blocks.dirt || block == Blocks.grass) {//Convert grass and dirt to podzol
							if(world.getSavedLightValue(EnumSkyBlock.Sky, x, offy + 1, z) <= darkThreshold) {
								world.setBlock(x, offy, z, Blocks.dirt, 2, 3);//Set to podzol
							} else {
								spreadPodzol(world, x, y, z);
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

	public static void spreadPodzol(World world, int x, int y, int z) {

		final ForgeDirection dirs[] = { ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.EAST, ForgeDirection.WEST };

		int Podzolish = 0;

		for(ForgeDirection dir: dirs) {
			int dx = x + dir.offsetX;
			int dy = y + dir.offsetY;
			int dz = z + dir.offsetZ;
			Block testBlock = world.getBlock(dx, dy, dz);
			Podzolish += testBlock == Blocks.dirt && world.getBlockMetadata(dx, dy, dz) == 2 ? 1 : 0;
			Podzolish += testBlock == DynamicTrees.blockRootyDirt ? 1 : 0;
			if(Podzolish >= 3) {
				world.setBlock(x, y, z, Blocks.dirt, 2, 3);
				break;
			}
		}
	}

}
