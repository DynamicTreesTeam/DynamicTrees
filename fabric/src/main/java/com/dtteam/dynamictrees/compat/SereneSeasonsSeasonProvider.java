package com.dtteam.dynamictrees.compat;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.season.SeasonProvider;
import com.dtteam.dynamictrees.systems.season.*;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import sereneseasons.api.season.Season;
import sereneseasons.init.ModConfig;

public class SereneSeasonsSeasonProvider implements SeasonProvider {

    private float seasonValue = 1.0f;

    @Override
    public Float getSeasonValue(Level level, BlockPos pos) {
        return seasonValue;
    }

    @Override
    public void updateTick(Level level, long dayTime) {
        seasonValue = ((sereneseasons.api.season.SeasonHelper.getSeasonState(level).getSubSeason().ordinal() + 0.5f) / Season.SubSeason.VALUES.length) * 4.0f;
    }

    @Override
    public boolean shouldSnowMelt(Level level, BlockPos pos) {
        if (ModConfig.seasons.generateSnowAndIce && seasonValue < SeasonHelper.WINTER_START) {
            return level.getBiome(pos).value().warmEnoughToRain(pos, level.getSeaLevel());
        }
        return false;
    }

    public static void registerSereneSeasonsProvider (){
        SeasonCompatibilityHandler.registerSeasonManager(DynamicTrees.SERENE_SEASONS, () -> {
            NormalSeasonManager seasonManager = new NormalSeasonManager(
                    world -> ModConfig.seasons.isDimensionWhitelisted(world.dimension()) ?
                            new Pair<>(new SereneSeasonsSeasonProvider(), new ActiveSeasonGrowthCalculator()) :
                            new Pair<>(new NullSeasonProvider(), new NullSeasonGrowthCalculator())
            );
            seasonManager.setTropicalPredicate((world, pos) -> sereneseasons.api.season.SeasonHelper.usesTropicalSeasons(world.getBiome(pos)));
            return seasonManager;
        });
    }
}
