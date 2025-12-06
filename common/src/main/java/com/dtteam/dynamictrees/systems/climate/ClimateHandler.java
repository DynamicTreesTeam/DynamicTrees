package com.dtteam.dynamictrees.systems.climate;

import com.dtteam.dynamictrees.api.season.ClimateZoneType;
import com.dtteam.dynamictrees.api.season.SeasonType;

public class ClimateHandler {

    private static final double[][] CLIMATE_MULTIPLIER_TABLE = {
            //biome:
            //TEMP   TROP   ARID   COLD
            { 1.0,    0.6,   0.9,   0.4 }, // TEMPERATE species
            { 0.7,    1.0,   0.5,   0.1 }, // TROPICAL species
            { 0.6,    0.7,   1.0,   0.3 }, // ARID species
            { 0.9,    0.4,   0.8,   1.0 }  // COLD species
    };

    public static double climateMultiplier(ClimateZoneType preferred, ClimateZoneType plantedIn, double minimum) {
        double realValue = CLIMATE_MULTIPLIER_TABLE[preferred.ordinal()][plantedIn.ordinal()];
        return realValue * (1-minimum) + minimum;
    }

    /**
     * For climates with regular seasons (temperate, cold) the offset is relative to summer (0.0 is summer).
     * For climate with dry-wet seasons (tropical, arid) the offsets is relative to wet season (0.0 is wet season).
     * Summer and wet season my not align.
     */
    public Float defaultClimateFruitOffsets(ClimateZoneType preferredTreeClimate, ClimateZoneType biomeClimate, Float preferredClimateOffset){
        if (preferredTreeClimate == biomeClimate){
            return preferredClimateOffset;
        }
        if (preferredTreeClimate.seasonType == SeasonType.DRY_WET && biomeClimate.seasonType == SeasonType.DRY_WET){
            return preferredClimateOffset;
        }
        return 0.0f;
    }

    /**
     * For climates with regular seasons (temperate, cold) the offset is relative to summer (0.0 is summer).
     * For climate with dry-wet seasons (tropical, arid) the offsets is relative to wet season (0.0 is wet season).
     * Summer and wet season my not align.
     */
    public Float defaultClimateSeedOffsets(ClimateZoneType preferredTreeClimate, ClimateZoneType biomeClimate, Float preferredClimateOffset){
        if (preferredTreeClimate == biomeClimate){
            return preferredClimateOffset;
        }
        if (preferredTreeClimate.seasonType == SeasonType.DRY_WET && biomeClimate.seasonType == SeasonType.DRY_WET){
            return preferredClimateOffset;
        }
        return 1.0f;
    }

}
