package com.dtteam.dynamictrees.compat;

import com.dtteam.dynamictrees.api.season.SeasonProvider;
import com.dtteam.dynamictrees.systems.season.*;
import com.teamtea.eclipticseasons.EclipticSeasons;
import com.teamtea.eclipticseasons.common.core.SolarHolders;
import com.teamtea.eclipticseasons.common.core.solar.SolarDataManager;
import com.teamtea.eclipticseasons.config.CommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class EclipticSeasonsProvider implements SeasonProvider {

    private float seasonValue = 1.0f;

    @Override
    public Float getSeasonValue() {
        return seasonValue;
    }

    @Override
    public void updateTick(Level level, long dayTime) {
        Optional<SolarDataManager> solarDataManager = SolarHolders.getSaveDataLazy(level);
        solarDataManager.ifPresent(dataManager ->
                seasonValue = dataManager.getSolarTermsDay() / (6f * CommonConfig.Season.lastingDaysOfEachTerm.get())
        );
    }

    @Override
    public boolean shouldSnowMelt(Level level, BlockPos pos) {
        return false;
    }

    public static void registerEclipticSeasonsProvider (){
        SeasonCompatibilityHandler.registerSeasonManager(EclipticSeasons.MODID, () ->
                new NormalSeasonManager(
                        world -> !world.dimensionType().hasFixedTime() ?
                                new Tuple<>(new EclipticSeasonsProvider(), new ActiveSeasonGrowthCalculator()) :
                                new Tuple<>(new NullSeasonProvider(), new NullSeasonGrowthCalculator())
                ));
    }
}
