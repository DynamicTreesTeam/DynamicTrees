package com.ferreusveritas.dynamictrees.seasons;

import com.ferreusveritas.dynamictrees.api.seasons.ISeasonGrowthCalculator;

//Simply returns 1.0f for all values so it's as if there's no seasonal change
public class SeasonGrowthCalculatorNull implements ISeasonGrowthCalculator {
	
	public float calcTemperateGrowthRate(float seasonValue) {
		return 1.0f;
	}
	
	public float calcTropicalGrowthRate(float seasonValue) {
		return 1.0f;
	}
	
	public float calcSeedDropRate(float seasonValue) {
		return 1.0f;
	}
	
}