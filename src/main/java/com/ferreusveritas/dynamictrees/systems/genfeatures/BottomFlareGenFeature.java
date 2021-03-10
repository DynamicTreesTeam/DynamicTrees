package com.ferreusveritas.dynamictrees.systems.genfeatures;

import com.ferreusveritas.dynamictrees.api.IPostGenFeature;
import com.ferreusveritas.dynamictrees.api.IPostGrowFeature;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.GenFeatureProperty;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.List;

public class BottomFlareGenFeature extends GenFeature implements IPostGenFeature, IPostGrowFeature{

	// Min radius for the flare.
	public static final GenFeatureProperty<Integer> MIN_RADIUS = GenFeatureProperty.createIntegerProperty("min_radius");

	public BottomFlareGenFeature(ResourceLocation registryName) {
		super(registryName, MIN_RADIUS);
	}

	@Override
	public ConfiguredGenFeature<?> createDefaultConfiguration() {
		return super.createDefaultConfiguration().with(MIN_RADIUS, 6);
	}

	@Override
	public boolean postGrow(ConfiguredGenFeature<?> configuredGenFeature, World world, BlockPos rootPos, BlockPos treePos, Species species, int soilLife, boolean natural) {
		if(soilLife > 0) {
			this.flareBottom(configuredGenFeature, world, rootPos, species);
			return true;
		}
		return false;
	}

	@Override
	public boolean postGeneration(ConfiguredGenFeature<?> configuredGenFeature, IWorld world, BlockPos rootPos, Species species, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, BlockState initialDirtState, Float seasonValue, Float seasonFruitProductionFactor) {
		this.flareBottom(configuredGenFeature, world, rootPos, species);
		return true;
	}
	
	/**
	 * Put a cute little flare on the bottom of the dark oaks
	 * 
	 * @param world The world
	 * @param rootPos The position of the rooty dirt block of the tree
	 */
	public void flareBottom(ConfiguredGenFeature<?> configuredGenFeature, IWorld world, BlockPos rootPos, Species species) {
		Family family = species.getFamily();

		//Put a cute little flare on the bottom of the dark oaks
		int radius3 = TreeHelper.getRadius(world, rootPos.up(3));

		if(radius3 > configuredGenFeature.get(MIN_RADIUS)) {
			family.getDynamicBranch().setRadius(world, rootPos.up(2), radius3 + 1, Direction.UP);
			family.getDynamicBranch().setRadius(world, rootPos.up(1), radius3 + 2, Direction.UP);
		}
	}
	
}
