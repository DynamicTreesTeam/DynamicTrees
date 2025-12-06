package com.dtteam.dynamictrees.systems.season;

import com.dtteam.dynamictrees.api.season.ClimateZoneType;
import com.dtteam.dynamictrees.api.season.SeasonGrowthCalculator;
import com.dtteam.dynamictrees.api.season.SeasonType;
import net.minecraft.util.Mth;

public class ActiveSeasonGrowthCalculator implements SeasonGrowthCalculator {

    protected float clippedSineWave(float seasonValue, float qPhase, float amplitude, float bias) {
        return Mth.clamp((((float) Math.sin((seasonValue / 2 * Math.PI) + (Math.PI * 0.5 * qPhase))) * amplitude) + bias, 0.0f, 1.0f);
    }

    private float peakClimateOffset(SeasonType type){
        float summerOffset = -0.5f;
        float wetSeasonOffset = summerOffset + 1.5f;
        if (type == SeasonType.DRY_WET){
            return wetSeasonOffset;
        } else return summerOffset;
    }

    @Override
    public float calcGrowthRate(Float seasonValue, ClimateZoneType type) {

        if (seasonValue == null) {
            return 1.0f;
        }

        return switch (type) {
            case TEMPERATE -> clippedSineWave(seasonValue, peakClimateOffset(SeasonType.TEMPERATURE), 1.0f, 1.0f);
            case TROPICAL -> clippedSineWave(seasonValue,  peakClimateOffset(SeasonType.DRY_WET), 0.3f, 0.9f);
            case ARID -> clippedSineWave(seasonValue,  peakClimateOffset(SeasonType.DRY_WET), 1.0f, 0.0f);
            case COLD -> clippedSineWave(seasonValue,  peakClimateOffset(SeasonType.TEMPERATURE), 2.0f, 0.0f);
            default -> 1.0f;
        };
    }

    @Override
    public float calcSeedDropRate(Float seasonValue, ClimateZoneType type) {

        if (seasonValue == null) {
            return 1.0f;
        }

        return switch (type) {
            case TEMPERATE -> clippedSineWave(seasonValue, 2.5f, 1.5f, -0.25f);
            case TROPICAL -> clippedSineWave(seasonValue, 3.5f, 0.3f, 0.9f);
            default -> 1.0f;
        };
    }

    @Override
    public float calcFruitProductionRate(Float seasonValue, ClimateZoneType type) {

        if (seasonValue == null) {
            return 1.0f;
        }

        return switch (type) {
            case TEMPERATE -> clippedSineWave(seasonValue, 3.5f, 1.0f, 1.0f);
            case TROPICAL -> clippedSineWave(seasonValue, 0.5f, 0.3f, 0.9f);
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