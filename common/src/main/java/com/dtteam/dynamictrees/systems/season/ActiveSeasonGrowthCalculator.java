package com.dtteam.dynamictrees.systems.season;

import com.dtteam.dynamictrees.api.season.ClimateZoneType;
import com.dtteam.dynamictrees.api.season.SeasonGrowthCalculator;
import com.dtteam.dynamictrees.api.season.SeasonType;
import com.dtteam.dynamictrees.config.DTConfigs;
import net.minecraft.util.Mth;

public class ActiveSeasonGrowthCalculator implements SeasonGrowthCalculator {

    protected float clippedSineWave(float seasonValue, float qPhase, float amplitude, float bias) {
        return Mth.clamp((((float) Math.sin((seasonValue / 2 * Math.PI) + (Math.PI * 0.5 * qPhase))) * amplitude) + bias, 0.0f, 1.0f);
    }

    private float peakClimateOffset(SeasonType type){
        float summerOffset = -0.5f;
        float wetSeasonOffset = (float)(summerOffset + DTConfigs.COMMON.wetSeasonOffset.get());
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
            case TEMPERATE -> clippedSineWave(seasonValue, peakClimateOffset(SeasonType.TEMPERATURE), 1.0f, 0.3f);
            case TROPICAL -> clippedSineWave(seasonValue, peakClimateOffset(SeasonType.DRY_WET), 0.3f, 0.9f);
            case ARID -> clippedSineWave(seasonValue, peakClimateOffset(SeasonType.DRY_WET), 5f, -0.35f);
            case COLD -> clippedSineWave(seasonValue, peakClimateOffset(SeasonType.TEMPERATURE), 0.6f, 0.4f);
            default -> 1.0f;
        };
    }

    @Override
    public float calcFruitProductionRate(Float seasonValue, ClimateZoneType type) {

        if (seasonValue == null) {
            return 1.0f;
        }

        return switch (type) {
            case TEMPERATE -> clippedSineWave(seasonValue, peakClimateOffset(SeasonType.TEMPERATURE), 1.0f, 1.0f);
            case TROPICAL -> clippedSineWave(seasonValue, peakClimateOffset(SeasonType.DRY_WET), 0.3f, 0.9f);
            case ARID -> clippedSineWave(seasonValue, peakClimateOffset(SeasonType.DRY_WET), 1.0f, 0.7f);
            case COLD -> clippedSineWave(seasonValue, peakClimateOffset(SeasonType.TEMPERATURE), 2.0f, 0f);
            default -> 1.0f;
        };
    }

    @Override
    public Float getPeakFruitProductionSeasonValue(ClimateZoneType type) {
        return switch (type) {
            case TEMPERATE, COLD -> 2 + peakClimateOffset(SeasonType.TEMPERATURE);
            case TROPICAL, ARID -> 2 + peakClimateOffset(SeasonType.DRY_WET);
            default -> null;
        };
    }

}