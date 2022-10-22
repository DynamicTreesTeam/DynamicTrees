package com.ferreusveritas.dynamictrees.compat.season;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import sereneseasons.api.season.Season.SubSeason;
import sereneseasons.api.season.SeasonHelper;
import sereneseasons.config.BiomeConfig;
import sereneseasons.config.SeasonsConfig;
import sereneseasons.season.SeasonHooks;

public class SereneSeasonsSeasonProvider implements SeasonProvider {

    private float seasonValue = 1.0f;

    @Override
    public Float getSeasonValue(Level level, BlockPos pos) {
        return seasonValue;
    }

    @Override
    public void updateTick(Level level, long dayTime) {
        seasonValue = ((SeasonHelper.getSeasonState(level).getSubSeason().ordinal() + 0.5f) / SubSeason.VALUES.length) * 4.0f;
    }

    @Override
    public boolean shouldSnowMelt(Level level, BlockPos pos) {
        if (SeasonsConfig.generateSnowAndIce.get() && seasonValue < com.ferreusveritas.dynamictrees.compat.season.SeasonHelper.WINTER) {
            Holder<Biome> biomeHolder = level.getBiome(pos);
            return BiomeConfig.enablesSeasonalEffects(biomeHolder) &&
                    SeasonHooks.getBiomeTemperature(level, biomeHolder, pos) >= 0.15f;
        }
        return false;
    }

}
