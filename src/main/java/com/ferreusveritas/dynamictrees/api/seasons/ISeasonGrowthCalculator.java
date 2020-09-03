package com.ferreusveritas.dynamictrees.api.seasons;

/***
 * Provides a means to calculate temperate and tropical growth rate values
 * 
 * @author ferreusveritas
 */
public interface ISeasonGrowthCalculator {
	
	float calcTemperateGrowthRate(float seasonValue);
	
	float calcTropicalGrowthRate(float seasonValue);
	
	float calcSeedDropRate(float seasonValue);
	
}
