package com.dtteam.dynamictrees.api.season;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

/**
 * Manages Seasonal output rates.
 *
 * @author ferreusveritas
 */
public interface SeasonManager {

    void updateTick(Level level, long dayTime);

    void flushMappings();

    float getGrowthFactor(Level level, BlockPos rootPos, float offset);

    float getSeedDropFactor(Level level, BlockPos rootPos, float offset);

    float getFruitProductionFactor(Level level, BlockPos rootPos, float offset);

    Float getSeasonValue(Level level, BlockPos rootPos);

    Float getPeakFruitProductionSeasonValue(Level level, BlockPos rootPos, float offset);

    ClimateZoneType getClimate(LevelAccessor level, BlockPos rootPos);

    boolean shouldSnowMelt(Level level, BlockPos pos);

    void clearCache(Level level);

}
