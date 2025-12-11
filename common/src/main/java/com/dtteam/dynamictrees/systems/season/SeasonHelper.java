package com.dtteam.dynamictrees.systems.season;

import com.dtteam.dynamictrees.api.season.ClimateZoneType;
import com.dtteam.dynamictrees.api.season.SeasonManager;
import com.dtteam.dynamictrees.api.worldgen.LevelContext;
import com.dtteam.dynamictrees.platform.Services;
import com.dtteam.dynamictrees.platform.services.IConfigHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import java.util.function.Supplier;

public class SeasonHelper {

    // A season provider returns a float value from 0(Inclusive) to 4(Exclusive) that signifies one of the four classic seasons.
    // A whole number is the beginning of a season with #.5 being the middle of the season.
    public static final float SPRING_START = 0.0f;
    public static final float SPRING_MIDDLE = 0.5f;
    public static final float SUMMER_START = 1.0f;
    public static final float SUMMER_MIDDLE = 1.5f;
    public static final float AUTUMN_START = 2.0f;
    public static final float AUTUMN_MIDDLE = 2.5f;
    public static final float WINTER_START = 3.0f;
    public static final float WINTER_MIDDLE = 3.5f;

    static public void updateTick(Level level, long dayTime) {
        SeasonCompatibilityHandler.getSeasonManager().updateTick(level, dayTime);
    }

    static public float globalSeasonalGrowthFactor(LevelContext levelContext, BlockPos rootPos, float offset) {
        return Services.CONFIG.getBoolConfig(IConfigHelper.ENABLE_SEASONAL_GROWTH)
                ? SeasonCompatibilityHandler.getSeasonManager().getGrowthFactor(levelContext.level(), rootPos, offset) : 1.0F;
    }

    static public float globalSeasonalSeedDropFactor(LevelContext levelContext, BlockPos pos, float offset) {
        return Services.CONFIG.getBoolConfig(IConfigHelper.ENABLE_SEASONAL_SEED_DROP)
                ? SeasonCompatibilityHandler.getSeasonManager().getSeedDropFactor(levelContext.level(), pos, offset) : 1.0F;
    }

    static public float globalSeasonalFruitProductionFactor(LevelContext levelContext, BlockPos pos, float offset) {
        return Services.CONFIG.getBoolConfig(IConfigHelper.ENABLE_SEASONAL_SEED_FRUIT_PRODUCTION)
                ? SeasonCompatibilityHandler.getSeasonManager().getFruitProductionFactor(levelContext.level(), pos, offset) : 1.0F;
    }

    static public Float getPeakFruitProductionSeason(LevelContext levelContext, BlockPos pos, float offset) {
        return Services.CONFIG.getBoolConfig(IConfigHelper.ENABLE_SEASONAL_SEED_FRUIT_PRODUCTION)
                ? SeasonCompatibilityHandler.getSeasonManager().getPeakFruitProductionSeasonValue(levelContext.level(), pos, offset) : null;
    }

    /**
     * @return season value 0.0(Early Spring, Inclusive) -> 4.0(Later Winter, Exclusive) or null if there's no seasons
     * in the world.
     */
    static public Float getSeasonValue(LevelContext levelContext, BlockPos pos) {
        return SeasonCompatibilityHandler.getSeasonManager().getSeasonValue(levelContext.level(), pos);
    }

    /**
     * Test if the season value falls between two seasonal points. Wraps around the Spring/Winter point(0) if seasonA >
     * seasonB;
     *
     * @param testValue value to test
     * @param SeasonA   chronological season boundary beginning
     * @param SeasonB   chronological season boundary ending
     * @return
     */
    static public boolean isSeasonBetween(Float testValue, float SeasonA, float SeasonB) {
        testValue %= 4.0f;
        SeasonA %= 4.0f;
        SeasonB %= 4.0f;

        if (SeasonA <= SeasonB) {
            return testValue > SeasonA && testValue < SeasonB; //Simply between point A and B(inside)
        } else {
            return testValue < SeasonB || testValue > SeasonA; //The test wraps around the zero point(outside)
        }

    }

    static public boolean shouldSnowMelt(Level level, BlockPos pos) {
        return SeasonCompatibilityHandler.getSeasonManager().shouldSnowMelt(level, pos);
    }

}
