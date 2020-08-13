package com.ferreusveritas.dynamictrees.systems.featuregen;

import com.ferreusveritas.dynamictrees.api.IPostGrowFeature;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FeatureGenPodzol implements IPostGrowFeature {
	
	@Override
	public boolean postGrow(World world, BlockPos rootPos, BlockPos treePos, Species species, int soilLife, boolean natural) {
//		if(ModConfigs.podzolGen.get()) {
//			NodeFindEnds endFinder = new NodeFindEnds();
//			TreeHelper.startAnalysisFromRoot(world, rootPos, new MapSignal(endFinder));
//			List<BlockPos> endPoints = endFinder.getEnds();
//			if(!endPoints.isEmpty()) {
//
//				Random random = world.rand;
//				BlockPos pos = endPoints.get(random.nextInt(endPoints.size()));
//
//				int x = pos.getX() + random.nextInt(5) - 2;
//				int z = pos.getZ() + random.nextInt(5) - 2;
//
//				final int darkThreshold = 4;
//
//				for(int i = 0; i < 32; i++) {
//
//					BlockPos offPos = new BlockPos(x, pos.getY() - 1 - i, z);
//
//					if(!world.isAirBlock(offPos)) {
//						Block block = world.getBlockState(offPos).getBlock();
//
//						if(block instanceof BlockBranch || block instanceof BlockMushroom || block instanceof BlockLeaves) {//Skip past Mushrooms and branches on the way down
//							continue;
//						}
//						else
//							if(block instanceof BlockFlower || block instanceof BlockTallGrass || block instanceof BlockDoublePlant) {//Kill Plants
//								if(world.getLightFor(EnumSkyBlock.SKY, offPos) <= darkThreshold) {
//									world.setBlockToAir(pos);
//								}
//								continue;
//							}
//							else
//								if(block == Blocks.DIRT || block == Blocks.GRASS) {//Convert grass or dirt to podzol
//									if(world.getLightFor(EnumSkyBlock.SKY, offPos.up()) <= darkThreshold) {
//										world.setBlockState(offPos, ModBlocks.blockStates.podzol);
//									} else {
//										spreadPodzol(world, pos);
//									}
//								}
//						break;
//					}
//				}
//			}
//		}
		return true;
	}

	public static void spreadPodzol(World world, BlockPos pos) {
//
//		int Podzolish = 0;
//
//		for(Direction dir: CoordUtils.HORIZONTALS) {
//			BlockPos deltaPos = pos.offset(dir);
//			Block testBlock = world.getBlockState(deltaPos).getBlock();
//			Podzolish += (testBlock == Blocks.DIRT) && (world.getBlockState(deltaPos).getBlock() == Blocks.PODZOL) ? 1 : 0;
//			Podzolish += testBlock == ModRegistries.blockRootyDirt ? 1 : 0;
//			if(Podzolish >= 3) {
//				world.setBlockState(pos, ModRegistries.blockStates.podzol);
//				break;
//			}
//		}
	}
	
}
