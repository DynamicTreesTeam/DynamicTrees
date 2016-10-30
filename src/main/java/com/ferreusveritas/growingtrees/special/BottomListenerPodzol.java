package com.ferreusveritas.growingtrees.special;

import java.util.Random;

import com.ferreusveritas.growingtrees.GrowingTrees;
import com.ferreusveritas.growingtrees.blocks.BlockBranch;
import com.ferreusveritas.growingtrees.blocks.BlockGrowingLeaves;

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
	public void run(World world, BlockGrowingLeaves leaves, int x, int y, int z, int subBlockNum, Random random){

		final int darkThreshold = 4;

		for(int i = 0; i < 32; i++){
			int offy = y - 1 - i;
			if(!world.isAirBlock(x, offy, z)){
				Block block = world.getBlock(x, offy, z);

				if(block instanceof BlockBranch || block instanceof BlockMushroom){
					continue;
				}
				else 
					if(block instanceof BlockFlower || block instanceof BlockTallGrass || block instanceof BlockDoublePlant){
						if(world.getSavedLightValue(EnumSkyBlock.Sky, x, offy, z) <= darkThreshold){
							world.setBlockToAir(x, y, z);
						}
						continue;
					}
					else
						if(block == Blocks.dirt || block == Blocks.grass){
							if(world.getSavedLightValue(EnumSkyBlock.Sky, x, offy + 1, z) <= darkThreshold){
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
	
	public static void spreadPodzol(World world, int x, int y, int z){

		final ForgeDirection dirs[] = { ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.EAST, ForgeDirection.WEST };

		int Podzolish = 0;

		for(ForgeDirection dir: dirs){
			int dx = x + dir.offsetX;
			int dy = y + dir.offsetY;
			int dz = z + dir.offsetZ;
			Block testBlock = world.getBlock(dx, dy, dz);
			Podzolish += testBlock == Blocks.dirt && world.getBlockMetadata(dx, dy, dz) == 2 ? 1 : 0;
			Podzolish += testBlock == GrowingTrees.blockRootyDirt ? 1 : 0;
			if(Podzolish >= 3){
				world.setBlock(x, y, z, Blocks.dirt, 2, 3);
				break;
			}
		}
	}

}
