package com.dtteam.dynamictrees.compat;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.systems.season.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

public class SereneSeasonsSeasonProvider implements SeasonProvider {

    private float seasonValue = 1.0f;

    @Override
    public Float getSeasonValue(Level level, BlockPos pos) {
        return seasonValue;
    }

    @Override
    public void updateTick(Level level, long dayTime) {
//        seasonValue = ((SeasonHelper.getSeasonState(level).getSubSeason().ordinal() + 0.5f) / SubSeason.VALUES.length) * 4.0f;
    }

    @Override
    public boolean shouldSnowMelt(Level level, BlockPos pos) {
//        if (ModConfig.seasons.generateSnowAndIce && seasonValue < com.dtteam.dynamictrees.compat.season.SeasonHelper.WINTER) {
//            Holder<Biome> biomeHolder = level.getBiome(pos);
//            // TODO 1.20: Reinstate Serene Seasons compat here, BiomeConfig class is gone
//            return /*BiomeConfig.enablesSeasonalEffects(biomeHolder) &&*/
//                    SeasonHooks.getBiomeTemperature(level, biomeHolder, pos) >= 0.15f;
//        }
        return false;
    }

    public static void registerSereneSeasonsProvider (){
//        SeasonCompatibilityHandler.registerSeasonManager(DynamicTrees.SERENE_SEASONS, () -> {
//            NormalSeasonManager seasonManager = new NormalSeasonManager(
//                    world -> ModConfig.seasons.isDimensionWhitelisted(world.dimension()) ?
//                            new Tuple<>(new SereneSeasonsSeasonProvider(), new ActiveSeasonGrowthCalculator()) :
//                            new Tuple<>(new NullSeasonProvider(), new NullSeasonGrowthCalculator())
//            );
//            seasonManager.setTropicalPredicate((world, pos) -> sereneseasons.api.season.SeasonHelper.usesTropicalSeasons(world.getBiome(pos)));
//            return seasonManager;
//        });
    }
}
