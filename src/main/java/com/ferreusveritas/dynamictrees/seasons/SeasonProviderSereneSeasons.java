package com.ferreusveritas.dynamictrees.seasons;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import sereneseasons.api.season.Season.SubSeason;
import sereneseasons.api.season.SeasonHelper;
import sereneseasons.config.BiomeConfig;
import sereneseasons.init.ModConfig;
import sereneseasons.season.SeasonASMHelper;

public class SeasonProviderSereneSeasons implements ISeasonProvider {
	
	private float seasonValue = 1.0f;
	
	@Override
	public Float getSeasonValue(World world, BlockPos pos) {
		return seasonValue;
	}
	
	@Override
	public void updateTick(World world, long worldTicks) {
		seasonValue = ((SeasonHelper.getSeasonState(world).getSubSeason().ordinal() + 0.5f) / SubSeason.VALUES.length) * 4.0f;
	}
	
	@Override
	public boolean shouldSnowMelt(World world, BlockPos pos) {
		if(ModConfig.seasons.generateSnowAndIce && seasonValue < com.ferreusveritas.dynamictrees.seasons.SeasonHelper.WINTER) {
			Biome biome = world.getBiome(pos);
			return BiomeConfig.enablesSeasonalEffects(biome) && SeasonASMHelper.getFloatTemperature(world, biome, pos) >= 0.15f;
		}
		return false;
	}
	
}
