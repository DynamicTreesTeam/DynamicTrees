package com.ferreusveritas.dynamictrees.systems.featuregen;

//public class FeatureGenClearVolume implements IPreGenFeature {
//
//	private final int height;
//
//	public FeatureGenClearVolume(int height) {
//		this.height = height;
//	}
//
//	@Override
//	public BlockPos preGeneration(World world, BlockPos rootPos, Species species, int radius, Direction facing, SafeChunkBounds safeBounds, JoCode joCode) {
//		//Erase a volume of blocks that could potentially get in the way
//		for(BlockPos pos : BlockPos.getAllInBoxMutable(rootPos.add(new Vec3i(-1,  1, -1)), rootPos.add(new Vec3i(1, height, 1)))) {
//			world.removeBlock(pos, false);
//		}
//		return rootPos;
//	}
//
//}
