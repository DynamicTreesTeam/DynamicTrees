package com.ferreusveritas.dynamictrees.systems.featuregen;

import com.ferreusveritas.dynamictrees.api.IFullGenFeature;
import com.ferreusveritas.dynamictrees.api.IPostGenFeature;
import com.ferreusveritas.dynamictrees.cells.LeafClusters;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class BushGenFeature implements IFullGenFeature, IPostGenFeature {

	private BlockState logState = Blocks.OAK_LOG.getDefaultState();
	private BlockState leavesState = Blocks.OAK_LEAVES.getDefaultState().with(LeavesBlock.PERSISTENT, true);
	private BlockState secondaryLeavesState = null;
	private Predicate<Biome> biomePredicate = i -> true;

	public BushGenFeature setLogState(BlockState logState) {
		this.logState = logState;
		return this;
	}

	public BushGenFeature setLeavesState(BlockState leavesState) {
		this.leavesState = leavesState;
		return this;
	}

	public BushGenFeature setSecondaryLeavesState(BlockState secondaryLeavesState) {
		this.secondaryLeavesState = secondaryLeavesState;
		return this;
	}

	public BushGenFeature setBiomePredicate(Predicate<Biome> biomePredicate) {
		this.biomePredicate = biomePredicate;
		return this;
	}

	@Override
	public boolean generate(IWorld world, BlockPos rootPos, Species species, Biome biome, Random random, int radius, SafeChunkBounds safeBounds) {
		commonGen(world, rootPos, species, random, radius, safeBounds);
		return true;
	}

	@Override
	public boolean postGeneration(IWorld world, BlockPos rootPos, Species species, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, BlockState initialDirtState, float seasonValue, float seasonFruitProductionFactor) {
		if(safeBounds != SafeChunkBounds.ANY && biomePredicate.test(biome)) {
			commonGen(world, rootPos, species, world.getRandom(), radius, safeBounds);
			return true;
		}
		return false;
	}

	protected void commonGen(IWorld world, BlockPos rootPos, Species species, Random random, int radius, SafeChunkBounds safeBounds) {
		if (radius <= 2) return;

		Vector3d vTree = new Vector3d(rootPos.getX(), rootPos.getY(), rootPos.getZ()).add(0.5, 0.5, 0.5);

		for (int i = 0; i < 2; i++) {
			int rad = MathHelper.clamp(random.nextInt(radius - 2) + 2, 2, radius - 1);
			Vector3d v = vTree.add(new Vector3d(1, 0, 0).scale(rad).rotateYaw((float) (random.nextFloat() * Math.PI * 2)));
			BlockPos vPos = new BlockPos(v);
			if (!safeBounds.inBounds(vPos, true)) continue;

			BlockPos pos = CoordUtils.findGround(world, vPos);
			BlockState soilBlockState = world.getBlockState(pos);

			pos = pos.up();
			if (!world.getBlockState(pos).getMaterial().isLiquid() && species.isAcceptableSoil(world, pos, soilBlockState)) {
				world.setBlockState(pos, logState, 3);

				SimpleVoxmap leafMap = LeafClusters.bush;
				BlockPos.Mutable leafPos = new BlockPos.Mutable();
				for (BlockPos.Mutable dPos : leafMap.getAllNonZero()) {
					leafPos.setPos( pos.getX() + dPos.getX(), pos.getY() + dPos.getY(), pos.getZ() + dPos.getZ() );
					if (safeBounds.inBounds(leafPos, true) && (coordHashCode(leafPos) % 5) != 0 && world.getBlockState(leafPos).getMaterial().isReplaceable()) {
						world.setBlockState(leafPos, (secondaryLeavesState == null || random.nextInt(4) != 0) ? leavesState : secondaryLeavesState, 3);
					}
				}
			}
		}
	}

	public static int coordHashCode(BlockPos pos) {
		int hash = (pos.getX() * 4111 ^ pos.getY() * 271 ^ pos.getZ() * 3067) >> 1;
		return hash & 0xFFFF;
	}

}
