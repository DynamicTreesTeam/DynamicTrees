package com.ferreusveritas.dynamictrees.systems.genfeatures;

import com.ferreusveritas.dynamictrees.api.IPostGenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.GenFeatureProperty;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Used to add mushrooms under a tree canopy.  Currently used by dark oaks
 * for roofed forests.
 * 
 * @author ferreusveritas
 */
public class HugeMushroomsGenFeature extends HugeMushroomGenFeature implements IPostGenFeature {

	public static final GenFeatureProperty<Integer> MAX_MUSHROOMS = GenFeatureProperty.createIntegerProperty("max_mushrooms");
	public static final GenFeatureProperty<Integer> MAX_ATTEMPTS = GenFeatureProperty.createIntegerProperty("max_attempts");

	public HugeMushroomsGenFeature(ResourceLocation registryName) {
		super(registryName, MAX_MUSHROOMS, MAX_ATTEMPTS);
	}

	@Override
	protected ConfiguredGenFeature<GenFeature> createDefaultConfiguration() {
		return super.createDefaultConfiguration().with(MAX_MUSHROOMS, 2).with(MAX_ATTEMPTS, 4);
	}

	@Override
	public boolean postGeneration(ConfiguredGenFeature<?> configuredGenFeature, IWorld world, BlockPos rootPos, Species species, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, BlockState initialDirtState, Float seasonValue, Float seasonFruitProductionFactor) {
		boolean worldGen = safeBounds != SafeChunkBounds.ANY;
		
		if (endPoints.isEmpty() || !worldGen) return false;
	
		BlockPos lowest = Collections.min(endPoints, (a, b) -> a.getY() - b.getY());
		
		Random rand = world.getRandom();
		
		int success = 0;
		
		if(radius >= 5) {
			for(int tries = 0; tries < configuredGenFeature.get(MAX_ATTEMPTS); tries++) {
				
				float angle = (float) (rand.nextFloat() * Math.PI * 2);
				int xOff = (int) (MathHelper.sin(angle) * (radius - 1));
				int zOff = (int) (MathHelper.cos(angle) * (radius - 1));
				
				BlockPos mushPos = rootPos.add(xOff, 0, zOff);
				
				mushPos = CoordUtils.findGround(world, new BlockPos(mushPos)).up();
				
				if(safeBounds.inBounds(mushPos, true)) {
					int maxHeight = lowest.getY() - mushPos.getY();
					if(maxHeight >= 2) {
						int height = MathHelper.clamp(rand.nextInt(maxHeight) + 3, 3, maxHeight);
						
						if(this.setHeight(height).generate(configuredGenFeature, world, mushPos.down(), species, biome, rand, radius, safeBounds)) {
							if(++success >= configuredGenFeature.get(MAX_MUSHROOMS)) {
								return true;
							}
						}
					}
				}
			}
		}

		return false;
	}
	
}
