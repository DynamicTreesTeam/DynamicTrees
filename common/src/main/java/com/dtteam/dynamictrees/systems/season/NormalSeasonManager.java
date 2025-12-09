package com.dtteam.dynamictrees.systems.season;

import com.dtteam.dynamictrees.api.season.ClimateZoneType;
import com.dtteam.dynamictrees.api.season.SeasonGrowthCalculator;
import com.dtteam.dynamictrees.api.season.SeasonManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
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

    private BiPredicate<LevelAccessor, BlockPos> isTropical = (level, rootPos) -> getBiomeForDist(level, rootPos).getBaseTemperature() > TROPICAL_THRESHHOLD;
    private BiPredicate<LevelAccessor, BlockPos> isCold = (level, rootPos) -> getBiomeForDist(level, rootPos).getBaseTemperature() < COLD_THRESHHOLD;
    private BiPredicate<LevelAccessor, BlockPos> isArid = (level, rootPos) -> {
        Biome b = getBiomeForDist(level, rootPos);
        return (b.getBaseTemperature() >= ARID_THRESHHOLD); // || b.climateSettings.downfall() <= ARID_DOWNFALL_THRESHHOLD
    };

    private static Biome getBiomeForDist(LevelAccessor level, BlockPos pos){
        if (level instanceof Level){ //Worldgen worlds are not Level, which would crash with getBiome
            return level.getBiome(pos).value();
        }
        return level.getUncachedNoiseBiome(pos.getX() >> 2, pos.getY() >> 2, pos.getZ() >> 2).value();
    }

    public void setTropicalPredicate(BiPredicate<LevelAccessor, BlockPos> predicate) {
        isTropical = predicate;
    }
    public void setAridPredicate(BiPredicate<LevelAccessor, BlockPos> predicate) {
        isArid = predicate;
    }
    public void setColdPredicate(BiPredicate<LevelAccessor, BlockPos> predicate) {
        isCold = predicate;
    }

    /**
     * Predicates should return true if tropical, arid or cold, respectively.
     * If all fail the location is determined to be temperate.
     */
    public ClimateZoneType getClimate(LevelAccessor level, BlockPos rootPos) {
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
        ClimateZoneType climate = getClimate(level, rootPos);
        if (getAsScan) {
            return getFruitProductionFactorAsScan(level.dimension().location(), climate, offset);
        }

        SeasonContext context = getContext(level);
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

    public float getFruitProductionFactorAsScan(ResourceLocation dimLoc, ClimateZoneType climate, float seasonValue) {
        if (!seasonContextMap.isEmpty()) {
            if (seasonContextMap.containsKey(dimLoc)) {
                SeasonContext context = seasonContextMap.get(dimLoc);
                SeasonGrowthCalculator calculator = context.getCalculator();
                return calculator.calcFruitProductionRate(seasonValue, climate);
            }
        }
        return 0.0f;
    }

}
