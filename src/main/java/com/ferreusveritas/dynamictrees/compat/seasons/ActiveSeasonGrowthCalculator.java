package com.ferreusveritas.dynamictrees.compat.seasons;

import com.ferreusveritas.dynamictrees.api.seasons.ClimateZoneType;
import com.ferreusveritas.dynamictrees.api.seasons.SeasonGrowthCalculator;
import net.minecraft.util.Mth;

public class ActiveSeasonGrowthCalculator implements SeasonGrowthCalculator {

    protected float clippedsineWave(float seasonValue, float qPhase, float amplitude, float bias) {
        return Mth.clamp((((float) Math.sin((seasonValue / 2 * Math.PI) + (Math.PI * 0.25 * qPhase))) * amplitude) + bias, 0.0f, 1.0f);
    }

    @Override
    public float calcGrowthRate(Float seasonValue, ClimateZoneType type) {

        if (seasonValue == null) {
            return 1.0f;
        }

        return switch (type) {
            case TEMPERATE -> clippedsineWave(seasonValue, 7, 0.8f, 1.0f);
            case TROPICAL -> clippedsineWave(seasonValue, 2, 0.31f, 0.9f);
            default -> 1.0f;
        };
    }

    @Override
    public float calcSeedDropRate(Float seasonValue, ClimateZoneType type) {

        if (seasonValue == null) {
            return 1.0f;
        }

        return switch (type) {
            case TEMPERATE -> clippedsineWave(seasonValue, 5, 1.5f, -0.25f);
            case TROPICAL -> clippedsineWave(seasonValue, 7, 0.31f, 0.9f);
            default -> 1.0f;
        };
    }

    @Override
    public float calcFruitProduction(Float seasonValue, ClimateZoneType type) {

        if (seasonValue == null) {
            return 1.0f;
        }

        return switch (type) {
            case TEMPERATE -> clippedsineWave(seasonValue, 7, 1.0f, 1.0f);
            case TROPICAL -> clippedsineWave(seasonValue, 1, 0.31f, 0.9f);
            default -> 1.0f;
        };
    }

    @Override
    public Float getPeakFruitProductionSeasonValue(ClimateZoneType type) {
        return switch (type) {
            case TEMPERATE -> 1.5F;
            case TROPICAL -> 0.5F;
            default -> null;
        };
    }

}