package com.dtteam.dynamictrees.systems.season;

import com.dtteam.dynamictrees.api.season.ClimateZoneType;
import com.dtteam.dynamictrees.api.season.SeasonGrowthCalculator;
import com.dtteam.dynamictrees.api.season.SeasonProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.HashMap;

public class SeasonContext {
    private final SeasonProvider provider;
    private final SeasonGrowthCalculator calculator;
    private final HashMap<ClimateZoneType, HashMap<Integer,Float>> cachedGrowthFactors = new HashMap<>();
    private final HashMap<ClimateZoneType, HashMap<Integer,Float>> cachedSeedDropFactors = new HashMap<>();
    private final HashMap<ClimateZoneType, HashMap<Integer,Float>> cachedFruitProductionFactors = new HashMap<>();
    private Float seasonValue;

    private final HashMap<ClimateZoneType, Float> peakFruitSeasons = new HashMap<>();

    private long methodTicks = 0;

    private static final int updateRate = 20;
    private static final float seasonValueCacheMultiplier = 256;

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
            seasonValue = provider.getLocationalSeasonValue(world, BlockPos.ZERO);
        }

        methodTicks++;
    }

    public SeasonProvider getSeasonProvider() {
        return provider;
    }

    public SeasonGrowthCalculator getCalculator() {
        return calculator;
    }

    public float getGrowthFactor(float offset, ClimateZoneType climate) {
        if (seasonValue == null || climate == ClimateZoneType.NONE) return 1;
        float season = (seasonValue + offset) % 4.0f;
        int key = (int)(season * seasonValueCacheMultiplier);

        HashMap<Integer, Float> values = cachedGrowthFactors.computeIfAbsent(climate, k -> new HashMap<>());
        if (!values.containsKey(key)){
            float calculated = calculator.calcGrowthRate(season, climate);
            values.put(key, calculated);
        }
        return values.get(key);
    }

    public float getSeedDropFactor(float offset, ClimateZoneType climate) {
        if (seasonValue == null || climate == ClimateZoneType.NONE) return 1;
        float season = (seasonValue + offset) % 4.0f;
        int key = (int)(season * seasonValueCacheMultiplier);

        HashMap<Integer, Float> values = cachedSeedDropFactors.computeIfAbsent(climate, k -> new HashMap<>());
        if (!values.containsKey(key)){
            float calculated = calculator.calcSeedDropRate(season, climate);
            values.put(key, calculated);
        }
        return values.get(key);
    }

    public float getFruitProductionFactor(float offset, ClimateZoneType climate) {
        if (seasonValue == null || climate == ClimateZoneType.NONE) return 1;
        float season = (seasonValue + offset) % 4.0f;
        int key = (int)(season * seasonValueCacheMultiplier);

        HashMap<Integer, Float> values = cachedFruitProductionFactors.computeIfAbsent(climate, k -> new HashMap<>());
        if (!values.containsKey(key)){
            float calculated = calculator.calcFruitProductionRate(season, climate);
            values.put(key, calculated);
        }
        return values.get(key);
    }

    public Float getPeakFruitProductionSeasonValue(float offset, ClimateZoneType climate) {
        if (seasonValue == null || climate == ClimateZoneType.NONE) return null;
        return (peakFruitSeasons.get(climate) + offset) % 4.0f;
    }

    public void clearCache(){
        cachedGrowthFactors.clear();
        cachedSeedDropFactors.clear();
        cachedFruitProductionFactors.clear();
        peakFruitSeasons.clear();
    }

}
