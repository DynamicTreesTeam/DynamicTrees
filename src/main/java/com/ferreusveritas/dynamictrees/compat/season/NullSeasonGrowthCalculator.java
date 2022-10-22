package com.ferreusveritas.dynamictrees.compat.season;

import com.ferreusveritas.dynamictrees.api.season.ClimateZoneType;
import com.ferreusveritas.dynamictrees.api.season.SeasonGrowthCalculator;

/**
 * {@link SeasonGrowthCalculator} that returns {@code 1.0f} for all values so there's no seasonal change.
 *
 * @author ferreusveritas
 */
public class NullSeasonGrowthCalculator implements SeasonGrowthCalculator {

    @Override
    public float calcGrowthRate(Float seasonValue, ClimateZoneType type) {
        return 1.0f;
    }

    @Override
    public float calcSeedDropRate(Float seasonValue, ClimateZoneType type) {
        return 1.0f;
    }

    @Override
    public float calcFruitProduction(Float seasonValue, ClimateZoneType type) {
        return 1.0f;
    }

    @Override
    public Float getPeakFruitProductionSeasonValue(ClimateZoneType type) {
        return null;
    }

}