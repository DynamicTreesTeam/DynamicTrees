package com.ferreusveritas.dynamictrees.seasons;

import net.minecraft.world.World;

public class SeasonContext {
	private ISeasonProvider provider;
	private ActiveSeasonGrowthCalculator calculator;
	private float temperateRate;
	private float tropicalRate;
	private float seedDropRate;
	
	public SeasonContext(ISeasonProvider provider, ActiveSeasonGrowthCalculator calculator) {
		this.provider = provider;
		this.calculator = calculator;
	}
	
	public void updateTick(World world, long worldTicks) {
		if(worldTicks % 20 == 0) {
			provider.updateTick(world, worldTicks);
			float seasonValue = provider.getSeasonValue(world);
			temperateRate = calculator.calcTemperateGrowthRate(seasonValue);
			tropicalRate = calculator.calcTropicalGrowthRate(seasonValue);
			seedDropRate = calculator.calcSeedDropRate(seasonValue);
		}
	}
	
	public ISeasonProvider getSeasonProvider() {
		return provider;
	}
	
	public float getTemperateValue() {
		return temperateRate;
	}
	
	public float getTropicalValue() {
		return tropicalRate;
	}
	
	public float getSeedDropRate() {
		return seedDropRate;
	}
	
}