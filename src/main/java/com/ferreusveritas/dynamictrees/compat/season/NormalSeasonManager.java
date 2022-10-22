package com.ferreusveritas.dynamictrees.compat.season;

import com.ferreusveritas.dynamictrees.api.seasons.ClimateZoneType;
import com.ferreusveritas.dynamictrees.api.seasons.SeasonGrowthCalculator;
import com.ferreusveritas.dynamictrees.api.seasons.SeasonManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;

public class NormalSeasonManager implements SeasonManager {

	public static final Supplier<NormalSeasonManager> NULL = NormalSeasonManager::new;

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
    // Tropical Predicate
    ////////////////////////////////////////////////////////////////

    static private final float TROPICAL_THRESHHOLD = 0.8f; //Same threshold used by Serene Seasons.  Seems smart enough

    private BiPredicate<Level, BlockPos> isTropical = (level, rootPos) -> level.getUncachedNoiseBiome(rootPos.getX() >> 2, rootPos.getY() >> 2, rootPos.getZ() >> 2).value().getBaseTemperature() > TROPICAL_THRESHHOLD;

    /**
     * Set the global predicate that determines if a world location is tropical. Predicate should return true if
     * tropical, false if temperate.
     */
    public void setTropicalPredicate(BiPredicate<Level, BlockPos> predicate) {
        isTropical = predicate;
    }

    public boolean isTropical(Level level, BlockPos rootPos) {
        return isTropical.test(level, rootPos);
    }


    ////////////////////////////////////////////////////////////////
    // ISeasonManager Interface
    ////////////////////////////////////////////////////////////////

    public void updateTick(Level level, long dayTime) {
        getContext(level).updateTick(level, dayTime);
    }

    public float getGrowthFactor(Level level, BlockPos rootPos, float offset) {
        SeasonContext context = getContext(level);
        return isTropical(level, rootPos) ? context.getTropicalGrowthFactor(offset) : context.getTemperateGrowthFactor(offset);
    }

    public float getSeedDropFactor(Level level, BlockPos rootPos, float offset) {
        SeasonContext context = getContext(level);
        return isTropical(level, rootPos) ? context.getTropicalSeedDropFactor(offset) : context.getTemperateSeedDropFactor(offset);
    }

    @Override
    public float getFruitProductionFactor(Level level, BlockPos rootPos, float offset, boolean getAsScan) {
        if (getAsScan) {
            return getFruitProductionFactorAsScan(level.dimension().location(), rootPos, offset);
        }

        SeasonContext context = getContext(level);
        return isTropical(level, rootPos) ? context.getTropicalFruitProductionFactor(offset) : context.getTemperateFruitProductionFactor(offset);
    }

    @Override
    public Float getSeasonValue(Level level, BlockPos pos) {
        return getContext(level).getSeasonProvider().getSeasonValue(level, pos);
    }

    @Override
    public Float getPeakFruitProductionSeasonValue(Level level, BlockPos rootPos, float offset) {
        SeasonContext context = getContext(level);
        return isTropical(level, rootPos) ? context.getTropicalPeakFruitProductionSeasonValue(offset) : context.getTemperatePeakFruitProductionSeasonValue(offset);
    }

    @Override
    public boolean shouldSnowMelt(Level level, BlockPos pos) {
        return getContext(level).getSeasonProvider().shouldSnowMelt(level, pos);
    }

    public float getFruitProductionFactorAsScan(ResourceLocation dimLoc, BlockPos rootPos, float offset) {
        if (seasonContextMap.size() > 0) {
            float seasonValue = rootPos.getY() / 64.0f;
            boolean tropical = rootPos.getZ() >= 1.0f;
            if (seasonContextMap.containsKey(dimLoc)) {
                SeasonContext context = seasonContextMap.get(dimLoc);
                SeasonGrowthCalculator calculator = context.getCalculator();
                return calculator.calcFruitProduction(seasonValue + offset, tropical ? ClimateZoneType.TROPICAL : ClimateZoneType.TEMPERATE);
            }
        }
        return 0.0f;
    }

}
