package com.ferreusveritas.dynamictrees.compat.seasons;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import sereneseasons.api.season.Season.SubSeason;
import sereneseasons.api.season.SeasonHelper;
import sereneseasons.config.BiomeConfig;
import sereneseasons.config.SeasonsConfig;
import sereneseasons.season.SeasonHooks;

import java.util.Objects;

public class SereneSeasonsSeasonProvider implements SeasonProvider {

    private float seasonValue = 1.0f;

    @Override
    public Float getSeasonValue(Level world, BlockPos pos) {
        return seasonValue;
    }

    @Override
    public void updateTick(Level world, long worldTicks) {
        seasonValue = ((SeasonHelper.getSeasonState(world).getSubSeason().ordinal() + 0.5f) / SubSeason.VALUES.length) * 4.0f;
    }

    @Override
    public boolean shouldSnowMelt(Level world, BlockPos pos) {
        if (SeasonsConfig.generateSnowAndIce.get() && seasonValue < com.ferreusveritas.dynamictrees.compat.seasons.SeasonHelper.WINTER) {
            final ResourceKey<Biome> biome = ResourceKey.create(Registry.BIOME_REGISTRY, Objects.requireNonNull(world.getBiome(pos).getRegistryName()));
            return BiomeConfig.enablesSeasonalEffects(biome) &&
                    SeasonHooks.getBiomeTemperature(world, biome, pos) >= 0.15f;
        }
        return false;
    }

}
