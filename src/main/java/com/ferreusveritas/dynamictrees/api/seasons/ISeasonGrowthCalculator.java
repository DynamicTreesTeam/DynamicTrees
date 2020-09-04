package com.ferreusveritas.dynamictrees.api.seasons;

/***
 * Provides a means to calculate temperate and tropical growth rate values
 * 
 * @author ferreusveritas
 */
public interface ISeasonGrowthCalculator {
	
	float calcGrowthRate(float seasonValue, ClimateZoneType type);
	
	float calcSeedDropRate(float seasonValue, ClimateZoneType type);
	
	float calcFruitProduction(float seasonValue, ClimateZoneType type);
	
}
