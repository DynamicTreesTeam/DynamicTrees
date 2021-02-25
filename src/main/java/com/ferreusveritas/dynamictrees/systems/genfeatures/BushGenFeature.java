package com.ferreusveritas.dynamictrees.systems.genfeatures;

import com.ferreusveritas.dynamictrees.api.IFullGenFeature;
import com.ferreusveritas.dynamictrees.api.IPostGenFeature;
import com.ferreusveritas.dynamictrees.cells.LeafClusters;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.GenFeatureProperty;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class BushGenFeature extends GenFeature implements IFullGenFeature, IPostGenFeature {

	public static final GenFeatureProperty<Block> LOG_BLOCK = GenFeatureProperty.createBlockProperty("log");
	public static final GenFeatureProperty<Block> LEAVES_BLOCK = GenFeatureProperty.createBlockProperty("leaves");
	public static final GenFeatureProperty<Block> SECONDARY_LEAVES_BLOCK = GenFeatureProperty.createBlockProperty("secondary_leaves");

	private Predicate<Biome> biomePredicate = i -> true;

	public BushGenFeature(ResourceLocation registryName) {
		super(registryName, LOG_BLOCK, LEAVES_BLOCK, SECONDARY_LEAVES_BLOCK);
	}

	@Override
	public ConfiguredGenFeature<?> createDefaultConfiguration() {
		return super.createDefaultConfiguration().with(LOG_BLOCK, Blocks.OAK_LOG).with(LEAVES_BLOCK, Blocks.OAK_LEAVES)
				.with(SECONDARY_LEAVES_BLOCK, null);
	}

	public BushGenFeature setBiomePredicate(Predicate<Biome> biomePredicate) {
		this.biomePredicate = biomePredicate;
		return this;
	}

	@Override
	public boolean generate(ConfiguredGenFeature<?> configuredGenFeature, IWorld world, BlockPos rootPos, Species species, Biome biome, Random random, int radius, SafeChunkBounds safeBounds) {
		this.commonGen(configuredGenFeature, world, rootPos, species, random, radius, safeBounds);
		return true;
	}

	@Override
	public boolean postGeneration(ConfiguredGenFeature<?> configuredGenFeature, IWorld world, BlockPos rootPos, Species species, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, BlockState initialDirtState, Float seasonValue, Float seasonFruitProductionFactor) {
		if(safeBounds != SafeChunkBounds.ANY && biomePredicate.test(biome)) {
			this.commonGen(configuredGenFeature, world, rootPos, species, world.getRandom(), radius, safeBounds);
			return true;
		}
		return false;
	}

	protected void commonGen(ConfiguredGenFeature<?> configuredGenFeature, IWorld world, BlockPos rootPos, Species species, Random random, int radius, SafeChunkBounds safeBounds) {
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
				world.setBlockState(pos, configuredGenFeature.get(LOG_BLOCK).getDefaultState(), 3);

				SimpleVoxmap leafMap = LeafClusters.bush;
				BlockPos.Mutable leafPos = new BlockPos.Mutable();
				for (BlockPos.Mutable dPos : leafMap.getAllNonZero()) {
					leafPos.setPos( pos.getX() + dPos.getX(), pos.getY() + dPos.getY(), pos.getZ() + dPos.getZ() );
					if (safeBounds.inBounds(leafPos, true) && (coordHashCode(leafPos) % 5) != 0 && world.getBlockState(leafPos).getMaterial().isReplaceable()) {
						world.setBlockState(leafPos, ((configuredGenFeature.get(SECONDARY_LEAVES_BLOCK) == null || random.nextInt(4) != 0) ?
								configuredGenFeature.get(LEAVES_BLOCK) : configuredGenFeature.get(SECONDARY_LEAVES_BLOCK))
								.getDefaultState().with(LeavesBlock.PERSISTENT, true), 3);
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
