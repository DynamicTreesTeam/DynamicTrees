package com.ferreusveritas.dynamictrees.seasons;

import com.ferreusveritas.dynamictrees.api.seasons.ISeasonGrowthCalculator;

import net.minecraft.util.math.MathHelper;

public class SeasonGrowthCalculatorActive implements ISeasonGrowthCalculator {
	
	@Override
	public float calcTemperateGrowthRate(float seasonValue) {
		return MathHelper.clamp((((float)Math.sin((seasonValue / 2 * Math.PI) + (Math.PI * 0.25f * 7)) + 1) * 0.8f) + 0.2f, 0.0f, 1.0f);
	}
	
	@Override
	public float calcTropicalGrowthRate(float seasonValue) {
		return (((float)Math.sin((seasonValue / 2 * Math.PI) + (Math.PI * 0.25 * 2)) + 1) * 0.31f) + 0.61f;
	}
	
	@Override
	public float calcSeedDropRate(float seasonValue) {
		return 1.0f;//TODO
	}
	
}