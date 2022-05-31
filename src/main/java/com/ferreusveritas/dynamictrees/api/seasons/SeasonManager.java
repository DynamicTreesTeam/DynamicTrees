package com.ferreusveritas.dynamictrees.api.seasons;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Manages Seasonal output rates.
 *
 * @author ferreusveritas
 */
public interface SeasonManager {

    void updateTick(Level world, long worldTicks);

    void flushMappings();

    float getGrowthFactor(Level world, BlockPos rootPos, float offset);

    float getSeedDropFactor(Level world, BlockPos rootPos, float offset);

    float getFruitProductionFactor(Level world, BlockPos rootPos, float offset, boolean getAsScan);

    Float getSeasonValue(Level world, BlockPos rootPos);

    boolean isTropical(Level world, BlockPos rootPos);

    boolean shouldSnowMelt(Level world, BlockPos pos);

}
