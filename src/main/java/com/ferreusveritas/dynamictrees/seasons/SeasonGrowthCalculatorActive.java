package com.ferreusveritas.dynamictrees.seasons;

import com.ferreusveritas.dynamictrees.api.seasons.ClimateZoneType;
import com.ferreusveritas.dynamictrees.api.seasons.ISeasonGrowthCalculator;
import net.minecraft.util.math.MathHelper;

public class SeasonGrowthCalculatorActive implements ISeasonGrowthCalculator {

	protected float clippedsineWave(float seasonValue, float qPhase, float amplitude, float bias) {
		return MathHelper.clamp((((float) Math.sin((seasonValue / 2 * Math.PI) + (Math.PI * 0.25 * qPhase))) * amplitude) + bias, 0.0f, 1.0f);
	}

	@Override
	public float calcGrowthRate(Float seasonValue, ClimateZoneType type) {

		if (seasonValue == null) {
			return 1.0f;
		}

		switch (type) {
			case TEMPERATE:
				return clippedsineWave(seasonValue, 7, 0.8f, 1.0f);
			case TROPICAL:
				return clippedsineWave(seasonValue, 2, 0.31f, 0.9f);
			default:
				return 1.0f;
		}
	}

	@Override
	public float calcSeedDropRate(Float seasonValue, ClimateZoneType type) {

		if (seasonValue == null) {
			return 1.0f;
		}

		switch (type) {
			case TEMPERATE:
				return clippedsineWave(seasonValue, 5, 1.5f, -0.25f);
			case TROPICAL:
				return clippedsineWave(seasonValue, 7, 0.31f, 0.9f);
			default:
				return 1.0f;
		}
	}

	@Override
	public float calcFruitProduction(Float seasonValue, ClimateZoneType type) {

		if (seasonValue == null) {
			return 1.0f;
		}

		switch (type) {
			case TEMPERATE:
				return clippedsineWave(seasonValue, 7, 1.0f, 1.0f);
			case TROPICAL:
				return clippedsineWave(seasonValue, 1, 0.31f, 0.9f);
			default:
				return 1.0f;
		}
	}

}
