package com.ferreusveritas.dynamictrees.genfeatures;

import java.util.List;
import java.util.Random;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.api.IGenFeature;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.BlockState;
import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;
import com.ferreusveritas.dynamictrees.api.backport.IBlockState;
import com.ferreusveritas.dynamictrees.api.backport.World;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockMushroom;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.init.Blocks;
import net.minecraft.world.EnumSkyBlock;

public class GenFeaturePodzol implements IGenFeature {

	private static final IBlockState podzolState = new BlockState(Blocks.dirt, 2);
	
	@Override
	public void gen(World world, BlockPos treePos, List<BlockPos> endPoints) {

		if(!endPoints.isEmpty()) {

			Random random = world.rand;
			BlockPos pos = endPoints.get(random.nextInt(endPoints.size()));
			
			int x = pos.getX() + random.nextInt(5) - 2;
			int z = pos.getZ() + random.nextInt(5) - 2;

			final int darkThreshold = 4;

			for(int i = 0; i < 32; i++) {

				BlockPos offPos = new BlockPos(x, pos.getY() - 1 - i, z);

				if(!world.isAirBlock(offPos)) {
					Block block = world.getBlockState(offPos).getBlock();

					if(block instanceof BlockBranch || block instanceof BlockMushroom || block instanceof BlockLeaves) {//Skip past Mushrooms and branches on the way down
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
							if(block == Blocks.dirt || block == Blocks.grass) {//Convert grass or dirt to podzol
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
	}

	public static void spreadPodzol(World world, BlockPos pos) {

		int Podzolish = 0;

		for(EnumFacing dir: EnumFacing.HORIZONTALS) {
			BlockPos deltaPos = pos.offset(dir);
			Block testBlock = world.getBlockState(deltaPos).getBlock();
			Podzolish += (testBlock == Blocks.dirt) && (world.getBlockState(deltaPos).getMeta() == 2) ? 1 : 0;
			Podzolish += testBlock == ModBlocks.blockRootyDirt ? 1 : 0;
			if(Podzolish >= 3) {
				world.setBlockState(pos, podzolState);
				break;
			}
		}
	}
}