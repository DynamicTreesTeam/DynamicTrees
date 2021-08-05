package com.ferreusveritas.dynamictrees.api.seasons;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Manages Seasonal output rates
 *
 * @author ferreusveritas
 */
public interface ISeasonManager {

	void updateTick(World world, long worldTicks);

	void flushMappings();

	float getGrowthFactor(World world, BlockPos rootPos, float offset);

	float getSeedDropFactor(World world, BlockPos rootPos, float offset);

	float getFruitProductionFactor(World world, BlockPos rootPos, float offset);

	Float getSeasonValue(World world, BlockPos rootPos);

	boolean isTropical(World world, BlockPos rootPos);

	boolean shouldSnowMelt(World world, BlockPos pos);

}
