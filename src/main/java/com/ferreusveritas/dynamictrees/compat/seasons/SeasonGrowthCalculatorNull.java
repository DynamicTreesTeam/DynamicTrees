package com.ferreusveritas.dynamictrees.compat.seasons;

import com.ferreusveritas.dynamictrees.api.seasons.ClimateZoneType;
import com.ferreusveritas.dynamictrees.api.seasons.ISeasonGrowthCalculator;

//Simply returns 1.0f for all values so it's as if there's no seasonal change
public class SeasonGrowthCalculatorNull implements ISeasonGrowthCalculator {

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

}