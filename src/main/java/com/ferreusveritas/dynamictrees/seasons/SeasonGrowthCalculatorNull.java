package com.ferreusveritas.dynamictrees.seasons;

import com.ferreusveritas.dynamictrees.api.seasons.ClimateZoneType;
import com.ferreusveritas.dynamictrees.api.seasons.ISeasonGrowthCalculator;

//Simply returns 1.0f for all values so it's as if there's no seasonal change
public class SeasonGrowthCalculatorNull implements ISeasonGrowthCalculator {
	
	public float calcGrowthRate(float seasonValue, ClimateZoneType type) {
		return 1.0f;
	}
	
	public float calcSeedDropRate(float seasonValue, ClimateZoneType type) {
		return 1.0f;
	}

	@Override
	public float calcFruitProduction(float seasonValue, ClimateZoneType type) {
		return 1.0f;
	}
	
}