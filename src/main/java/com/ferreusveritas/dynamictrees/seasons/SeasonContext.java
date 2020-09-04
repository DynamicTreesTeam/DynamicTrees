package com.ferreusveritas.dynamictrees.seasons;

import com.ferreusveritas.dynamictrees.api.seasons.ClimateZoneType;

import net.minecraft.world.World;

public class SeasonContext {
	private ISeasonProvider provider;
	private SeasonGrowthCalculatorActive calculator;
	private float temperateGrowthFactor;
	private float tropicalGrowthFactor;
	private float temperateSeedDropFactor;
	private float tropicalSeedDropFactor;
	private float temperateFruitProductionFactor;
	private float tropicalFruitProductionFactor;
	
	public SeasonContext(ISeasonProvider provider, SeasonGrowthCalculatorActive calculator) {
		this.provider = provider;
		this.calculator = calculator;
	}
	
	public void updateTick(World world, long worldTicks) {
		
		if(worldTicks % 20 == 0) {
			provider.updateTick(world, worldTicks);
			float seasonValue = provider.getSeasonValue(world);
			temperateGrowthFactor = calculator.calcGrowthRate(seasonValue, ClimateZoneType.TEMPERATE);
			tropicalGrowthFactor = calculator.calcGrowthRate(seasonValue, ClimateZoneType.TROPICAL);
			temperateSeedDropFactor = calculator.calcSeedDropRate(seasonValue, ClimateZoneType.TEMPERATE);
			tropicalSeedDropFactor = calculator.calcSeedDropRate(seasonValue, ClimateZoneType.TROPICAL);
			temperateFruitProductionFactor = calculator.calcFruitProduction(seasonValue, ClimateZoneType.TEMPERATE);
			tropicalFruitProductionFactor = calculator.calcFruitProduction(seasonValue, ClimateZoneType.TROPICAL);
		}
	}
	
	public ISeasonProvider getSeasonProvider() {
		return provider;
	}
	
	public float getTemperateGrowthFactor() {
		return temperateGrowthFactor;
	}
	
	public float getTropicalGrowthFactor() {
		return tropicalGrowthFactor;
	}
	
	public float getTemperateSeedDropFactor() {
		return temperateSeedDropFactor;
	}
	
	public float getTropicalSeedDropFactor() {
		return tropicalSeedDropFactor;
	}
	
	public float getTemperateFruitProductionFactor() {
		return temperateFruitProductionFactor;
	}
	
	public float getTropicalFruitProductionFactor() {
		return tropicalFruitProductionFactor;
	}
	
}