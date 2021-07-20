package com.ferreusveritas.dynamictrees.systems.genfeatures;

import com.ferreusveritas.dynamictrees.api.IFullGenFeature;
import com.ferreusveritas.dynamictrees.api.IPostGenFeature;
import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.cells.LeafClusters;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
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

public class BushGenFeature extends GenFeature implements IFullGenFeature, IPostGenFeature {

	public static final ConfigurationProperty<Block> LOG_BLOCK = ConfigurationProperty.block("log");
	public static final ConfigurationProperty<Block> LEAVES_BLOCK = ConfigurationProperty.block("leaves");
	public static final ConfigurationProperty<Block> SECONDARY_LEAVES_BLOCK = ConfigurationProperty.block("secondary_leaves");

	public BushGenFeature(ResourceLocation registryName) {
		super(registryName);
	}

	@Override
	protected void registerProperties() {
		this.register(LOG_BLOCK, LEAVES_BLOCK, SECONDARY_LEAVES_BLOCK, BIOME_PREDICATE);
	}

	@Override
	public ConfiguredGenFeature<GenFeature> createDefaultConfiguration() {
		return super.createDefaultConfiguration()
				.with(BIOME_PREDICATE, i -> true)
				.with(LOG_BLOCK, Blocks.OAK_LOG)
				.with(LEAVES_BLOCK, Blocks.OAK_LEAVES);
	}

	@Override
	public boolean generate(ConfiguredGenFeature<?> configuredGenFeature, IWorld world, BlockPos rootPos, Species species, Biome biome, Random random, int radius, SafeChunkBounds safeBounds) {
		this.commonGen(configuredGenFeature, world, rootPos, species, random, radius, safeBounds);
		return true;
	}

	@Override
	public boolean postGeneration(ConfiguredGenFeature<?> configuredGenFeature, IWorld world, BlockPos rootPos, Species species, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, BlockState initialDirtState, Float seasonValue, Float seasonFruitProductionFactor) {
		if (safeBounds != SafeChunkBounds.ANY && configuredGenFeature.get(BIOME_PREDICATE).test(biome)) {
			this.commonGen(configuredGenFeature, world, rootPos, species, world.getRandom(), radius, safeBounds);
			return true;
		}
		return false;
	}

	protected void commonGen(ConfiguredGenFeature<?> configuredGenFeature, IWorld world, BlockPos rootPos, Species species, Random random, int radius, SafeChunkBounds safeBounds) {
		if (radius <= 2) {
			return;
		}

		final boolean worldGen = safeBounds != SafeChunkBounds.ANY;

		Vector3d vTree = new Vector3d(rootPos.getX(), rootPos.getY(), rootPos.getZ()).add(0.5, 0.5, 0.5);

		for (int i = 0; i < 2; i++) {
			int rad = MathHelper.clamp(random.nextInt(radius - 2) + 2, 2, radius - 1);
			Vector3d v = vTree.add(new Vector3d(1, 0, 0).scale(rad).yRot((float) (random.nextFloat() * Math.PI * 2)));
			BlockPos vPos = new BlockPos(v);

			if (!safeBounds.inBounds(vPos, true)) {
				continue;
			}

			final BlockPos groundPos = CoordUtils.findWorldSurface(world, vPos, worldGen);
			final BlockState soilBlockState = world.getBlockState(groundPos);

			final BlockPos pos = groundPos.above();
			if (!world.getBlockState(groundPos).getMaterial().isLiquid() && species.isAcceptableSoil(world, groundPos, soilBlockState)) {
				world.setBlock(pos, configuredGenFeature.get(LOG_BLOCK).defaultBlockState(), 3);

				SimpleVoxmap leafMap = LeafClusters.BUSH;
				BlockPos.Mutable leafPos = new BlockPos.Mutable();
				for (BlockPos.Mutable dPos : leafMap.getAllNonZero()) {
					leafPos.set( pos.getX() + dPos.getX(), pos.getY() + dPos.getY(), pos.getZ() + dPos.getZ() );
					if (safeBounds.inBounds(leafPos, true) && (coordHashCode(leafPos) % 5) != 0 && world.getBlockState(leafPos).getMaterial().isReplaceable()) {
						Block leaf = ((configuredGenFeature.get(SECONDARY_LEAVES_BLOCK) == null || random.nextInt(4) != 0) ?
								configuredGenFeature.get(LEAVES_BLOCK) : configuredGenFeature.get(SECONDARY_LEAVES_BLOCK));
						BlockState leafState = leaf.defaultBlockState();
						if (leaf instanceof LeavesBlock)  leafState = leafState.setValue(LeavesBlock.PERSISTENT, true);
						world.setBlock(leafPos, leafState, 3);
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
