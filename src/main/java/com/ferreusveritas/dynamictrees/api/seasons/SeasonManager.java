package com.ferreusveritas.dynamictrees.api.seasons;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

/**
 * Manages Seasonal output rates.
 *
 * @author ferreusveritas
 */
public interface SeasonManager {

    void updateTick(World world, long worldTicks);

    void flushMappings();

    float getGrowthFactor(World world, BlockPos rootPos, float offset);

    float getSeedDropFactor(World world, BlockPos rootPos, float offset);

    float getFruitProductionFactor(World world, BlockPos rootPos, float offset, boolean getAsScan);

    Float getSeasonValue(World world, BlockPos rootPos);

    Float getPeakFruitProductionSeasonValue(World world, BlockPos rootPos, float offset);

    boolean isTropical(IWorld world, BlockPos rootPos);

    boolean shouldSnowMelt(World world, BlockPos pos);

}
