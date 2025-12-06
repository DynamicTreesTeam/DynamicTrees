package com.dtteam.dynamictrees.systems.climate;

import com.dtteam.dynamictrees.api.season.ClimateZoneType;

public class ClimateHandler {

    public float offClimateMultipliers(ClimateZoneType preferredTreeClimate, ClimateZoneType biomeClimate){
        if (preferredTreeClimate == ClimateZoneType.NONE || biomeClimate == ClimateZoneType.NONE) return 1.0f;
        return switch (preferredTreeClimate){
            case TEMPERATE -> switch (biomeClimate){
                case COLD, ARID -> 0.75f;
                default -> 1.0f;
            };
            case TROPICAL -> switch (biomeClimate){
                case ARID -> 0.75F;
                case COLD -> 0.0F;
                default -> 1.0f;
            };
            case ARID -> switch (biomeClimate){
                case TROPICAL -> 0.0F;
                case COLD -> 0.75F;
                default -> 1.0f;
            };
            case COLD -> switch (biomeClimate){
                case TROPICAL -> 0.0F;
                case ARID -> 0.75F;
                default -> 1.0f;
            };
            default -> 1.0f;
        };
    }

//    public float offClimateFruitOffsets(ClimateZoneType preferredTreeClimate, ClimateZoneType biomeClimate){
//
//    }

}
