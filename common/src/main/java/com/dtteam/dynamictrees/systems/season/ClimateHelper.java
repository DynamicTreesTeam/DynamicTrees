package com.dtteam.dynamictrees.systems.season;

import com.dtteam.dynamictrees.api.season.ClimateZoneType;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

public class ClimateHelper {

    private static final double[][] CLIMATE_MULTIPLIER_TABLE = {
            //biome:
            //NONE TEMP TROP ARID COLD
            { 1.0, 1.0, 1.0, 1.0, 1.0 }, // NONE
            { 1.0, 1.0, 0.6, 0.9, 0.4 }, // TEMPERATE species
            { 1.0, 0.7, 1.0, 0.6, 0.1 }, // TROPICAL species
            { 1.0, 0.6, 0.7, 1.0, 0.3 }, // ARID species
            { 1.0, 0.9, 0.4, 0.8, 1.0 }  // COLD species
    };

    public static double climateMultiplier(ClimateZoneType preferred, ClimateZoneType plantedIn, double minimum) {
        double realValue = CLIMATE_MULTIPLIER_TABLE[preferred.ordinal()][plantedIn.ordinal()];
        return realValue * (1-minimum) + minimum;
    }

    public static double climateMultiplier(Species species, ClimateZoneType plantedIn, double minimum) {
        return climateMultiplier(species.getPreferredClimate(), plantedIn, minimum);
    }

    static public ClimateZoneType getClimate(LevelAccessor level, BlockPos rootPos){
        return SeasonCompatibilityHandler.getSeasonManager().getClimate(level, rootPos);
    }

}
