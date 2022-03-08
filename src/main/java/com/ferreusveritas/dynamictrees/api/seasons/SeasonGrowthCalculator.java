package com.ferreusveritas.dynamictrees.api.seasons;

import javax.annotation.Nullable;

/**
 * Provides a means to calculate temperate and tropical growth rate values.
 *
 * @author ferreusveritas
 */
public interface SeasonGrowthCalculator {

    float calcGrowthRate(Float seasonValue, ClimateZoneType type);

    float calcSeedDropRate(Float seasonValue, ClimateZoneType type);

    float calcFruitProduction(Float seasonValue, ClimateZoneType type);

    /**
     * @return the season value at which the fruit production will be at its highest, or {@code null} if the specified
     * {@code type} is unsupported
     */
    @Nullable
    Float getPeakFruitProductionSeasonValue(ClimateZoneType type);

}
