package com.dtteam.dynamictrees.systems.season;

import com.dtteam.dynamictrees.api.season.ClimateZoneType;
import com.dtteam.dynamictrees.api.season.SeasonGrowthCalculator;

/**
 * {@link SeasonGrowthCalculator} that returns {@code 1.0f} for all values so there's no seasonal change.
 *
 * @author ferreusveritas
 */
public class NullSeasonGrowthCalculator implements SeasonGrowthCalculator {

    public float calcGrowthRate(Float seasonValue, ClimateZoneType type) {
        return 1.0f;
    }

    public float calcSeedDropRate(Float seasonValue, ClimateZoneType type) {
        return 1.0f;
    }

    public float calcFruitProductionRate(Float seasonValue, ClimateZoneType type) {
        return 1.0f;
    }

    public Float getPeakFruitProductionSeasonValue(ClimateZoneType type) {
        return null;
    }

}