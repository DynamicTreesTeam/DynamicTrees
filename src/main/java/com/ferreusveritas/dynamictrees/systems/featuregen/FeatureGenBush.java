package com.ferreusveritas.dynamictrees.systems.featuregen;

//public class FeatureGenBush implements IFullGenFeature, IPostGenFeature {
//
//	private BlockState logState = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.OAK);
//	private BlockState leavesState = Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.OAK).withProperty(BlockOldLeaf.CHECK_DECAY, false);
//	private BlockState secondaryLeavesState = null;
//	private Predicate<Biome> biomePredicate = i -> true;
//
//	public FeatureGenBush setLogState(BlockState logState) {
//		this.logState = logState;
//		return this;
//	}
//
//	public FeatureGenBush setLeavesState(BlockState leavesState) {
//		this.leavesState = leavesState;
//		return this;
//	}
//
//	public FeatureGenBush setSecondaryLeavesState(BlockState secondaryLeavesState) {
//		this.secondaryLeavesState = secondaryLeavesState;
//		return this;
//	}
//
//	public FeatureGenBush setBiomePredicate(Predicate<Biome> biomePredicate) {
//		this.biomePredicate = biomePredicate;
//		return this;
//	}
//
//	@Override
//	public boolean generate(World world, BlockPos rootPos, Species species, Biome biome, Random random, int radius, SafeChunkBounds safeBounds) {
//		commonGen(world, rootPos, species, random, radius, safeBounds);
//		return true;
//	}
//
//	@Override
//	public boolean postGeneration(World world, BlockPos rootPos, Species species, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, BlockState initialDirtState) {
//		if(safeBounds != SafeChunkBounds.ANY && biomePredicate.test(biome)) {
//			commonGen(world, rootPos, species, world.rand, radius, safeBounds);
//			return true;
//		}
//		return false;
//	}
//
//	protected void commonGen(World world, BlockPos rootPos, Species species, Random random, int radius, SafeChunkBounds safeBounds) {
//		if (radius <= 2) return;
//
//		Vec3d vTree = new Vec3d(rootPos).addVector(0.5, 0.5, 0.5);
//
//		for (int i = 0; i < 2; i++) {
//			int rad = MathHelper.clamp(world.rand.nextInt(radius - 2) + 2, 2, radius - 1);
//			Vec3d v = vTree.add(new Vec3d(1, 0, 0).scale(rad).rotateYaw((float) (random.nextFloat() * Math.PI * 2)));
//			BlockPos vPos = new BlockPos(v);
//			if (!safeBounds.inBounds(vPos, true)) continue;
//
//			BlockPos pos = CoordUtils.findGround(world, vPos);
//			BlockState soilBlockState = world.getBlockState(pos);
//
//			pos = pos.up();
//			if (!world.getBlockState(pos).getMaterial().isLiquid() && species.isAcceptableSoil(world, pos, soilBlockState)) {
//				world.setBlockState(pos, logState);
//
//				SimpleVoxmap leafMap = LeafClusters.bush;
//				MutableBlockPos leafPos = new MutableBlockPos();
//				for (MutableBlockPos dPos : leafMap.getAllNonZero()) {
//					leafPos.setPos( pos.getX() + dPos.getX(), pos.getY() + dPos.getY(), pos.getZ() + dPos.getZ() );
//					if (safeBounds.inBounds(leafPos, true) && (coordHashCode(leafPos) % 5) != 0 && world.getBlockState(leafPos).getBlock().isReplaceable(world, leafPos)) {
//						world.setBlockState(leafPos, (secondaryLeavesState == null || random.nextInt(4) != 0) ? leavesState : secondaryLeavesState);
//					}
//				}
//			}
//		}
//	}
//
//	public static int coordHashCode(BlockPos pos) {
//		int hash = (pos.getX() * 4111 ^ pos.getY() * 271 ^ pos.getZ() * 3067) >> 1;
//		return hash & 0xFFFF;
//	}
//
//}
