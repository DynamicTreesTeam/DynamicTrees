package com.ferreusveritas.dynamictrees.systems.genfeatures;

import com.ferreusveritas.dynamictrees.api.IPostGrowFeature;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyBlock;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import com.ferreusveritas.dynamictrees.systems.nodemappers.FindEndsNode;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import net.minecraft.block.*;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class PodzolGenFeature extends GenFeature implements IPostGrowFeature {

	public PodzolGenFeature(ResourceLocation registryName) {
		super(registryName);
	}

	@Override
	public boolean postGrow(ConfiguredGenFeature<?> configuredGenFeature, World world, BlockPos rootPos, BlockPos treePos, Species species, int soilLife, boolean natural) {
		if(DTConfigs.podzolGen.get()) {
			FindEndsNode endFinder = new FindEndsNode();
			TreeHelper.startAnalysisFromRoot(world, rootPos, new MapSignal(endFinder));
			List<BlockPos> endPoints = endFinder.getEnds();
			if(!endPoints.isEmpty()) {

				Random random = world.random;
				BlockPos pos = endPoints.get(random.nextInt(endPoints.size()));

				int x = pos.getX() + random.nextInt(5) - 2;
				int z = pos.getZ() + random.nextInt(5) - 2;

				final int darkThreshold = 4;

				for(int i = 0; i < 32; i++) {

					BlockPos offPos = new BlockPos(x, pos.getY() - 1 - i, z);

					if(!world.isEmptyBlock(offPos)) {
						Block block = world.getBlockState(offPos).getBlock();

						if(block instanceof BranchBlock || block instanceof MushroomBlock || block instanceof LeavesBlock) { //Skip past Mushrooms and branches on the way down
							continue;
						}
						else
							if(block instanceof FlowerBlock || block instanceof TallGrassBlock || block instanceof DoublePlantBlock) {//Kill Plants
								if(world.getBrightness(LightType.SKY, offPos) <= darkThreshold) {
									world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
								}
								continue;
							}
							else
								if(block == Blocks.DIRT || block == Blocks.GRASS) {//Convert grass or dirt to podzol
									if(world.getBrightness(LightType.SKY, offPos.above()) <= darkThreshold) {
										world.setBlockAndUpdate(offPos, DTRegistries.BLOCK_STATES.PODZOL);
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
			BlockPos deltaPos = pos.relative(dir);
			Block testBlock = world.getBlockState(deltaPos).getBlock();
			podzolish += (testBlock == Blocks.PODZOL) ? 1 : 0;
			podzolish += testBlock instanceof RootyBlock ? 1 : 0;
			if(podzolish >= 3) {
				world.setBlockAndUpdate(pos, DTRegistries.BLOCK_STATES.PODZOL);
				break;
			}
		}
	}
	
}
