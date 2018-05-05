package com.ferreusveritas.dynamictrees.systems.featuregen;

import java.util.List;
import java.util.Random;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.api.IGenFeature;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockMushroom;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

public class FeatureGenPodzol implements IGenFeature {
	
	@Override
	public void gen(World world, BlockPos treePos, List<BlockPos> endPoints, SafeChunkBounds safeBounds) {

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
							if(world.getLightFor(EnumSkyBlock.SKY, offPos) <= darkThreshold) {
								world.setBlockToAir(pos);
							}
							continue;
						}
						else
							if(block == Blocks.DIRT || block == Blocks.GRASS) {//Convert grass or dirt to podzol
								if(world.getLightFor(EnumSkyBlock.SKY, offPos.up()) <= darkThreshold) {
									world.setBlockState(offPos, ModBlocks.blockStates.podzol);
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
			Podzolish += (testBlock == Blocks.DIRT) && (world.getBlockState(deltaPos).getValue(BlockDirt.VARIANT) == BlockDirt.DirtType.PODZOL) ? 1 : 0;
			Podzolish += testBlock == ModBlocks.blockRootyDirt ? 1 : 0;
			if(Podzolish >= 3) {
				world.setBlockState(pos, ModBlocks.blockStates.podzol);
				break;
			}
		}
	}
}
