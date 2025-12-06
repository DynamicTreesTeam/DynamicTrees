package com.dtteam.dynamictrees.systems.season;

import com.dtteam.dynamictrees.api.season.ClimateZoneType;
import com.dtteam.dynamictrees.api.season.SeasonGrowthCalculator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.function.BiFunction;

public class SeasonContext {
    private final SeasonProvider provider;
    private final SeasonGrowthCalculator calculator;
//    private HashMap<ClimateZoneType, HashMap<Integer,Float>> cachedGrowthFactors = new HashMap<>();
//    private HashMap<ClimateZoneType, HashMap<Integer,Float>> cachedSeedDropFactors = new HashMap<>();
//    private HashMap<ClimateZoneType, HashMap<Integer,Float>> cachedFruitProductionFactors = new HashMap<>();
    private Float seasonValue;

    private final HashMap<ClimateZoneType, Float> peakFruitSeasons = new HashMap<>();

    private long methodTicks = 0;

    private static final int updateRate = 20;
    private static final float seasonValueCacheMultiplier = 256;
    private static final float wetSeasonOffset = 2f;

    public SeasonContext(SeasonProvider provider, SeasonGrowthCalculator calculator) {
        this.provider = provider;
        this.calculator = calculator;
        for (ClimateZoneType climate : ClimateZoneType.values()){
            peakFruitSeasons.put(climate, calculator.getPeakFruitProductionSeasonValue(climate));
        }
    }

    public void updateTick(Level world, long dayTime) {

        if (methodTicks % updateRate == 0) {
            provider.updateTick(world, dayTime);
            seasonValue = provider.getSeasonValue(world, BlockPos.ZERO);

//            for (ClimateZoneType climate : ClimateZoneType.values()) {
//                updateCache(cachedGrowthFactors, calculator::calcGrowthRate, climate, seasonValue);
//                updateCache(cachedSeedDropFactors, calculator::calcSeedDropRate, climate, seasonValue);
//                updateCache(cachedFruitProductionFactors, calculator::calcFruitProductionRate, climate, seasonValue);
//            }
        }

        methodTicks++;
    }

//    private void updateCache (HashMap<ClimateZoneType, HashMap<Integer,Float>> cache, BiFunction<Float, ClimateZoneType, Float> calculate, ClimateZoneType climate, float seasonVal){
//        seasonVal %= 4;
//        HashMap<Integer, Float> values = cache.getOrDefault(climate, null);
//        if (values == null){
//            cache.put(climate, new HashMap<>());
//            values = cache.get(climate);
//        }
//        values.put((int)(seasonVal * seasonValueCacheMultiplier), calculate.apply(seasonVal, climate));
//    }

    public SeasonProvider getSeasonProvider() {
        return provider;
    }

    public SeasonGrowthCalculator getCalculator() {
        return calculator;
    }

    public float getGrowthFactor(float offset, ClimateZoneType climate) {
        if (seasonValue == null || climate == ClimateZoneType.NONE) return 1;
        return calculator.calcGrowthRate(seasonValue + offset, climate);
    }

    public float getSeedDropFactor(float offset, ClimateZoneType climate) {
        if (seasonValue == null || climate == ClimateZoneType.NONE) return 1;
        return calculator.calcSeedDropRate(seasonValue + offset, climate);
    }

    public float getFruitProductionFactor(float offset, ClimateZoneType climate) {
        if (seasonValue == null || climate == ClimateZoneType.NONE) return 1;
        return calculator.calcFruitProductionRate(seasonValue + offset, climate);
    }

    public Float getPeakFruitProductionSeasonValue(float offset, ClimateZoneType climate) {
        if (seasonValue == null || climate == ClimateZoneType.NONE) return null;
        return peakFruitSeasons.get(climate) + offset;
    }

}
