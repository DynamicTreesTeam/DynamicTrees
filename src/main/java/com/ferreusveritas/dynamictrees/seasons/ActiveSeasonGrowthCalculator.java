package com.ferreusveritas.dynamictrees.seasons;

import com.ferreusveritas.dynamictrees.api.seasons.ISeasonGrowthCalculator;

import net.minecraft.util.math.MathHelper;

public class ActiveSeasonGrowthCalculator implements ISeasonGrowthCalculator {
	
	@Override
	public float calcTemperateGrowthRate(float seasonValue) {
		return MathHelper.clamp(((((float)Math.sin(seasonValue / 2 * Math.PI) + 1) / 2f) * 1.75f) - 0.5f, 0.0f, 1.0f);
	}
	
	@Override
	public float calcTropicalGrowthRate(float seasonValue) {
		return 1.0f;//TODO
	}
	
	@Override
	public float calcSeedDropRate(float seasonValue) {
		return 1.0f;//TODO
	}
	
}