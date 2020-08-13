package com.ferreusveritas.dynamictrees.systems.featuregen;

//public class FeatureGenFruit implements IPostGrowFeature, IPostGenFeature {
//
//	protected final BlockFruit blockFruit;
//	protected final BlockState ripeFruitState;
//	protected final BlockState unripeFruitState;
//	protected float verSpread = 30;
//	protected int qty = 4;
//	protected float rayDistance = 5;
//	protected int fruitingRadius = 8;
//
//	public FeatureGenFruit(BlockState unripeFruitState, BlockState ripeFruitState) {
//		this.ripeFruitState = ripeFruitState;
//		this.unripeFruitState = unripeFruitState;
//		this.blockFruit = null;
//	}
//
//	public FeatureGenFruit(BlockFruit fruit) {
//		this.ripeFruitState = null;
//		this.unripeFruitState = null;
//		this.blockFruit = fruit;
//	}
//
//	public FeatureGenFruit setRayDistance(float rayDistance) {
//		this.rayDistance = rayDistance;
//		return this;
//	}
//
//	public FeatureGenFruit setFruitingRadius(int fruitingRadius) {
//		this.fruitingRadius = fruitingRadius;
//		return this;
//	}
//
//	public int getQuantity(boolean worldGen) {
//		return worldGen ? 10 : 1;
//	}
//
//	@Override
//	public boolean postGeneration(World world, BlockPos rootPos, Species species, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, BlockState initialDirtState) {
//		if(!endPoints.isEmpty()) {
//			int qty = getQuantity(true);
//			for(int i = 0; i < qty; i++) {
//				BlockPos endPoint = endPoints.get(world.rand.nextInt(endPoints.size()));
//				addFruit(world, species, rootPos.up(), endPoint, true, false, safeBounds);
//			}
//			return true;
//		}
//		return false;
//	}
//
//	@Override
//	public boolean postGrow(World world, BlockPos rootPos, BlockPos treePos, Species species, int soilLife, boolean natural) {
//		BlockState blockState = world.getBlockState(treePos);
//		BlockBranch branch = TreeHelper.getBranch(blockState);
//
//		if(branch != null && branch.getRadius(blockState) >= fruitingRadius && natural) {
//			NodeFindEnds endFinder = new NodeFindEnds();
//			TreeHelper.startAnalysisFromRoot(world, rootPos, new MapSignal(endFinder));
//			List<BlockPos> endPoints = endFinder.getEnds();
//			int qty = getQuantity(false);
//			if(!endPoints.isEmpty()) {
//				for(int i = 0; i < qty; i++) {
//					BlockPos endPoint = endPoints.get(world.rand.nextInt(endPoints.size()));
//					addFruit(world, species, rootPos.up(), endPoint, false, true, SafeChunkBounds.ANY);
//				}
//			}
//		}
//
//		return true;
//	}
//
//	protected void addFruit(World world, Species species, BlockPos treePos, BlockPos branchPos, boolean worldGen, boolean enableHash, SafeChunkBounds safeBounds) {
//		BlockPos fruitPos = CoordUtils.getRayTraceFruitPos(world, species, treePos, branchPos, safeBounds);
//		if(fruitPos != BlockPos.ZERO) {
//			if ( !enableHash || ( (CoordUtils.coordHashCode(fruitPos, 0) & 3) == 0) ) {
//				BlockState setState;
//				if(blockFruit != null) {
//					setState = blockFruit.getStateForAge(worldGen ? blockFruit.getAgeForWorldGen(world, fruitPos) : 0);
//				} else {
//					setState = worldGen ? ripeFruitState : unripeFruitState;
//				}
//				world.setBlockState(fruitPos, setState);
//			}
//		}
//	}
//
//}