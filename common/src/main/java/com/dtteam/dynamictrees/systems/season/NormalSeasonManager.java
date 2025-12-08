package com.dtteam.dynamictrees.systems.season;

import com.dtteam.dynamictrees.api.season.ClimateZoneType;
import com.dtteam.dynamictrees.api.season.SeasonGrowthCalculator;
import com.dtteam.dynamictrees.api.season.SeasonManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;

public class NormalSeasonManager implements SeasonManager {

	public static final Supplier<SeasonManager> NULL = NormalSeasonManager::new;

    private final Map<ResourceLocation, SeasonContext> seasonContextMap = new HashMap<>();
    private Function<Level, Tuple<SeasonProvider, SeasonGrowthCalculator>> seasonMapper = w -> new Tuple<>(new NullSeasonProvider(), new NullSeasonGrowthCalculator());

    public NormalSeasonManager() {
    }

    public NormalSeasonManager(Function<Level, Tuple<SeasonProvider, SeasonGrowthCalculator>> seasonMapper) {
        this.seasonMapper = seasonMapper;
    }

    private Tuple<SeasonProvider, SeasonGrowthCalculator> createProvider(Level level) {
        return seasonMapper.apply(level);
    }

    private SeasonContext getContext(Level level) {
        return seasonContextMap.computeIfAbsent(level.dimension().location(), d -> {
            Tuple<SeasonProvider, SeasonGrowthCalculator> tuple = createProvider(level);
            return new SeasonContext(tuple.getA(), tuple.getB());
        });
    }

    public void flushMappings() {
        seasonContextMap.clear();
    }


    ////////////////////////////////////////////////////////////////
    // Climate Predicates
    ////////////////////////////////////////////////////////////////

    static private final float ARID_THRESHHOLD = 1.0f;
    static private final float ARID_DOWNFALL_THRESHHOLD = 0.0f;
    static private final float TROPICAL_THRESHHOLD = 0.8f; //Same threshold used by Serene Seasons.  Seems smart enough
    static private final float COLD_THRESHHOLD = 0.2f;

    private BiPredicate<Level, BlockPos> isTropical = (level, rootPos) -> level.getUncachedNoiseBiome(rootPos.getX() >> 2, rootPos.getY() >> 2, rootPos.getZ() >> 2).value().getBaseTemperature() > TROPICAL_THRESHHOLD;
    private BiPredicate<Level, BlockPos> isCold = (level, rootPos) -> level.getUncachedNoiseBiome(rootPos.getX() >> 2, rootPos.getY() >> 2, rootPos.getZ() >> 2).value().getBaseTemperature() < COLD_THRESHHOLD;
    private BiPredicate<Level, BlockPos> isArid = (level, rootPos) -> {
        Biome b = level.getUncachedNoiseBiome(rootPos.getX() >> 2, rootPos.getY() >> 2, rootPos.getZ() >> 2).value();
        return (b.getBaseTemperature() >= ARID_THRESHHOLD); // || b.climateSettings.downfall() <= ARID_DOWNFALL_THRESHHOLD
    };

    public void setTropicalPredicate(BiPredicate<Level, BlockPos> predicate) {
        isTropical = predicate;
    }
    public void setAridPredicate(BiPredicate<Level, BlockPos> predicate) {
        isArid = predicate;
    }
    public void setColdPredicate(BiPredicate<Level, BlockPos> predicate) {
        isCold = predicate;
    }

    /**
     * Predicates should return true if tropical, arid or cold, respectively.
     * If all fail the location is determined to be temperate.
     */
    public ClimateZoneType getClimate(Level level, BlockPos rootPos) {
        if (isCold.test(level, rootPos)) return ClimateZoneType.COLD;
        if (isArid.test(level, rootPos)) return ClimateZoneType.ARID;
        if (isTropical.test(level, rootPos)) return ClimateZoneType.TROPICAL;
        return ClimateZoneType.TEMPERATE;
    }

    ////////////////////////////////////////////////////////////////
    // ISeasonManager Interface
    ////////////////////////////////////////////////////////////////

    public void updateTick(Level level, long dayTime) {
        getContext(level).updateTick(level, dayTime);
    }

    public float getGrowthFactor(Level level, BlockPos rootPos, float offset) {
        SeasonContext context = getContext(level);
        ClimateZoneType climate = getClimate(level, rootPos);
        return context.getGrowthFactor(offset, climate);
    }

    public float getSeedDropFactor(Level level, BlockPos rootPos, float offset) {
        SeasonContext context = getContext(level);
        ClimateZoneType climate = getClimate(level, rootPos);
        return context.getSeedDropFactor(offset, climate);
    }

    @Override
    public float getFruitProductionFactor(Level level, BlockPos rootPos, float offset, boolean getAsScan) {
        if (getAsScan) {
            return getFruitProductionFactorAsScan(level.dimension().location(), rootPos, offset);
        }

        SeasonContext context = getContext(level);
        ClimateZoneType climate = getClimate(level, rootPos);
        return context.getFruitProductionFactor(offset, climate);
    }

    @Override
    public Float getSeasonValue(Level level, BlockPos pos) {
        return getContext(level).getSeasonProvider().getSeasonValue(level, pos);
    }

    @Override
    public Float getPeakFruitProductionSeasonValue(Level level, BlockPos rootPos, float offset) {
        SeasonContext context = getContext(level);
        ClimateZoneType climate = getClimate(level, rootPos);
        return context.getPeakFruitProductionSeasonValue(offset, climate);
    }

    @Override
    public boolean shouldSnowMelt(Level level, BlockPos pos) {
        return getContext(level).getSeasonProvider().shouldSnowMelt(level, pos);
    }

    public float getFruitProductionFactorAsScan(ResourceLocation dimLoc, BlockPos rootPos, float offset) {
        if (!seasonContextMap.isEmpty()) {
            float seasonValue = rootPos.getY() / 64.0f;
            boolean tropical = rootPos.getZ() >= 1.0f;
            if (seasonContextMap.containsKey(dimLoc)) {
                SeasonContext context = seasonContextMap.get(dimLoc);
                SeasonGrowthCalculator calculator = context.getCalculator();
                return calculator.calcFruitProductionRate(seasonValue + offset, tropical ? ClimateZoneType.TROPICAL : ClimateZoneType.TEMPERATE);
            }
        }
        return 0.0f;
    }

}
