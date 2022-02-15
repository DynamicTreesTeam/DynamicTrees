package com.ferreusveritas.dynamictrees.compat.seasons;

import com.ferreusveritas.dynamictrees.api.seasons.SeasonManager;
import com.ferreusveritas.dynamictrees.compat.CompatHandler;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.Supplier;

public class SeasonHelper {

    // A season provider returns a float value from 0(Inclusive) to 4(Exclusive) that signifies one of the four classic seasons.
    // A whole number is the beginning of a season with #.5 being the middle of the season.
    public static final float SPRING = 0.0f;
    public static final float SUMMER = 1.0f;
    public static final float AUTUMN = 2.0f;
    public static final float WINTER = 3.0f;

    public static final float FULL_SEASON = 1.0F;
    public static final float HALF_SEASON = 1.0F;

    // Tropical convenience values.
    public static final float DRY = SUMMER;
    public static final float WET = WINTER;

    private static SeasonManager seasonManager = NormalSeasonManager.NULL.get();

    static public SeasonManager getSeasonManager() {
        return seasonManager;
    }

    /**
     * Maybe you don't like the global function season function.  Fine, do it all yourself then!
     *
     * <p>Add-ons should not use this method! {@link CompatHandler#registerSeasonManager(String, Supplier)}
     * should be used to register a season manager for a corresponding mod to respect the preferred season mod
     * configuration option.</p>
     */
    static public void setSeasonManager(SeasonManager manager) {
        seasonManager = manager;
    }

    static public void updateTick(World world, long worldTicks) {
        seasonManager.updateTick(world, worldTicks);
    }

    static public float globalSeasonalGrowthFactor(World world, BlockPos rootPos) {
        return globalSeasonalGrowthFactor(world, rootPos, 0);
    }

    static public float globalSeasonalGrowthFactor(World world, BlockPos rootPos, float offset) {
        return DTConfigs.ENABLE_SEASONAL_GROWTH_FACTOR.get() ?
                seasonManager.getGrowthFactor(world, rootPos, offset) : 1.0F;
    }

    static public float globalSeasonalSeedDropFactor(World world, BlockPos pos) {
        return globalSeasonalSeedDropFactor(world, pos, 0);
    }

    static public float globalSeasonalSeedDropFactor(World world, BlockPos pos, float offset) {
        return DTConfigs.ENABLE_SEASONAL_SEED_DROP_FACTOR.get() ?
                seasonManager.getSeedDropFactor(world, pos, offset) : 1.0F;
    }

    static public float globalSeasonalFruitProductionFactor(World world, BlockPos pos, boolean getAsScan) {
        return globalSeasonalFruitProductionFactor(world, pos, 0F, getAsScan);
    }

    static public float globalSeasonalFruitProductionFactor(World world, BlockPos pos, float offset, boolean getAsScan) {
        return DTConfigs.ENABLE_SEASONAL_FRUIT_PRODUCTION_FACTOR.get() ?
                seasonManager.getFruitProductionFactor(world, pos, offset, getAsScan) : 1.0F;
    }

    /**
     * @param world The world
     * @return season value 0.0(Early Spring, Inclusive) -> 4.0(Later Winter, Exclusive) or null if there's no seasons
     * in the world.
     */
    static public Float getSeasonValue(World world, BlockPos pos) {
        return seasonManager.getSeasonValue(world, pos);
    }

    /**
     * Tests if the position in world is considered tropical and thus follows tropical season rules.
     *
     * @param world
     * @param pos
     * @return
     */
    static public boolean isTropical(World world, BlockPos pos) {
        return seasonManager.isTropical(world, pos);
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

    static public boolean shouldSnowMelt(World world, BlockPos pos) {
        return seasonManager.shouldSnowMelt(world, pos);
    }

}
