package com.ferreusveritas.dynamictrees.systems.featuregen;

import com.ferreusveritas.dynamictrees.api.IPostGrowFeature;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyBlock;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NodeFindEnds;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import net.minecraft.block.*;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class PodzolGenFeature implements IPostGrowFeature {
	
	@Override
	public boolean postGrow(World world, BlockPos rootPos, BlockPos treePos, Species species, int soilLife, boolean natural) {
		if(DTConfigs.podzolGen.get()) {
			NodeFindEnds endFinder = new NodeFindEnds();
			TreeHelper.startAnalysisFromRoot(world, rootPos, new MapSignal(endFinder));
			List<BlockPos> endPoints = endFinder.getEnds();
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

						if(block instanceof BranchBlock || block instanceof MushroomBlock || block instanceof LeavesBlock) { //Skip past Mushrooms and branches on the way down
							continue;
						}
						else
							if(block instanceof FlowerBlock || block instanceof TallGrassBlock || block instanceof DoublePlantBlock) {//Kill Plants
								if(world.getLightFor(LightType.SKY, offPos) <= darkThreshold) {
									world.setBlockState(pos, Blocks.AIR.getDefaultState());
								}
								continue;
							}
							else
								if(block == Blocks.DIRT || block == Blocks.GRASS) {//Convert grass or dirt to podzol
									if(world.getLightFor(LightType.SKY, offPos.up()) <= darkThreshold) {
										world.setBlockState(offPos, DTRegistries.blockStates.podzol);
									} else {
										spreadPodzol(world, pos);
									}
								}
						break;
					}
				}
			}
		}
		return true;
	}

	public static void spreadPodzol(World world, BlockPos pos) {
		int podzolish = 0;

		for(Direction dir: CoordUtils.HORIZONTALS) {
			BlockPos deltaPos = pos.offset(dir);
			Block testBlock = world.getBlockState(deltaPos).getBlock();
			podzolish += (testBlock == Blocks.PODZOL) ? 1 : 0;
			podzolish += testBlock instanceof RootyBlock ? 1 : 0;
			if(podzolish >= 3) {
				world.setBlockState(pos, DTRegistries.blockStates.podzol);
				break;
			}
		}
	}
	
}
