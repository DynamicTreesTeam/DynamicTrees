package com.ferreusveritas.dynamictrees.api.seasons;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Manages Seasonal output rates.
 *
 * @author ferreusveritas
 */
public interface SeasonManager {

    void updateTick(Level level, long worldTicks);

    void flushMappings();

    float getGrowthFactor(Level level, BlockPos rootPos, float offset);

    float getSeedDropFactor(Level level, BlockPos rootPos, float offset);

    float getFruitProductionFactor(Level level, BlockPos rootPos, float offset, boolean getAsScan);

    Float getSeasonValue(Level level, BlockPos rootPos);

    Float getPeakFruitProductionSeasonValue(Level level, BlockPos rootPos, float offset);

    boolean isTropical(Level level, BlockPos rootPos);

    boolean shouldSnowMelt(Level level, BlockPos pos);

}
